package org.graylog2.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;
import org.graylog2.GelfSenderResult;

public class GelfHandler extends Handler {
	private static final int MAX_SHORT_MESSAGE_LENGTH = 250;

	private SenderConfiguration senderConfiguration;
	private String facility;
	private String originHost;
	private Map<String, String> fields;
	private GelfSender gelfSender;
	private volatile boolean closed;

	public GelfHandler() {
		LogManager manager = LogManager.getLogManager();
		String prefix = getClass().getName();
		this.senderConfiguration = new SenderConfiguration(prefix, manager);

		configure(manager, prefix);
	}

	public GelfHandler(SenderConfiguration senderConfiguration) {
		LogManager manager = LogManager.getLogManager();
		String prefix = getClass().getName();
		this.senderConfiguration = senderConfiguration;

		configure(manager, prefix);
	}

	private void configure(LogManager manager, String prefix) {
		facility = manager.getProperty(prefix + ".facility");
		originHost = manager.getProperty(prefix + ".originHost");
		if (originHost == null) {
			originHost = getLocalHostName();
		}

		int fieldNumber = 0;
		fields = new HashMap<String, String>();
		while (true) {
			final String property = manager.getProperty(prefix + ".additionalField." + fieldNumber);
			if (null == property) {
				break;
			}
			final int index = property.indexOf('=');
			if (-1 != index) {
				fields.put(property.substring(0, index), property.substring(index + 1));
			}
			fieldNumber++;
		}

		final String level = manager.getProperty(prefix + ".level");
		if (null != level) {
			setLevel(Level.parse(level.trim()));
		} else {
			setLevel(Level.INFO);
		}

		boolean extractStacktrace = "true".equalsIgnoreCase(manager.getProperty(prefix + ".extractStacktrace"));
		setFormatter(new SimpleFormatter(extractStacktrace));

		final String filter = manager.getProperty(prefix + ".filter");
		try {
			if (null != filter) {
				setFilter((Filter) getClass().getClassLoader().loadClass(filter).newInstance());
			}
		} catch (final Exception e) {
			// ignore
		}
		// This only used for testing
		final String testSender = manager.getProperty(prefix + ".graylogTestSenderClass");
		try {
			if (null != testSender) {
				gelfSender = (GelfSender) getClass().getClassLoader().loadClass(testSender).newInstance();
			}
		} catch (final Exception e) {
			// ignore
		}
	}

	@Override
	public synchronized void flush() {
	}

	@Override
	public void publish(final LogRecord record) {
		if (!closed && isLoggable(record)) {
			send(record);
		}
	}

	private synchronized void send(LogRecord record) {
		try {
			if (null == gelfSender) {
				gelfSender = new GelfSenderFactory().createSender(senderConfiguration);
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

	@Override
	public synchronized void close() {
		if (null != gelfSender) {
			gelfSender.close();
			gelfSender = null;
		}
		closed = true;
	}

	private GelfMessage makeMessage(final LogRecord record) {
		String message = getFormatter().format(record);
		final String shortMessage = formatShortMessage(message);
		final GelfMessage gelfMessage = new GelfMessage(shortMessage, message, record.getMillis(),
				String.valueOf(levelToSyslogLevel(record.getLevel())));
		gelfMessage.addField("SourceClassName", record.getSourceClassName());
		gelfMessage.addField("SourceMethodName", record.getSourceMethodName());
		if (null != originHost) {
			gelfMessage.setHost(originHost);
		}
		if (null != facility) {
			gelfMessage.setFacility(facility);
		}
		if (null != fields) {
			for (final Map.Entry<String, String> entry : fields.entrySet()) {
				gelfMessage.addField(entry.getKey(), entry.getValue());
			}
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

	private int levelToSyslogLevel(final Level level) {
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

	// public void setGraylogPort(int graylogPort) {
	// this.graylogPort = graylogPort;
	// }
	//
	// public void setOriginHost(String originHost) {
	// this.originHost = originHost;
	// }
	//
	// public void setGraylogHost(String graylogHost) {
	// this.graylogHost = graylogHost;
	// }
	//
	// public void setFacility(String facility) {
	// this.facility = facility;
	// }
	//
	private String getLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException uhe) {
			throw new IllegalStateException(
					"Origin host could not be resolved automatically. Please set originHost property", uhe);
		}
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
