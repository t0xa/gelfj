package org.graylog2.logging;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GelfLogRecord extends LogRecord {
	private static final long serialVersionUID = 43242341L;
	private Map<String, Object> fields;

	public GelfLogRecord(Level level, String msg) {
		super(level, msg);
	}

	public void setField(String key, Object value) {
		if (fields == null) {
			fields = new LinkedHashMap<String, Object>();
		}
		fields.put(key, value);
	}

	public Object getField(String key) {
		return fields != null ? fields.get(key) : null;
	}

	public Map<String, Object> getFields() {
		if (fields == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(fields);
	}
}
