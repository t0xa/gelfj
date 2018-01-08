package org.graylog2;

import junit.framework.Assert;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPInputStream;

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
        int size1 = bytes2[0].remaining() - 12;
        int size2 = bytes2[1].remaining() - 12;
        byte[] gzArray = new byte[size1 + size2];
        skipHeader(bytes2[0]);
        bytes2[0].get(gzArray, 0, size1);
        skipHeader(bytes2[1]);
        bytes2[1].get(gzArray, size1, size2);
        GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzArray));
        try {
            while (gzip.read() != -1) {}
        } catch (IOException e) {
            fail("GZIP decompression error: " + e.getMessage());
        }
    }

    private void skipHeader(ByteBuffer src) {
        for (int i = 0; i < 12; i++) {
            src.get();
        }
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
    public void concatByteArrayTest() {
        GelfMessage message = new GelfMessage("Short", "Long", 1L, "WARNING");

        byte[] test1 = message.concatByteArray("ABC is ea".getBytes(), "sy as 123".getBytes());
        assertThat("Bytes concatenates correctly", new String(test1), is("ABC is easy as 123"));

        byte[] test2 = message.concatByteArray(new byte[]{}, "sy as 123".getBytes());
        assertThat("Empty bytes concatenates correctly", new String(test2), is("sy as 123"));

        byte[] test3 = message.concatByteArray(new byte[]{}, new byte[]{});
        assertThat("Two empty bytes concatenates correctly", test3, is(new byte[]{}));
    }

    @Test
    public void numericTimestampTest() {
        GelfMessage message = new GelfMessage("Short", "Long", 1L, "WARNING");
        JSONObject object = (JSONObject) JSONValue.parse(message.toJson());
        assertThat((Double) object.get("timestamp"), is(0.001d));

        GelfMessage message2 = new GelfMessage("Short", "Long", 1515403544687L, "WARNING");
        JSONObject object2 = (JSONObject) JSONValue.parse(message2.toJson());
        assertThat( (Double) object2.get("timestamp"), is(1515403544.687d));
    }
}
