package com.github.pukkaone.gelf.protocol;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class GelfSSLSender extends GelfTCPSender {

    private final SSLSocketFactory socketFactory;

    public GelfSSLSender(String host, int port, boolean trustAllCertificates) throws IOException {
        super(host, port);
        try {
            this.socketFactory = initSocketFactory(trustAllCertificates);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Could not find an SSL provider. Sending GELF logs won't work.", e);
        }
    }

    private SSLSocketFactory initSocketFactory(boolean trustAllCertificates)
            throws KeyManagementException, NoSuchAlgorithmException
    {
        if (trustAllCertificates) {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, getAllTrustingTrustManagers(), new SecureRandom());
            return context.getSocketFactory();
        } else {
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
    }

    private TrustManager[] getAllTrustingTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    @Override
    protected Socket getSocket() throws IOException {
        Socket tcpSocket = super.getSocket();
        return socketFactory.createSocket(
                tcpSocket,
                tcpSocket.getInetAddress().getHostAddress(),
                tcpSocket.getPort(),
                true);
    }
}
