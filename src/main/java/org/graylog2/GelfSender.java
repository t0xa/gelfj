package org.graylog2;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class GelfSender {

    private static final int DEFAULT_PORT = 12201;

    private static final int PORT_MIN = 8000;
    private static final int PORT_MAX = 8888;

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
        int port = PORT_MIN;

        DatagramSocket resultingSocket = null;
        boolean binded = false;
        while (!binded) {
            try {
                resultingSocket = new DatagramSocket(port);
                binded = true;
            } catch (SocketException e) {
                port++;

                if (port > PORT_MAX)
                    throw e;
            }
        }
        return resultingSocket;
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
