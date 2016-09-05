package org.graylog2.sender;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

import org.graylog2.message.GelfMessage;

public class GelfUDPSender implements GelfSender {
	private static final byte[] GELF_CHUNKED_ID = new byte[] { 0x1e, 0x0f };
	private static final int MAXIMUM_CHUNK_SIZE = 1420;

	private String host;
	private int port;
	private int sendBufferSize;
	private UDPBufferBuilder bufferBuilder;
	private DatagramChannel channel;

	private static final int MAX_RETRIES = 5;

	public GelfUDPSender(String host, int port, int sendBufferSize) throws IOException {
		this.host = host;
		this.port = port;
		this.sendBufferSize = sendBufferSize;
		this.bufferBuilder = new UDPBufferBuilder();
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
		return sendDatagrams(bufferBuilder.toUDPBuffers(message.toJson()));
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

	public static class UDPBufferBuilder extends BufferBuilder {
		public ByteBuffer[] toUDPBuffers(String message) {
			byte[] messageBytes = gzipMessage(message);
			// calculate the length of the datagrams array
			int diagrams_length = messageBytes.length / MAXIMUM_CHUNK_SIZE;
			// In case of a remainder, due to the integer division, add a extra
			// datagram
			if (messageBytes.length % MAXIMUM_CHUNK_SIZE != 0) {
				diagrams_length++;
			}
			ByteBuffer[] datagrams = new ByteBuffer[diagrams_length];
			if (messageBytes.length > MAXIMUM_CHUNK_SIZE) {
				sliceDatagrams(messageBytes, datagrams);
			} else {
				datagrams[0] = ByteBuffer.allocate(messageBytes.length);
				datagrams[0].put(messageBytes);
				datagrams[0].flip();
			}
			return datagrams;
		}

		private void sliceDatagrams(byte[] messageBytes, ByteBuffer[] datagrams) {
			int messageLength = messageBytes.length;
			byte[] messageId = new byte[8];
			new Random().nextBytes(messageId);

			// Reuse length of datagrams array since this is supposed to be the
			// correct number of datagrams
			int num = datagrams.length;
			for (int idx = 0; idx < num; idx++) {
				byte[] header = concatByteArray(GELF_CHUNKED_ID,
						concatByteArray(messageId, new byte[] { (byte) idx, (byte) num }));
				int from = idx * MAXIMUM_CHUNK_SIZE;
				int to = from + MAXIMUM_CHUNK_SIZE;
				if (to >= messageLength) {
					to = messageLength;
				}

				byte[] range = new byte[to - from];
				System.arraycopy(messageBytes, from, range, 0, range.length);

				byte[] datagram = concatByteArray(header, range);
				datagrams[idx] = ByteBuffer.allocate(datagram.length);
				datagrams[idx].put(datagram);
				datagrams[idx].flip();
			}
		}

		private byte[] concatByteArray(byte[] first, byte[] second) {
			byte[] result = new byte[first.length + second.length];
			System.arraycopy(first, 0, result, 0, first.length);
			System.arraycopy(second, 0, result, first.length, second.length);
			return result;
		}
	}
}
