package org.graylog2.sender;

public interface GelfSenderConfiguration {

	String getGraylogHost();

	int getGraylogPort();

	int getSocketSendBufferSize();

	String getAmqpURI();

	String getAmqpExchangeName();

	String getAmqpRoutingKey();

	int getAmqpMaxRetries();

}