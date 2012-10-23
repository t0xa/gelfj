package org.graylog2.logging;

import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;

import java.io.IOException;

/**
 * @author lkmikkel
 */
public class TestGelfHandlerSender extends GelfSender
{
    private static GelfMessage lastMessage;

    public TestGelfHandlerSender() throws IOException
    {
        super("localhost");
    }

    @Override
    public boolean sendMessage(GelfMessage message) {
        this.lastMessage = message;
        return true;
    }

    public GelfMessage getLastMessage() {
        return this.lastMessage;
    }
}
