package org.graylog2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class GelfUDPSender implements GelfSender {
	private String host;
	private int port;
	private int sendBufferSize;
	private DatagramChannel channel;

	private static final int MAX_RETRIES = 5;

	public GelfUDPSender(String host, int port, int sendBufferSize) throws IOException {
		this.host = host;
		this.port = port;
		this.sendBufferSize = sendBufferSize;
		setChannel(initiateChannel());
	}

	private DatagramChannel initiateChannel() throws IOException {
		DatagramChannel resultingChannel = DatagramChannel.open();
		DatagramSocket socket = resultingChannel.socket();
		socket.bind(new InetSocketAddress(0));
		if (sendBufferSize > 0) {
			socket.setSendBufferSize(sendBufferSize);
		}
		socket.connect(new InetSocketAddress(this.host, this.port));
		return resultingChannel;
	}

	public GelfSenderResult sendMessage(GelfMessage message) {
		if (!message.isValid()) {
			return GelfSenderResult.MESSAGE_NOT_VALID;
		}
		return sendDatagrams(message.toUDPBuffers());
	}

	private GelfSenderResult sendDatagrams(ByteBuffer[] bytesList) {
		int tries = 0;
		Exception lastException = null;
		do {
			try {
				if (!getChannel().isOpen()) {
					setChannel(initiateChannel());
				}
				for (ByteBuffer buffer : bytesList) {
					getChannel().write(buffer);
				}
				return GelfSenderResult.OK;
			} catch (IOException e) {
				tries++;
				lastException = e;
			}
		} while (tries <= MAX_RETRIES);

		return new GelfSenderResult(GelfSenderResult.ERROR_CODE, lastException);
	}

	public void close() {
		try {
			getChannel().close();
		} catch (IOException ignoredException) {
		}
	}

	public DatagramChannel getChannel() {
		return channel;
	}

	public void setChannel(DatagramChannel channel) {
		this.channel = channel;
	}
}
