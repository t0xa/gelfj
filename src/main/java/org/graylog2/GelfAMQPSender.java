package org.graylog2;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

public class GelfAMQPSender implements GelfSender {
    private boolean shutdown = false;

    private ConnectionFactory factory;
    private Channel channel;

    private String exchangeName;
    private String routingKey;
    private int maxRetries;

    public GelfAMQPSender(String host, String exchangeName, String routingKey, int maxRetries) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        factory = new ConnectionFactory();
        factory.setUri(host);

        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.maxRetries = maxRetries;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown || !message.isValid()) {
            return false;
        }

        // set unique id to identify duplicates after connection failure
        String uuid = UUID.randomUUID().toString();
        String messageid = "gelf" + message.getHost() + message.getFacility() + message.getTimestamp() + uuid;

        int tries = 0;
        do {
            try {
                // establish the connection the first time
                if (channel == null) {
                    Connection connection = factory.newConnection();
                    channel = connection.createChannel();
                }

                BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder();
                propertiesBuilder.contentType("application/json; charset=utf-8");
                propertiesBuilder.contentEncoding("gzip");
                propertiesBuilder.messageId(messageid);
                propertiesBuilder.timestamp(new Date(message.getJavaTimestamp()));
                BasicProperties properties = propertiesBuilder.build();

                channel.basicPublish(exchangeName, routingKey, properties, message.toAMQPBuffer().array());
                channel.waitForConfirms();

                return true;
            } catch (Exception e) {
                channel = null;
                tries++;
            }
        } while (tries <= maxRetries || maxRetries < 0);

        return false;
    }

    public void close() {
        shutdown = true;
        try {
            channel.close();
        } catch (Exception e) {
        }
    }
}
