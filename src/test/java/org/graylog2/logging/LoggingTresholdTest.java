package org.graylog2.logging;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingTresholdTest {

    @Test
    public void testLogFormattingWithParameter() {
        Logger myLogger = Logger.getLogger("org.graylog2.TestLogger");
        myLogger.log(Level.FINE, "Testing FINE message");
        myLogger.log(Level.INFO, "Testing INFO message");
    }

}
