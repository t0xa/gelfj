package com.github.pukkaone.gelf.protocol;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class GelfAMQPSender extends GelfSender {

    private volatile boolean shutdown = false;

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private String exchangeName;
    private String routingKey;
    private int maxRetries;

    public GelfAMQPSender(
            String host, String exchangeName, String routingKey, int maxRetries)
        throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        factory = new ConnectionFactory();
        factory.setUri(host);

        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.maxRetries = maxRetries;
    }

    private synchronized Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            connection = factory.newConnection();
        }
        return connection;
    }

    private synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                // Ignore exception closing connection.
            }
            connection = null;
        }
    }

    private synchronized Channel getChannel() throws IOException, TimeoutException {
        if (channel == null) {
            channel = getConnection().createChannel();
            channel.confirmSelect();
        }
        return channel;
    }

    private synchronized void closeChannel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                // Ignore exception closing channel.
            }
            channel = null;
        }
    }

    private ByteBuffer toAMQPBuffer(String json) {
        byte[] messageBytes = gzipMessage(json);
        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown || !message.isValid()) {
            return false;
        }

        // set unique id to identify duplicates after connection failure
        String messageId = "gelf" + message.getTimestampMillis() + UUID.randomUUID();

        int tries = 0;
        do {
            try {
                Channel channel = getChannel();

                BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder();
                propertiesBuilder.contentType("application/json; charset=utf-8");
                propertiesBuilder.contentEncoding("gzip");
                propertiesBuilder.messageId(messageId);
                propertiesBuilder.timestamp(new Date(message.getTimestampMillis()));
                BasicProperties properties = propertiesBuilder.build();

                channel.basicPublish(
                    exchangeName,
                    routingKey,
                    properties,
                    toAMQPBuffer(message.toJson()).array());
                channel.waitForConfirms();

                return true;
            } catch (InterruptedException | IOException | TimeoutException e) {
                closeChannel();
                closeConnection();
                tries++;
            }
        } while (tries <= maxRetries || maxRetries < 0);

        return false;
    }

    public void close() {
        shutdown = true;
        closeChannel();
        closeConnection();
    }
}
