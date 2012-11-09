package org.graylog2;

public interface GelfSender {
	public boolean sendMessage(GelfMessage message);
	public void close();
}
