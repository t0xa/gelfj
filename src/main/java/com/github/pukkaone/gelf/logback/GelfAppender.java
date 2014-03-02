package com.github.pukkaone.gelf.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.pukkaone.gelf.protocol.GelfAMQPSender;
import com.github.pukkaone.gelf.protocol.GelfMessage;
import com.github.pukkaone.gelf.protocol.GelfSender;
import com.github.pukkaone.gelf.protocol.GelfTCPSender;
import com.github.pukkaone.gelf.protocol.GelfUDPSender;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Appender which sends events to GELF server.
 */
public class GelfAppender extends AppenderBase<ILoggingEvent> {

    private String graylogHost;
    private int graylogPort = GelfSender.DEFAULT_PORT;
    private String originHost;
    private boolean levelIncluded = true;
    private boolean locationIncluded;
    private boolean loggerIncluded = true;
    private boolean markerIncluded;
    private boolean mdcIncluded;
    private boolean threadIncluded;
    private Map<String, String> additionalFields = new HashMap<>();
    private String amqpURI;
    private String amqpExchange;
    private String amqpRoutingKey;
    private int amqpMaxRetries;
    private GelfMessageFactory marshaller = new GelfMessageFactory();
    private GelfSender gelfSender;

    public String getGraylogHost() {
        return graylogHost;
    }

    public void setGraylogHost(String graylogHost) {
        this.graylogHost = graylogHost;
    }

    public int getGraylogPort() {
        return graylogPort;
    }

    public void setGraylogPort(int graylogPort) {
        this.graylogPort = graylogPort;
    }

    private String getLocalHostName() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            addError("Unknown local hostname", e);
        }

        return hostName;
    }

    public String getOriginHost() {
        if (originHost == null) {
            originHost = getLocalHostName();
        }
        return originHost;
    }

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public boolean isLevelIncluded() {
        return levelIncluded;
    }

    public void setLevelIncluded(boolean levelIncluded) {
        this.levelIncluded = levelIncluded;
    }

    public boolean isLocationIncluded() {
        return this.locationIncluded;
    }

    public void setLocationIncluded(boolean locationIncluded) {
        this.locationIncluded = locationIncluded;
    }

    public boolean isLoggerIncluded() {
        return loggerIncluded;
    }

    public void setLoggerIncluded(boolean loggerIncluded) {
        this.loggerIncluded = loggerIncluded;
    }

    public boolean isMarkerIncluded() {
        return markerIncluded;
    }

    public void setMarkerIncluded(boolean markerIncluded) {
        this.markerIncluded = markerIncluded;
    }

    public boolean isMdcIncluded() {
        return mdcIncluded;
    }

    public void setMdcIncluded(boolean mdcIncluded) {
        this.mdcIncluded = mdcIncluded;
    }

    public boolean isThreadIncluded() {
        return threadIncluded;
    }

    public void setThreadIncluded(boolean threadIncluded) {
        this.threadIncluded = threadIncluded;
    }

    public Map<String, String> getAdditionalFields() {
        return additionalFields;
    }

    public void addAdditionalField(String keyValue) {
        String[] parts = keyValue.split("=", 2);
        if (parts.length != 2) {
            addError(String.format(
                "additionalField must be in the format key=value, but found [%s]",
                keyValue));
            return;
        }
         additionalFields.put(parts[0], parts[1]);
    }

    public String getAmqpURI() {
        return amqpURI;
    }

    public void setAmqpURI(String amqpURI) {
        this.amqpURI = amqpURI;
    }

    public String getAmqpExchange() {
        return amqpExchange;
    }

    public void setAmqpExchange(String amqpExchange) {
        this.amqpExchange = amqpExchange;
    }

    public String getAmqpRoutingKey() {
        return amqpRoutingKey;
    }

    public void setAmqpRoutingKey(String amqpRoutingKey) {
        this.amqpRoutingKey = amqpRoutingKey;
    }

    public int getAmqpMaxRetries() {
        return amqpMaxRetries;
    }

    public void setAmqpMaxRetries(int amqpMaxRetries) {
        this.amqpMaxRetries = amqpMaxRetries;
    }

    private GelfUDPSender getGelfUDPSender(String graylogHost, int graylogPort)
        throws IOException
    {
        return new GelfUDPSender(graylogHost, graylogPort);
    }

    private GelfTCPSender getGelfTCPSender(String graylogHost, int graylogPort)
        throws IOException
    {
        return new GelfTCPSender(graylogHost, graylogPort);
    }

    private GelfAMQPSender getGelfAMQPSender(
            String amqpURI,
            String amqpExchange,
            String amqpRoutingKey,
            int amqpMaxRetries)
        throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        return new GelfAMQPSender(amqpURI, amqpExchange, amqpRoutingKey, amqpMaxRetries);
    }

    @Override
    public void start() {
        if (graylogHost == null && amqpURI == null) {
            addError("Graylog2 hostname and AMQP URI are empty!");
            return;
        }
        if (graylogHost != null && amqpURI != null) {
            addError("Graylog2 hostname and AMQP URI are both set!");
            return;
        }

        try {
            if (graylogHost != null && graylogHost.startsWith("tcp:")) {
                String tcpGraylogHost = graylogHost.substring(4);
                gelfSender = getGelfTCPSender(tcpGraylogHost, graylogPort);
            } else if (graylogHost != null && graylogHost.startsWith("udp:")) {
                String udpGraylogHost = graylogHost.substring(4);
                gelfSender = getGelfUDPSender(udpGraylogHost, graylogPort);
            } else if (amqpURI != null) {
                gelfSender = getGelfAMQPSender(amqpURI, amqpExchange, amqpRoutingKey, amqpMaxRetries);
            } else {
                gelfSender = getGelfUDPSender(graylogHost, graylogPort);
            }
        } catch (UnknownHostException e) {
            addError(String.format("Unknown Graylog2 hostname [%s]", getGraylogHost()), e);
            return;
        } catch (SocketException e) {
            addError("Socket exception", e);
            return;
        } catch (IOException e) {
            addError("IO exception", e);
            return;
        } catch (URISyntaxException e) {
            addError(String.format("Invalid AMQP URI [%s]", getAmqpURI()), e);
            return;
        } catch (NoSuchAlgorithmException e) {
            addError("AMQP algorithm exception", e);
            return;
        } catch (KeyManagementException e) {
            addError("AMQP key exception", e);
            return;
        }

        super.start();
    }

    protected GelfSender getGelfSender() {
        return gelfSender;
    }

    @Override
    protected void append(ILoggingEvent event) {
        GelfMessage message = marshaller.createMessage(this, event);

        if (getGelfSender() == null || !getGelfSender().sendMessage(message)) {
            addError("Could not send GELF message");
        }
    }

    @Override
    public void stop() {
        gelfSender.close();
        super.stop();
    }
}
