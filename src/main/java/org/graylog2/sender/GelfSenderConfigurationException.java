package org.graylog2.sender;

public class GelfSenderConfigurationException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public GelfSenderConfigurationException(String message) {
		super(message);
	}

	public GelfSenderConfigurationException(String message, Exception cause) {
		super(message, cause);
	}

	public Exception getCauseException() {
		return (Exception) super.getCause();
	}
}
