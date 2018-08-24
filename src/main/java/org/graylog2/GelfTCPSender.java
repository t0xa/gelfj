package org.graylog2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class GelfTCPSender implements GelfSender {
	private boolean shutdown = false;
	private InetAddress host;
	private int port;
	private boolean keepalive;
	private Socket socket;
    private OutputStream os;

    public GelfTCPSender() {
    }

	public GelfTCPSender(String host, int port, boolean keepalive) throws IOException {
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.keepalive = keepalive;
		initConnection();
	}
	
	private void initConnection() throws IOException {
		this.socket = createSocket();
        this.os = socket.getOutputStream();
	}

	private Socket createSocket() throws UnknownHostException, IOException {
		Socket socket = new Socket(host, port);
		socket.setKeepAlive(keepalive);
		return socket;
	}

	public GelfSenderResult sendMessage(GelfMessage message) {
		if (shutdown || !message.isValid()) {
			return GelfSenderResult.MESSAGE_NOT_VALID_OR_SHUTTING_DOWN;
		}

		try {
			// reconnect if necessary
			if (!isConnectionInitialized()) {
				initConnection();
			}
            os.write(message.toTCPBuffer().array());

			return GelfSenderResult.OK;
		} catch (IOException e) {
			// if an error occours, signal failure
			socket = null;
			return new GelfSenderResult(GelfSenderResult.ERROR_CODE, e);
		}
	}

	private boolean isConnectionInitialized() {
		return socket != null && os != null;
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
