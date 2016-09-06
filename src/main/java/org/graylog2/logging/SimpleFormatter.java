package org.graylog2.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleFormatter extends Formatter {
	private boolean extractStacktrace;

	public SimpleFormatter(boolean extractStacktrace) {
		this.extractStacktrace = extractStacktrace;
	}

	@Override
	public String format(LogRecord record) {
		String message = formatMessage(record);
		if (extractStacktrace) {
			final Throwable thrown = record.getThrown();
			if (null != thrown) {
				final StringWriter sw = new StringWriter();
				thrown.printStackTrace(new PrintWriter(sw));
				message += "\n\r" + sw.toString();
			}
		}
		return message != null ? message : "";
	}

	/**
	 * Overriden to support percent parameters
	 */
	@Override
	public String formatMessage(LogRecord record) {
		String format = record.getMessage();
		java.util.ResourceBundle catalog = record.getResourceBundle();
		if (catalog != null) {
			try {
				format = catalog.getString(record.getMessage());
			} catch (java.util.MissingResourceException ex) {
				format = record.getMessage();
			}
		}
		try {
			Object parameters[] = record.getParameters();
			if (parameters == null || parameters.length == 0) {
				return format;
			}
			if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0
					|| format.indexOf("{3") >= 0) {
				return java.text.MessageFormat.format(format, parameters);
			} else {
				return String.format(format, parameters);
			}
		} catch (Exception exception) {
			return format;
		}
	}
}
