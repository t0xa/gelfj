package org.graylog2.logging;

import java.util.logging.LogManager;

public class SenderConfiguration {
	private String graylogHost;
	private String amqpURI;
	private String amqpExchangeName;
	private String amqpRoutingKey;
	private int amqpMaxRetries;
	private int graylogPort;

	public SenderConfiguration(String prefix, LogManager manager) {
		graylogHost = manager.getProperty(prefix + ".graylogHost");
		final String port = manager.getProperty(prefix + ".graylogPort");
		graylogPort = null == port ? 12201 : Integer.parseInt(port);
		amqpURI = manager.getProperty(prefix + ".amqpURI");
		amqpExchangeName = manager.getProperty(prefix + ".amqpExchangeName");
		amqpRoutingKey = manager.getProperty(prefix + ".amqpRoutingKey");
		String maxRetries = manager.getProperty(prefix + ".amqpMaxRetries");
		amqpMaxRetries = maxRetries == null ? 0 : Integer.valueOf(maxRetries);
	}

	public String getGraylogHost() {
		return graylogHost;
	}
	
	public int getGraylogPort() {
		return graylogPort;
	}

	public String getAmqpURI() {
		return amqpURI;
	}

	public String getAmqpExchangeName() {
		return amqpExchangeName;
	}

	public String getAmqpRoutingKey() {
		return amqpRoutingKey;
	}

	public int getAmqpMaxRetries() {
		return amqpMaxRetries;
	}
}