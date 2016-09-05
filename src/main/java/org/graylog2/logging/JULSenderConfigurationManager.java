package org.graylog2.logging;

import java.util.logging.LogManager;

import org.graylog2.sender.GelfSenderConfiguration;

public class JULSenderConfigurationManager {
	private JULSenderConfigurationManager() {
	}

	public static GelfSenderConfiguration getConfiguration(String prefix, LogManager manager) {
		final String port = manager.getProperty(prefix + ".graylogPort");
		String sendBufferSize = manager.getProperty(prefix + ".socketSendBufferSize");
		String maxRetries = manager.getProperty(prefix + ".amqpMaxRetries");

		GelfSenderConfiguration configuration = new GelfSenderConfiguration();
		configuration.setGraylogHost(manager.getProperty(prefix + ".graylogHost"));
		if (port != null) {
			configuration.setGraylogPort(Integer.parseInt(port));
		}
		configuration.setAmqpURI(manager.getProperty(prefix + ".amqpURI"));
		if (sendBufferSize != null) {
			configuration.setSocketSendBufferSize(Integer.parseInt(sendBufferSize));
		}
		configuration.setAmqpExchangeName(manager.getProperty(prefix + ".amqpExchangeName"));
		configuration.setAmqpRoutingKey(manager.getProperty(prefix + ".amqpRoutingKey"));
		if (maxRetries != null) {
			configuration.setAmqpMaxRetries(Integer.valueOf(maxRetries));
		}
		return configuration;
	}
}