package org.graylog2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class GelfSender {

    private static final int DEFAULT_PORT = 12201;

    private InetAddress host;
    private int port;
    private DatagramSocket socket;

    public GelfSender(String host) throws UnknownHostException, SocketException {
        this(host, DEFAULT_PORT);
    }

    public GelfSender(String host, int port) throws UnknownHostException, SocketException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.socket = initiateSocket();
    }

    private DatagramSocket initiateSocket() throws SocketException {
        return new DatagramSocket(0);
    }

    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && sendDatagrams(message.toDatagrams());
    }

    public boolean sendDatagrams(List<byte[]> bytesList) {
        for (byte[] bytes : bytesList) {
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, host, port);
            try {
                socket.send(datagramPacket);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public void close() {
        socket.close();
    }
}
