package com.github.pukkaone.gelf.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class GelfMessage {

    private static final String VERSION_VALUE = "1.1";
    private static final String FACILITY_VALUE = "gelf-java";
    private static final String HOST = "host";
    private static final String SHORT_MESSAGE = "short_message";
    private static final String FULL_MESSAGE = "full_message";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private long timestampMillis;
    private Map<String, Object> fieldNameToValueMap = new HashMap<>();

    public GelfMessage() {
        fieldNameToValueMap.put("version", VERSION_VALUE);
        fieldNameToValueMap.put("_facility", FACILITY_VALUE);
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public GelfMessage setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;

        fieldNameToValueMap.put("timestamp", timestampMillis / 1000.0);
        return this;
    }

    public String getHost() {
        return (String) fieldNameToValueMap.get(HOST);
    }

    public GelfMessage setHost(String host) {
        fieldNameToValueMap.put(HOST, host);
        return this;
    }

    public GelfMessage setLevel(int level) {
        fieldNameToValueMap.put("level", level);
        return this;
    }

    public GelfMessage setFile(String file) {
        fieldNameToValueMap.put("_file", file);
        return this;
    }

    public GelfMessage setLine(int line) {
        fieldNameToValueMap.put("_line", line);
        return this;
    }

    public GelfMessage setLogger(String logger) {
        fieldNameToValueMap.put("_logger", logger);
        return this;
    }

    public GelfMessage setMarker(String marker) {
        fieldNameToValueMap.put("_marker", marker);
        return this;
    }

    public GelfMessage setThread(String thread) {
        fieldNameToValueMap.put("_thread", thread);
        return this;
    }

    public GelfMessage setShortMessage(String shortMessage) {
        fieldNameToValueMap.put(SHORT_MESSAGE, shortMessage);
        return this;
    }

    public GelfMessage setFullMessage(String fullMessage) {
        fieldNameToValueMap.put(FULL_MESSAGE, fullMessage);
        return this;
    }

    public Object getField(String name) {
        return fieldNameToValueMap.get('_' + name);
    }

    public GelfMessage addField(String name, Object value) {
        // Disallow sending id additional field.
        if (!"id".equals(name)) {
            fieldNameToValueMap.put('_' + name, value);
        }
        return this;
    }

    public boolean isEmpty(String fieldName) {
        Object value = fieldNameToValueMap.get(fieldName);
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            String s = (String) value;
            return "".equals(s.trim());
        }

        return true;
    }

    public boolean isValid() {
        return !isEmpty(SHORT_MESSAGE) && !isEmpty(HOST);
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(fieldNameToValueMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot write JSON", e);
        }
    }
}
