package org.graylog2;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.graylog2.log.Log4jVersionChecker;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GelfMessageFactory {
    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;
    
    public static final GelfMessage makeMessage(LoggingEvent event, boolean extractStackTrace) {
        long timeStamp = Log4jVersionChecker.getTimeStamp(event);
        Level level = event.getLevel();

        LocationInfo locationInformation = event.getLocationInformation();
        String file = locationInformation.getFileName();
        String lineNumber = locationInformation.getLineNumber();

        String renderedMessage = event.getRenderedMessage();
        String shortMessage;

        if (renderedMessage == null) {
            renderedMessage = "";
        }

        if (renderedMessage.length() > MAX_SHORT_MESSAGE_LENGTH) {
            shortMessage = renderedMessage.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        }
        else {
            shortMessage = renderedMessage;
        }

        if (extractStackTrace) {
            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                renderedMessage += "\n\r" + extractStacktrace(throwableInformation);
            }
        }

        return new GelfMessage(shortMessage, renderedMessage, timeStamp,
                               String.valueOf(level.getSyslogEquivalent()), lineNumber, file);
    }
    
    private static String extractStacktrace(ThrowableInformation throwableInformation) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwableInformation.getThrowable().printStackTrace(pw);
        return sw.toString();
    }
}
