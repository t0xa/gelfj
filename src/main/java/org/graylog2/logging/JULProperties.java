package org.graylog2.logging;

import java.util.logging.LogManager;

public class JULProperties {
	private final LogManager manager;
	private final String prefix;

	public JULProperties(LogManager manager, String prefix) {
		this.manager = manager;
		this.prefix = prefix;
	}

	public String getProperty(String key) {
		return manager.getProperty(prefix + "." + key);
	}
}
