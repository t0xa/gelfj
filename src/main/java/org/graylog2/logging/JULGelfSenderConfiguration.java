package org.graylog2.logging;

import java.util.logging.LogManager;

import org.graylog2.sender.GelfSenderConfiguration;

public class JULGelfSenderConfiguration implements GelfSenderConfiguration {
	private String graylogHost;
	private String amqpURI;
	private String amqpExchangeName;
	private String amqpRoutingKey;
	private int socketSendBufferSize;
	private int amqpMaxRetries;
	private int graylogPort;

	public JULGelfSenderConfiguration(String prefix, LogManager manager) {
		final String port = manager.getProperty(prefix + ".graylogPort");
		String sendBufferSize = manager.getProperty(prefix + ".socketSendBufferSize");
		String maxRetries = manager.getProperty(prefix + ".amqpMaxRetries");

		this.graylogHost = manager.getProperty(prefix + ".graylogHost");
		this.graylogPort = port == null ? 12201 : Integer.parseInt(port);
		this.amqpURI = manager.getProperty(prefix + ".amqpURI");
		this.socketSendBufferSize = sendBufferSize == null ? 0 : Integer.parseInt(sendBufferSize);
		this.amqpExchangeName = manager.getProperty(prefix + ".amqpExchangeName");
		this.amqpRoutingKey = manager.getProperty(prefix + ".amqpRoutingKey");
		this.amqpMaxRetries = maxRetries == null ? 0 : Integer.valueOf(maxRetries);
	}

	public String getGraylogHost() {
		return graylogHost;
	}

	public int getGraylogPort() {
		return graylogPort;
	}

	public int getSocketSendBufferSize() {
		return socketSendBufferSize;
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