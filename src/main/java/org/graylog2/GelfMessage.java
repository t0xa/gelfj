package org.graylog2;

import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class GelfMessage {

    public static final int MAXIMUM_CHUNK_SIZE = 1420;
    public static final String GELF_VERSION = "1.0";
    public static final byte[] GELF_CHUNKED_ID = new byte[]{0x1e, 0x0f};

    private static final String ID_NAME = "id";

    private String version = GELF_VERSION;
    private String host;
    private String shortMessage;
    private String fullMessage;
    private Long timestamp;
    private String level;
    private String facility = "gelf-java";
    private String line;
    private String file;
    private Map<String, Object> additonalFields = new HashMap<String, Object>();

    public GelfMessage() {
    }

    public GelfMessage(String shortMessage, String fullMessage, Date timestamp, String level) {
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.timestamp = timestamp.getTime() / 1000L;
        this.level = level;
    }

    public GelfMessage(String shortMessage, String fullMessage, Long timestamp, String level) {
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.timestamp = timestamp;
        this.level = level;
    }

    public GelfMessage(String shortMessage, String fullMessage, Long timestamp, String level, String line, String file) {
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.timestamp = timestamp / 1000L;
        this.level = level;
        this.line = line;
        this.file = file;
    }

    public String toJson() {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("version", getVersion());
        map.put("host", getHost());
        map.put("short_message", getShortMessage());
        map.put("full_message", getFullMessage());
        map.put("timestamp", getTimestamp().intValue());

        map.put("level", getLevel());
        map.put("facility", getFacility());
        map.put("file", getFile());
        map.put("line", getLine());

        for (String additionalField : additonalFields.keySet()) {
            if (ID_NAME.equals(additionalField)) continue;
            Object value = additonalFields.get(additionalField);
            map.put("_" + additionalField, value);
        }

        return JSONValue.toJSONString(map);
    }

    public List<byte[]> toDatagrams() {
        byte[] messageBytes = gzipMessage(toJson());
        List<byte[]> datagrams = new ArrayList<byte[]>();
        if (messageBytes.length > MAXIMUM_CHUNK_SIZE) {
            sliceDatagrams(messageBytes, datagrams);
        } else {
            datagrams.add(messageBytes);
        }
        return datagrams;
    }

    private void sliceDatagrams(byte[] messageBytes, List<byte[]> datagrams) {
        int messageLength = messageBytes.length;
        byte[] messageId = Arrays.copyOf((new Date().getTime() + getHost()).getBytes(), 32);
        int num = ((Double) Math.ceil((double) messageLength / MAXIMUM_CHUNK_SIZE)).intValue();
        for (int idx = 0; idx < num; idx++) {
            byte[] header = concatByteArray(GELF_CHUNKED_ID, concatByteArray(messageId, new byte[]{0x00, (byte) idx, 0x00, (byte) num}));
            int from = idx * MAXIMUM_CHUNK_SIZE;
            int to = from + MAXIMUM_CHUNK_SIZE;
            if (to >= messageLength) {
                to = messageLength;
            }
            byte[] datagram = concatByteArray(header, Arrays.copyOfRange(messageBytes, from, to));
            datagrams.add(datagram);
        }
    }

    private byte[] gzipMessage(String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream stream = new GZIPOutputStream(bos);
            stream.write(message.getBytes());
            stream.close();
            byte[] zipped = bos.toByteArray();
            bos.close();
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public GelfMessage addField(String key, String value) {
        getAdditonalFields().put(key, value);
        return this;
    }

    public Map<String, Object> getAdditonalFields() {
        return additonalFields;
    }

    public void setAdditonalFields(Map<String, Object> additonalFields) {
        this.additonalFields = additonalFields;
    }

    public boolean isValid() {
        return !isEmpty(version) && !isEmpty(host) && !isEmpty(shortMessage) && !isEmpty(facility);
    }

    public boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public byte[] concatByteArray(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
