package org.graylog2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class GelfSender {

    private static final int DEFAULT_PORT = 12201;


    private InetAddress host;
    private int port;
	private DatagramChannel channel;


    public GelfSender(String host) throws IOException, SocketException {
        this(host, DEFAULT_PORT);
    }

    public GelfSender(String host, int port) throws IOException, SocketException {
        this.host = InetAddress.getByName(host);
        this.port = port;
		this.channel = initiateChannel();
    }

	private DatagramChannel initiateChannel() throws IOException
	{

		DatagramChannel resultingChannel = DatagramChannel.open();
		resultingChannel.socket().bind(new InetSocketAddress(0));
		resultingChannel.connect(new InetSocketAddress(this.host, this.port));
		resultingChannel.configureBlocking(false);

		return resultingChannel;
    }

    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && sendDatagrams(message.toDatagrams());
    }

	public boolean sendDatagrams(ByteBuffer[] bytesList) {
            try {
				channel.write( bytesList);
            } catch (IOException e) {
                return false;
            }

        return true;
    }

    public void close() {
		try
		{
			channel.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
    }
}
