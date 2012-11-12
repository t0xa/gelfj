package org.graylog2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class GelfSender {

    public static final int DEFAULT_PORT = 12201;

    private InetAddress host;
    private int port;
	private DatagramChannel channel;

    public GelfSender(String host) throws IOException {
        this(host, DEFAULT_PORT);
    }

    public GelfSender(String host, int port) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.channel = initiateChannel();
    }

    private DatagramChannel initiateChannel() throws IOException {
        DatagramChannel resultingChannel = DatagramChannel.open();
        resultingChannel.socket().bind(new InetSocketAddress(0));
        resultingChannel.connect(new InetSocketAddress(this.host, this.port));
        resultingChannel.configureBlocking(false);

        return resultingChannel;
    }

    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && sendDatagrams(message.toBuffers());
    }

	public boolean sendDatagrams(ByteBuffer[] bytesList) {
            try {
				for (ByteBuffer buffer : bytesList) {
                    channel.write(buffer);
                }
            } catch (IOException e) {
                return false;
            }

        return true;
    }

    public void close() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
