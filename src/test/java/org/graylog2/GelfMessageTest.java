package org.graylog2;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    public void testSendLongMessage() throws Exception {
        String longString = "01234567890123456789 ";
        for (int i = 0; i < 15; i++) {
            longString += longString;
        }
        GelfMessage message = new GelfMessage("Long", longString, new Date().getTime(), "1");
        ByteBuffer[] bytes2 = message.toUDPBuffers();
        assertEquals(2, bytes2.length);
        assertTrue(bytes2[0].get(10) ==  (byte) 0x00);
        assertTrue(bytes2[0].get(11) == (byte) 0x02);
        assertTrue(bytes2[1].get(10) == (byte) 0x01);
        assertTrue(bytes2[1].get(11) == (byte) 0x02);
    }

    @Test
    public void testSimpleMessage() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime(), "1");
        ByteBuffer[] bytes = message.toUDPBuffers();
        assertEquals(1, bytes.length);
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
        assertThat("Short message is set to <empty> when null", message.getShortMessage(), is("<empty>"));

        message.setFullMessage(null);
        assertThat("Not valid when not full message set nor short message set", message.isValid(), is(false));

        message.setShortMessage("Hamburg");
        message.setFullMessage(null);
        assertThat("Valid when short message is set", message.isValid(), is(true));
    }

    @Test
    public void testInvalidLevelMessage() {
        GelfMessage message = new GelfMessage("Short", "Long", 1L, "WARNING");
        message.setHost("localhost");

        JSONObject object = (JSONObject) JSONValue.parse(message.toJson());

        assertThat("Message with invalid level defaults to info", (Long) object.get("level"), is(6L));
    }
}
