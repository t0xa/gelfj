package org.graylog2.sender;

public class GelfSenderConfiguration {
	private String graylogHost;
	private int graylogPort;
	private String amqpURI;
	private String amqpExchangeName;
	private String amqpRoutingKey;
	private int socketSendBufferSize;
	private int amqpMaxRetries;

	public GelfSenderConfiguration() {
		this.graylogPort = 12201;
	}

	public String getGraylogHost() {
		return graylogHost;
	}

	public void setGraylogHost(String graylogHost) {
		this.graylogHost = graylogHost;
	}

	public int getGraylogPort() {
		return graylogPort;
	}

	public void setGraylogPort(int graylogPort) {
		this.graylogPort = graylogPort;
	}

	public String getAmqpURI() {
		return amqpURI;
	}

	public void setAmqpURI(String amqpURI) {
		this.amqpURI = amqpURI;
	}

	public String getAmqpExchangeName() {
		return amqpExchangeName;
	}

	public void setAmqpExchangeName(String amqpExchangeName) {
		this.amqpExchangeName = amqpExchangeName;
	}

	public String getAmqpRoutingKey() {
		return amqpRoutingKey;
	}

	public void setAmqpRoutingKey(String amqpRoutingKey) {
		this.amqpRoutingKey = amqpRoutingKey;
	}

	public int getSocketSendBufferSize() {
		return socketSendBufferSize;
	}

	public void setSocketSendBufferSize(int socketSendBufferSize) {
		this.socketSendBufferSize = socketSendBufferSize;
	}

	public int getAmqpMaxRetries() {
		return amqpMaxRetries;
	}

	public void setAmqpMaxRetries(int amqpMaxRetries) {
		this.amqpMaxRetries = amqpMaxRetries;
	}
}