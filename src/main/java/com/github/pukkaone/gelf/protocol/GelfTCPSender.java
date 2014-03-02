package com.github.pukkaone.gelf.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GelfTCPSender extends GelfSender {

    private boolean shutdown;
    private InetAddress host;
    private int port;
    private Socket socket;

    private void connect() throws IOException {
        socket = new Socket(host, port);
    }

    public GelfTCPSender(String host, int port) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        connect();
    }

    private ByteBuffer toTCPBuffer(String json) {
        // Do not use GZIP, as the headers will contain \0 bytes.
        // graylog2-server uses \0 as a delimiter for TCP frames
        // see: https://github.com/Graylog2/graylog2-server/issues/127
        byte[] messageBytes = json.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length + 1);
        buffer.put(messageBytes).put((byte) 0);
        buffer.flip();
        return buffer;
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

            socket.getOutputStream().write(toTCPBuffer(message.toJson()).array());
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
            // Ignore exception closing socket.
        }
    }
}
