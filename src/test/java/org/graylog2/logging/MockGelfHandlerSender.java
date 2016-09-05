package org.graylog2.logging;

import java.io.IOException;

import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSender;
import org.graylog2.sender.GelfSenderResult;

/**
 * @author lkmikkel
 */
public class MockGelfHandlerSender implements GelfSender {
    private GelfMessage lastMessage;

    public MockGelfHandlerSender() throws IOException {
    }

    public GelfSenderResult sendMessage(GelfMessage message) {
        lastMessage = message;
        return GelfSenderResult.OK;
    }
    
    public void close() {
    }

    public GelfMessage getLastMessage() {
        return lastMessage;
    }
}
