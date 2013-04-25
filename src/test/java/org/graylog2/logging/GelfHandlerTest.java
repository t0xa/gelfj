package org.graylog2.logging;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * @author lkmikkel
 */
public class GelfHandlerTest
{
    private TestGelfHandlerSender gelfSender;

    @Before
    public void setUp() throws IOException {
        gelfSender = new TestGelfHandlerSender();

        InputStream is = GelfHandlerTest.class.getResourceAsStream("logging-test.properties");
        LogManager.getLogManager().readConfiguration(is);
    }

    @Test
    public void handleNullMessage() {
        Logger myLogger = Logger.getLogger("testNullMessage");
        myLogger.log(Level.FINE, null);

        assertThat( "Message short message", gelfSender.getLastMessage().getShortMessage(), notNullValue() );
        assertThat( "Message full message", gelfSender.getLastMessage().getFullMessage(), notNullValue() );
    }

    @Test
    public void handleAdditionalField() {
        Logger myLogger = Logger.getLogger("testAdditionalField");
        myLogger.log( Level.FINE, "test additional field" );

        assertEquals("bar", gelfSender.getLastMessage().getAdditonalFields().get("foo"));
        assertNull(gelfSender.getLastMessage().getAdditonalFields().get("non-existent"));
    }


    @Test
    public void handleStackTraces() {
        Logger myLogger = Logger.getLogger("testStackTraces");
        myLogger.log( Level.FINE, "test stacktrace:", new RuntimeException("test") );

        Pattern regex = Pattern.compile("^.*java\\.lang\\.RuntimeException: test.*at org\\.graylog2\\.logging\\.GelfHandlerTest\\.handleStackTraces.*$", Pattern.MULTILINE | Pattern.DOTALL);
        assertTrue(regex.matcher(gelfSender.getLastMessage().getFullMessage()).matches());
    }

    @Test
    public void testLogFormattingWithParameter() {
        Logger myLogger = Logger.getLogger("testLogFormattingWithParameter");
        myLogger.log( Level.FINE, "logging param: {0}", "param1");

        assertEquals( gelfSender.getLastMessage().getFullMessage(), "logging param: param1" );
    }

    @Test
    public void testLogFormattingWithParameters() {
        Logger myLogger = Logger.getLogger("testLogFormattingWithParameters");
        myLogger.log( Level.FINE, "logging params: {0} {1}", new Object[] {new Integer(1), "param2"});

        assertEquals( gelfSender.getLastMessage().getFullMessage(), "logging params: 1 param2" );
    }

    @Test
    public void testLogFormattingWithPercentParameters() {
        Logger myLogger = Logger.getLogger("testLogFormattingWithPercentParameters");
        myLogger.log( Level.FINE, "logging percent params: %d %s", new Object[] {new Integer(1), "param2"});

        assertEquals( gelfSender.getLastMessage().getFullMessage(), "logging percent params: 1 param2" );
    }

    @Test
    public void testLogFormattingWithPercentParameters_InvalidParameters() {
        Logger myLogger = Logger.getLogger("testLogFormattingWithPercentParameters_InvalidParameters");
        myLogger.log( Level.FINE, "logging percent params: %d %d", new Object[] {new Integer(1), "param2"});

        assertEquals( gelfSender.getLastMessage().getFullMessage(), "logging percent params: %d %d" );
    }

    @Test
    public void testNullLogWithParameters() {
        Logger myLogger = Logger.getLogger("testNullLogWithParameters");
        myLogger.log( Level.FINE, null, new Object[] {new Integer(1), "param2"});

        assertEquals( gelfSender.getLastMessage().getFullMessage(), "" );
    }

}
