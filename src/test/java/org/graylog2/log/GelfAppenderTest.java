package org.graylog2.log;

import org.apache.log4j.Category;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
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
import static org.junit.Assert.*;

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

        assertThat("Message hostname", gelfSender.getLastMessage().getHost(), notNullValue());

        gelfAppender.setOriginHost("example.com");
        gelfAppender.append(event);
        assertThat(gelfSender.getLastMessage().getHost(), is("example.com"));
    }

    @Test
    public void handleNullInAppend() {

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, null, new RuntimeException("LOL"));
        gelfAppender.append(event);

        assertThat("Message short message", gelfSender.getLastMessage().getShortMessage(), notNullValue());
        assertThat("Message full message", gelfSender.getLastMessage().getFullMessage(), notNullValue());
    }

    @Test
    public void handleMDC() {

        gelfAppender.setUseDiagnosticContext(true);

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, "", new RuntimeException("LOL"));
        MDC.put("foo", "bar");

        gelfAppender.append(event);

        assertEquals("bar", gelfSender.getLastMessage().getAdditonalFields().get("foo"));
        assertNull(gelfSender.getLastMessage().getAdditonalFields().get("non-existent"));
    }

    @Test
    public void handleNDC() {

        gelfAppender.setUseDiagnosticContext(true);

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, "", new RuntimeException("LOL"));
        NDC.push("Foobar");

        gelfAppender.append(event);

        assertEquals("Foobar", gelfSender.getLastMessage().getAdditonalFields().get("context"));
    }

    @Test
    public void disableDiagnosticContext() {

        gelfAppender.setUseDiagnosticContext(false);

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, "", new RuntimeException("LOL"));

        MDC.put("foo", "bar");
        NDC.push("Foobar");

        gelfAppender.append(event);

        assertNull(gelfSender.getLastMessage().getAdditonalFields().get("context"));
        assertNull(gelfSender.getLastMessage().getAdditonalFields().get("foo"));
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
