package org.graylog2.log;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketException;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * (c) Copyright: Anton Yakimov
 *
 *
 */
public class GelfAppenderTest {

    private TestGelfSender gelfSender;
    private GelfAppender gelfAppender;

    @Before
    public void setUp() throws UnknownHostException, SocketException {
        gelfSender = new TestGelfSender("localhost");

        gelfAppender = new GelfAppender() {

            @Override
            public GelfSender getGelfSender() {
                return gelfSender;
            }

            @Override
            public void append(LoggingEvent event) {
                super.append(event);
            }
        };
    }

    @Test
    public void ensureHostnameForMessage() {

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, "Das Auto", new RuntimeException("LOL"));
        gelfAppender.append(event);

        assertThat("Message short message", gelfSender.getLastMessage().getShortMessage(), notNullValue());
        assertThat("Message full message", gelfSender.getLastMessage().getFullMessage(), notNullValue());
    }

    @Test
    public void handleNullInAppend() throws UnknownHostException, SocketException {

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, null, new RuntimeException("LOL"));
        gelfAppender.append(event);

        assertThat("Message hostname", gelfSender.getLastMessage().getHost(), notNullValue());

        gelfAppender.setOriginHost("example.com");
        gelfAppender.append(event);
        assertThat(gelfSender.getLastMessage().getHost(), is("example.com"));
    }

    private class TestGelfSender extends GelfSender {

        private GelfMessage lastMessage;

        public TestGelfSender(String host) throws UnknownHostException, SocketException {
            super(host);
        }

        @Override
        public boolean sendMessage(GelfMessage message) {
            this.lastMessage = message;
            return true;
        }

        public GelfMessage getLastMessage() {
            return lastMessage;
        }
    }

}
