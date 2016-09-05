package org.graylog2.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.graylog2.host.HostConfiguration;
import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSender;
import org.graylog2.sender.GelfSenderConfiguration;
import org.graylog2.sender.GelfSenderConfigurationException;
import org.graylog2.sender.GelfSenderFactory;
import org.graylog2.sender.GelfSenderResult;

public class GelfHandler extends Handler {
	private static final int MAX_SHORT_MESSAGE_LENGTH = 250;
	private HostConfiguration hostConfiguration;
	private GelfSenderConfiguration senderConfiguration;
	private Map<String, String> fields;
	private GelfSender gelfSender;
	private boolean closed;

	public GelfHandler() {
		JULProperties properties = new JULProperties(LogManager.getLogManager(), getClass().getName());
		this.hostConfiguration = JULConfigurationManager.getHostConfiguration(properties);
		this.senderConfiguration = JULConfigurationManager.getGelfSenderConfiguration(properties);

		configure(properties);
	}

	public GelfHandler(GelfSenderConfiguration senderConfiguration) {
		JULProperties properties = new JULProperties(LogManager.getLogManager(), getClass().getName());
		this.hostConfiguration = JULConfigurationManager.getHostConfiguration(properties);
		this.senderConfiguration = senderConfiguration;

		configure(properties);
	}

	private void configure(JULProperties properties) {
		int fieldNumber = 0;
		fields = new HashMap<String, String>();
		while (true) {
			final String property = properties.getProperty("additionalField." + fieldNumber);
			if (null == property) {
				break;
			}
			final int index = property.indexOf('=');
			if (-1 != index) {
				fields.put(property.substring(0, index), property.substring(index + 1));
			}
			fieldNumber++;
		}

		final String level = properties.getProperty("level");
		if (null != level) {
			setLevel(Level.parse(level.trim()));
		} else {
			setLevel(Level.INFO);
		}

		boolean extractStacktrace = "true".equalsIgnoreCase(properties.getProperty("extractStacktrace"));
		setFormatter(new SimpleFormatter(extractStacktrace));

		final String filter = properties.getProperty("filter");
		try {
			if (null != filter) {
				setFilter((Filter) getClass().getClassLoader().loadClass(filter).newInstance());
			}
		} catch (Exception ignoredException) {
		}
	}

	@Override
	public synchronized void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record)) {
			send(record);
		}
	}

	private synchronized void send(LogRecord record) {
		if (!closed) {
			try {
				if (null == gelfSender) {
					gelfSender = GelfSenderFactory.getInstance().createSender(senderConfiguration);
				}
				GelfSenderResult gelfSenderResult = gelfSender.sendMessage(makeMessage(record));
				if (!GelfSenderResult.OK.equals(gelfSenderResult)) {
					reportError("Error during sending GELF message. Error code: " + gelfSenderResult.getCode() + ".",
							gelfSenderResult.getException(), ErrorManager.WRITE_FAILURE);
				}
			} catch (GelfSenderConfigurationException exception) {
				reportError(exception.getMessage(), exception.getCauseException(), ErrorManager.WRITE_FAILURE);
			} catch (Exception exception) {
				reportError("Could not send GELF message", exception, ErrorManager.WRITE_FAILURE);
			}
		}
	}

	@Override
	public synchronized void close() {
		if (null != gelfSender) {
			gelfSender.close();
			gelfSender = null;
		}
		closed = true;
	}

	private GelfMessage makeMessage(LogRecord record) {
		String message = getFormatter().format(record);
		final String shortMessage = formatShortMessage(message);
		final GelfMessage gelfMessage = new GelfMessage(shortMessage, message, record.getMillis(),
				String.valueOf(levelToSyslogLevel(record.getLevel())));
		gelfMessage.addField("SourceClassName", record.getSourceClassName());
		gelfMessage.addField("SourceMethodName", record.getSourceMethodName());
		if (null != hostConfiguration.getOriginHost()) {
			gelfMessage.setHost(hostConfiguration.getOriginHost());
		}
		if (null != hostConfiguration.getFacility()) {
			gelfMessage.setFacility(hostConfiguration.getFacility());
		}
		for (final Map.Entry<String, String> entry : fields.entrySet()) {
			gelfMessage.addField(entry.getKey(), entry.getValue());
		}
		return gelfMessage;
	}

	private String formatShortMessage(String message) {
		final String shortMessage;
		if (message.length() > MAX_SHORT_MESSAGE_LENGTH) {
			shortMessage = message.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
		} else {
			shortMessage = message;
		}
		return shortMessage;
	}

	private int levelToSyslogLevel(Level level) {
		final int syslogLevel;
		if (level.intValue() == Level.SEVERE.intValue()) {
			syslogLevel = 3;
		} else if (level.intValue() == Level.WARNING.intValue()) {
			syslogLevel = 4;
		} else if (level.intValue() == Level.INFO.intValue()) {
			syslogLevel = 6;
		} else {
			syslogLevel = 7;
		}
		return syslogLevel;
	}

	public synchronized void setGelfSender(GelfSender gelfSender) {
		this.gelfSender = gelfSender;
	}

	public void setAdditionalField(String entry) {
		if (entry == null)
			return;
		final int index = entry.indexOf('=');
		if (-1 != index) {
			String key = entry.substring(0, index);
			String val = entry.substring(index + 1);
			if (key.equals(""))
				return;
			fields.put(key, val);
		}
	}

	public Map<String, String> getFields() {
		return fields;
	}
}
