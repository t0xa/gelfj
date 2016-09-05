package org.graylog2.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

import org.graylog2.message.GelfMessage;

public class GelfTCPSender implements GelfSender {
	private boolean shutdown = false;
	private String host;
	private int port;
	private int sendBufferSize;
	private Socket socket;
	private OutputStream os;

	public GelfTCPSender(String host, int port, int sendBufferSize) throws IOException {
		this.host = host;
		this.port = port;
		this.sendBufferSize = sendBufferSize;
	}

	public GelfSenderResult sendMessage(GelfMessage message) {
		if (shutdown || !message.isValid()) {
			return GelfSenderResult.MESSAGE_NOT_VALID_OR_SHUTTING_DOWN;
		}
		try {
			if (!isConnected()) {
				connect();
			}
			os.write(message.toTCPBuffer().array());
			return GelfSenderResult.OK;
		} catch (IOException e) {
			closeConnection();
			return new GelfSenderResult(GelfSenderResult.ERROR_CODE, e);
		}
	}

	private void connect() throws UnknownHostException, IOException, SocketException {
		socket = new Socket(host, port);
		socket.setSendBufferSize(sendBufferSize);
		os = socket.getOutputStream();
	}

	private boolean isConnected() {
		return socket != null && os != null;
	}

	private void closeConnection() {
		try {
			os.close();
		} catch (Exception ignoredException) {
		} finally {
			os = null;
		}
		try {
			socket.close();
		} catch (Exception ignoredException) {
		} finally {
			socket = null;
		}
	}

	public void close() {
		shutdown = true;
		closeConnection();
	}
}
