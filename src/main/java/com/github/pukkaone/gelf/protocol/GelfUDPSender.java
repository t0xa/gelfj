package com.github.pukkaone.gelf.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class GelfUDPSender extends GelfSender {

    private static final int MAXIMUM_CHUNK_SIZE = 1420;
    private static final int MAXIMUM_CHUNKS = 128;
    private static final int HEADER_SIZE = 12;
    private static final int MAXIMUM_DATAGRAM_SIZE =
            HEADER_SIZE + MAXIMUM_CHUNK_SIZE;
    private static final byte[] CHUNKED_ID = new byte[] { 0x1e, 0x0f };

    private byte[] hostBytes;
    private InetAddress host;
    private int port;
    private DatagramChannel channel;

    private byte[] hashHostName(String host) {
        return ByteBuffer.allocate(4).putInt(host.hashCode()).array();
    }

    private DatagramChannel initiateChannel() throws IOException {
        DatagramChannel resultingChannel = DatagramChannel.open();
        resultingChannel.socket().bind(new InetSocketAddress(0));
        resultingChannel.connect(new InetSocketAddress(this.host, this.port));
        resultingChannel.configureBlocking(false);
        return resultingChannel;
    }

    public GelfUDPSender(String host, int port) throws IOException {
        this.hostBytes = hashHostName(host);
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.channel = initiateChannel();
    }

    private void sliceDatagrams(byte[] messageBytes, ByteBuffer[] datagrams) {
        byte[] messageId = ByteBuffer.allocate(8)
            .putInt((int) System.currentTimeMillis())
            .put(hostBytes)
            .array();

        int fromOffset = 0;
        for (int idx = 0; idx < datagrams.length; ++idx) {
            int fromLength =
                    (fromOffset + MAXIMUM_CHUNK_SIZE > messageBytes.length)
                    ? messageBytes.length - fromOffset
                    : MAXIMUM_CHUNK_SIZE;

            ByteBuffer datagram = ByteBuffer.allocate(MAXIMUM_DATAGRAM_SIZE)
                .put(CHUNKED_ID)
                .put(messageId)
                .put((byte) idx)
                .put((byte) datagrams.length)
                .put(messageBytes, fromOffset, fromLength);
            datagram.flip();
            datagrams[idx] = datagram;

            fromOffset += MAXIMUM_CHUNK_SIZE;
        }
    }

    private ByteBuffer[] toUDPBuffers(String json) {
        byte[] messageBytes = gzipMessage(json);

        int chunksCount = messageBytes.length / MAXIMUM_CHUNK_SIZE;
        if (messageBytes.length % MAXIMUM_CHUNK_SIZE != 0) {
            ++chunksCount;
        }
        if (chunksCount > MAXIMUM_CHUNKS) {
            chunksCount = MAXIMUM_CHUNKS;
        }

        ByteBuffer[] datagrams = new ByteBuffer[chunksCount];
        if (messageBytes.length > MAXIMUM_CHUNK_SIZE) {
            sliceDatagrams(messageBytes, datagrams);
        } else {
            datagrams[0] = ByteBuffer.allocate(messageBytes.length);
            datagrams[0].put(messageBytes);
            datagrams[0].flip();
        }
        return datagrams;
    }

    private boolean sendDatagrams(ByteBuffer[] buffers) {
        try {
            for (ByteBuffer buffer : buffers) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && sendDatagrams(toUDPBuffers(message.toJson()));
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
