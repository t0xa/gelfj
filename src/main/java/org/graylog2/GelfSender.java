package org.graylog2;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class GelfSender {

    private InetAddress host;
    private int port;
    private DatagramSocket socket;

    public GelfSender(String host) throws UnknownHostException, SocketException {
        this(host, 12201);
    }

    public GelfSender(String host, int port) throws UnknownHostException, SocketException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.socket = initiateSocket();
    }

    private DatagramSocket initiateSocket() throws SocketException {
        int from = 8000;
        int to = 8888;
        DatagramSocket resultingSocket = null;
        boolean binded = false;
        while (!binded) {
            try {
                resultingSocket = new DatagramSocket(from);
                binded = true;
            } catch (SocketException e) {
                from++;
                if (from > to)
                    throw e;
            }
        }
        return resultingSocket;
    }

    public boolean sendMessage(GelfMessage message) {
        if (!message.isValid()) return false;
        return sendDatagrams(message.toDatagrams());
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
