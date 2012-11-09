package org.graylog2.logging;

import org.graylog2.GelfMessage;
import org.graylog2.GelfUDPSender;

import java.io.IOException;

/**
 * @author lkmikkel
 */
public class TestGelfHandlerSender extends GelfUDPSender
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
