package org.graylog2;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

public class GelfMessageTest {

    @Test
    public void testAdditionalFieldsIds() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime()/1000L, "1");
        Map<String,Object> additonalFields = message.getAdditonalFields();
        additonalFields.put("id", "LOLCAT");
        additonalFields.put("_id", "typos in my closet");

        String data = message.toJson();
        Map resultingMap = (Map) JSONValue.parse(data);
        assertNull(resultingMap.get("_id"));
        assertNotNull(resultingMap.get("__id"));
    }

    @Test
    public void testSendLongMessage() throws Exception {
        String longString = "01234567890123456789 ";
        for(int i =0; i < 15; i++) {
            longString += longString;
        }
        System.out.println(longString.length());
        GelfMessage message = new GelfMessage("Long", longString , new Date().getTime()/1000L, "1");
        List<byte[]> bytes2 = message.toDatagrams();
        assertEquals(2, bytes2.size());
    }

    @Test
    public void testSimpleMessage() throws Exception {
        GelfMessage message = new GelfMessage("Short", "Long", new Date().getTime()/1000L, "1");
        List<byte[]> bytes = message.toDatagrams();
        assertEquals(1, bytes.size());
    }
}
