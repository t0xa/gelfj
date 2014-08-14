package org.graylog2;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GelfMessageMutabilityTest {

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
