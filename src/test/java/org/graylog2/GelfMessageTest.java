package org.graylog2;

import org.json.simple.JSONValue;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

public class GelfMessageTest {

    @Test
    public void testAdditionalFieldsIds() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date(), "1");
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
        GelfMessage message = new GelfMessage("Long", longString, new Date(), "1");
        List<byte[]> bytes2 = message.toDatagrams();
        assertEquals(2, bytes2.size());
    }

    @Test
    public void testSimpleMessage() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date(), "1");
        List<byte[]> bytes = message.toDatagrams();
        assertEquals(1, bytes.size());
    }

    @Test
    public void testAdditionalFields() throws Exception {
        GelfMessage message = new GelfMessage();
        message.setTimestamp(1L);
        message.addField("one", "two").addField("three", 4).addField("five", 6.0);

        String json = message.toJson();

        Map resultingMap = (Map) JSONValue.parse(json);

        assertEquals(resultingMap.get("_one"), "two");
        assertEquals(resultingMap.get("_three"), 4L);
        assertEquals(resultingMap.get("_five"), 6.0);
        assertEquals(resultingMap.get("_seven"), 8L);
    }

    @Test
    public void generateBallastMessage() {
        Date date = new Date();
    }
}
