package com.github.pukkaone.gelf.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class GelfMessageTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GelfMessage message;

    @Before
    public void beforeMethod() throws Exception {
        message = new GelfMessage();
    }

    @Test
    public void testAdditionalFields() throws Exception {
        message.setTimestampMillis(1L);
        message.addField("one", "two").addField("three", 4).addField("five", 6.0).addField("seven", 8L);

        String json = message.toJson();

        Map resultingMap = OBJECT_MAPPER.readValue(json, HashMap.class);

        assertThat((String) resultingMap.get("_one"), is("two"));
        assertThat((Integer) resultingMap.get("_three"), is(4));
        assertThat((Double) resultingMap.get("_five"), is(6.0));
        assertThat((Integer) resultingMap.get("_seven"), is(8));
    }

    @Test
    public void when_host_is_empty_then_message_is_invalid() {
        message.setHost(null);
        message.setShortMessage("short message");

        assertThat(message.isValid(), is(false));
    }

    @Test
    public void when_short_message_is_empty_then_message_is_invalid() {
        message.setHost("localhost");
        message.setShortMessage(null);

        assertThat(message.isValid(), is(false));
    }
}
