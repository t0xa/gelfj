package org.graylog2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.*;

public class GelfTCPSender implements GelfSender {
	private boolean shutdown = false;
	private InetAddress host;
	private int port;
	private Socket socket;
    private OutputStream os;

    public GelfTCPSender() {
    }

	public GelfTCPSender(String host, int port) throws IOException {
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.socket = new Socket(host, port);
        this.os = socket.getOutputStream();
	}

	public boolean sendMessage(GelfMessage message) {
		if (shutdown || !message.isValid()) {
			return false;
		}

		try {
			// reconnect if necessary
			if (socket == null || os == null) {
				socket = new Socket(host, port);
                os = socket.getOutputStream();
			}

            os.write(message.toTCPBuffer().array());

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
            if (os != null){
                os.close();
            }
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
