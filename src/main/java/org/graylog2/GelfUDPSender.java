package org.graylog2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;

public class GelfUDPSender implements GelfSender {

	private String host;
	private int port;
	private DatagramChannel channel;
	private Date lastChannelRefresh = null;

	private static final int MAX_RETRIES = 5;
	private static final int REFRESH_CHANNEL_SECONDS = 30;

    public GelfUDPSender() {
    }

    public GelfUDPSender(String host) throws IOException {
		this(host, DEFAULT_PORT);
	}

	public GelfUDPSender(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		setChannel(initiateChannel());
	}

	private DatagramChannel initiateChannel() throws IOException {
		DatagramChannel resultingChannel = DatagramChannel.open();
		resultingChannel.socket().bind(new InetSocketAddress(0));
		resultingChannel.connect(new InetSocketAddress(this.host, this.port));
		resultingChannel.configureBlocking(false);

		return resultingChannel;
	}

	public GelfSenderResult sendMessage(GelfMessage message) {
		if (!message.isValid()) return GelfSenderResult.MESSAGE_NOT_VALID;
		return sendDatagrams(message.toUDPBuffers());
	}

	private GelfSenderResult sendDatagrams(ByteBuffer[] bytesList) {

		int tries = 0;
		Exception lastException = null;
		do {

			try {

				if (isChannelOld() ) {
					getChannel().close();
				}
				
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public DatagramChannel getChannel() {
        return channel;
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
        this.lastChannelRefresh = new Date();
    }
    
    private boolean isChannelOld() {
    	if (this.lastChannelRefresh == null)
    		return true;
    	
    	Date now = new Date();
    	if ((now.getTime() - REFRESH_CHANNEL_SECONDS * 1000) > this.lastChannelRefresh.getTime())
    		return true;
    	
    	return false;
    }
}
