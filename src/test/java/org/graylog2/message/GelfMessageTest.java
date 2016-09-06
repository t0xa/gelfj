package org.graylog2.message;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class GelfMessageTest {
    @Test
    public void testAdditionalFieldsIds() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime(), "1");
        message.addField("id", "LOLCAT").addField("_id", "typos in my closet");

        String data = message.toJson();
        Map resultingMap = (Map) JSONValue.parse(data);
        assertNull(resultingMap.get("_id"));
        assertNotNull(resultingMap.get("__id"));
    }
    
    @Test
    public void testAdditionalFields() throws Exception {
        GelfMessage message = new GelfMessage();
        message.setJavaTimestamp(1L);
        message.addField("one", "two").addField("three", 4).addField("five", 6.0).addField("seven",8);

        String json = message.toJson();

        Map resultingMap = (Map) JSONValue.parse(json);

        assertThat("String is string", (String) resultingMap.get("_one"), is("two"));
        assertThat("Long is long", (Long) resultingMap.get("_three"), is(4L));
        assertThat("Int is int", (Double) resultingMap.get("_five"), is(6.0));
        assertThat("Second Long is long", (Long) resultingMap.get("_seven"), is(8L));
    }

    @Test
    public void testEmptyShortMessage() {
        GelfMessage message = new GelfMessage(null, "Long message", 1L, "1");
        message.setHost("localhost");
        message.setVersion("0.0");

        assertThat("Message with empty short message is Valid", message.isValid(), is(true));
        assertThat("Short message is set to 'null' when null", message.getShortMessage(), is("null"));

        message.setFullMessage(null);
        assertThat("An empty message is valid (neither full nor short message set)", message.isValid(), is(true));

        message.setShortMessage("Hamburg");
        message.setFullMessage(null);
        assertThat("Valid when short message is set", message.isValid(), is(true));
    }

    @Test
    public void testZeroLengthMessage() {
        GelfMessage message = new GelfMessage("", "", 1L, "1");

        message.setHost("localhost");
        message.setVersion("0.0");
        assertThat("Message with a zero length short message is Valid",
          message.isValid(), is(true));
        assertThat("Short message is set to an empty string when zero length",
          message.getShortMessage(), is(""));
    }

    @Test
    public void testInvalidLevelMessage() {
        GelfMessage message = new GelfMessage("Short", "Long", 1L, "WARNING");
        message.setHost("localhost");

        JSONObject object = (JSONObject) JSONValue.parse(message.toJson());

        assertThat("Message with invalid level defaults to info", (Long) object.get("level"), is(6L));
    }
    
    @Test
    public void testMutability() {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime(), "1");

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", "id");
        map.put("x", "y");

        message.setAdditonalFields(map);

        message.addField("idz", "LOLCAT").addField("_id", "typos in my closet");

        assertThat("Modifying original map doesn't modify message map", map.size(), is(2));
        assertThat("Modifying original map ", message.getAdditonalFields().size(), is(4));
    }

    @Test
    public void testJavaImmutableMap() {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime(), "1");

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("id", "id");
        map.put("x", "y");
        Map<String, Object> additonalFields = Collections.unmodifiableMap(map);

        message.setAdditonalFields(additonalFields);

        message.addField("idz", "LOLCAT").addField("_id", "typos in my closet");

        assertThat("If created from Immutable map, it should be possible to add fields", message.getAdditonalFields().size(), is(4));
    }

    @Test
    public void testGuavaImmutableMaps() {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime(), "1");

        ImmutableMap<String, Object> build = ImmutableMap.<String, Object>builder().put("id", "id").put("x", "y").build();
        message.setAdditonalFields(build);

        message.addField("idz", "LOLCAT").addField("_id", "typos in my closet");

        assertThat("Modifying original map doesn't modify message map", message.getAdditonalFields().size(), is(4));
    }
}
