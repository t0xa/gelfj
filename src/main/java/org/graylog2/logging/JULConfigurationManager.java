package org.graylog2.logging;

import org.graylog2.host.HostConfiguration;
import org.graylog2.sender.GelfSenderConfiguration;

public class JULConfigurationManager {
	private JULConfigurationManager() {
	}

	public static HostConfiguration getHostConfiguration(JULProperties properties) {
		String originHost = properties.getProperty("originHost");
		String facility = properties.getProperty("facility");

		HostConfiguration configuration = new HostConfiguration();
		if (originHost != null) {
			configuration.setOriginHost(originHost);
		}
		configuration.setFacility(facility);
		return configuration;
	}

	public static GelfSenderConfiguration getGelfSenderConfiguration(JULProperties properties) {
		final String port = properties.getProperty("graylogPort");
		String sendBufferSize = properties.getProperty("socketSendBufferSize");
		String maxRetries = properties.getProperty("amqpMaxRetries");

		GelfSenderConfiguration configuration = new GelfSenderConfiguration();
		configuration.setGraylogHost(properties.getProperty("graylogHost"));
		if (port != null) {
			configuration.setGraylogPort(Integer.parseInt(port));
		}
		configuration.setAmqpURI(properties.getProperty("amqpURI"));
		if (sendBufferSize != null) {
			configuration.setSocketSendBufferSize(Integer.parseInt(sendBufferSize));
		}
		configuration.setAmqpExchangeName(properties.getProperty("amqpExchangeName"));
		configuration.setAmqpRoutingKey(properties.getProperty("amqpRoutingKey"));
		if (maxRetries != null) {
			configuration.setAmqpMaxRetries(Integer.valueOf(maxRetries));
		}
		return configuration;
	}
}