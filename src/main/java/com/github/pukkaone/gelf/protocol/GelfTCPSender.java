package com.github.pukkaone.gelf.protocol;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GelfTCPSender extends GelfSender {

    private boolean shutdown;
    private InetAddress host;
    private int port;
    private Socket socket;
    private OutputStreamWriter writer;

    private OutputStreamWriter getWriter() throws IOException {
        if (socket == null) {
            socket = getSocket();
            writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        }
        return writer;
    }

    protected Socket getSocket() throws IOException {
        return new Socket(host, port);
    }

    public GelfTCPSender(String host, int port) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown || !message.isValid()) {
            return false;
        }

        try {
            // Do not use GZIP, as the headers will contain \0 bytes.
            // graylog2-server uses \0 as a delimiter for TCP frames.
            // See: https://github.com/Graylog2/graylog2-server/issues/127
            OutputStreamWriter writer = getWriter();
            writer.write(message.toJson());
            writer.write('\0');
            writer.flush();
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
        } finally {
            socket = null;
        }
    }
}
