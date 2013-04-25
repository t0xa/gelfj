package org.graylog2;

import java.io.IOException;
import java.net.*;

public class GelfTCPSender implements GelfSender {
	private boolean shutdown = false;
	private InetAddress host;
	private int port;
	private Socket socket;

    public GelfTCPSender() {
    }

	public GelfTCPSender(String host, int port) throws IOException {
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.socket = new Socket(host, port);
	}

	public boolean sendMessage(GelfMessage message) {
		if (shutdown || !message.isValid()) {
			return false;
		}

		try {
			// reconnect if necessary
			if (socket == null) {
				socket = new Socket(host, port);
			}

			socket.getOutputStream().write(message.toBuffer().array());

			return true;
		} catch (IOException e) {
			// if an error occours, signal failure
			socket = null;
			return false;
		}
	}

	public void close() {
		shutdown = true;
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
