package org.graylog2;

public interface GelfSender {
	public static final int DEFAULT_PORT = 12201;

	public boolean sendMessage(GelfMessage message);
	public void close();
}
