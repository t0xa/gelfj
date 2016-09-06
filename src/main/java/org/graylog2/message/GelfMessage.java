package org.graylog2.message;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class GelfMessage {
	private static final String ID_NAME = "id";
	private static final String GELF_VERSION = "1.1";
	private static final BigDecimal TIME_DIVISOR = new BigDecimal(1000);

	private String version = GELF_VERSION;
	private String host;
	private String shortMessage;
	private String fullMessage;
	private long javaTimestamp;
	private String level;
	private String facility = "gelf-java";
	private String line;
	private String file;
	private Map<String, Object> additonalFields = new HashMap<String, Object>();

	public GelfMessage() {
	}

	public GelfMessage(String shortMessage, String fullMessage, long timestamp, String level) {
		this(shortMessage, fullMessage, timestamp, level, null, null);
	}

	public GelfMessage(String shortMessage, String fullMessage, long timestamp, String level, String line,
			String file) {
		this.shortMessage = shortMessage != null ? shortMessage : "null";
		this.fullMessage = fullMessage;
		this.javaTimestamp = timestamp;
		this.level = level;
		this.line = line;
		this.file = file;
	}

	public String toJson() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("version", getVersion());
		map.put("host", getHost());
		map.put("short_message", getShortMessage());
		map.put("full_message", getFullMessage());
		map.put("timestamp", getTimestamp());

		map.put("facility", getFacility());
		try {
			map.put("level", Long.parseLong(getLevel()));
		} catch (NumberFormatException e) {
			map.put("level", 6L); // fallback to info
		}

		if (null != getFile()) {
			map.put("file", getFile());
		}
		if (null != getLine()) {
			try {
				map.put("line", Long.parseLong(getLine()));
			} catch (NumberFormatException e) {
				map.put("line", -1L);
			}
		}

		for (Map.Entry<String, Object> additionalField : additonalFields.entrySet()) {
			if (!ID_NAME.equals(additionalField.getKey())) {
				map.put("_" + additionalField.getKey(), additionalField.getValue());
			}
		}
		return JSONValue.toJSONString(map);
	}

	public int getCurrentMillis() {
		return (int) System.currentTimeMillis();
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

	public String getTimestamp() {
		return new BigDecimal(javaTimestamp).divide(TIME_DIVISOR).toPlainString();
	}

	public Long getJavaTimestamp() {
		return javaTimestamp;
	}

	public void setJavaTimestamp(long javaTimestamp) {
		this.javaTimestamp = javaTimestamp;
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

	public GelfMessage addField(String key, Object value) {
		getAdditonalFields().put(key, value);
		return this;
	}

	public Map<String, Object> getAdditonalFields() {
		return additonalFields;
	}

	public void setAdditonalFields(Map<String, Object> additonalFields) {
		this.additonalFields = new HashMap<String, Object>(additonalFields);
	}

	public boolean isValid() {
		return isShortOrFullMessagesExists() && !isEmpty(version) && !isEmpty(host) && !isEmpty(facility);
	}

	private boolean isShortOrFullMessagesExists() {
		return shortMessage != null || fullMessage != null;
	}

	public boolean isEmpty(String str) {
		return str == null || "".equals(str.trim());
	}
}
