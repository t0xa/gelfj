package org.graylog2.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * anton @ 11.30.1 17:28
 */
public class GelfAppender extends AppenderSkeleton {

    private String graylogHost;
    private int graylogPort = 12201;
    private GelfSender gelfSender;
    private boolean extractStacktrace;

    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;

    public int getGraylogPort() {
        return graylogPort;
    }

    public void setGraylogPort(int graylogPort) {
        this.graylogPort = graylogPort;
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public void setGraylogHost(String graylogHost) {
        this.graylogHost = graylogHost;
    }

    public boolean isExtractStacktrace() {
        return extractStacktrace;
    }

    public void setExtractStacktrace(boolean extractStacktrace) {
        this.extractStacktrace = extractStacktrace;
    }

    @Override
    public void activateOptions() {
        try {
            gelfSender = new GelfSender(graylogHost, graylogPort);
        } catch (UnknownHostException e) {
            errorHandler.error("Unknown Graylog2 hostname:" + getGraylogHost(), e, ErrorCode.WRITE_FAILURE);
        } catch (SocketException e) {
            errorHandler.error("Socket exception", e, ErrorCode.WRITE_FAILURE);
        }
    }

    @Override
    protected void append(LoggingEvent event) {
        Level level = event.getLevel();
        long timeStamp = event.getTimeStamp();

        LocationInfo locationInformation = event.getLocationInformation();
        String file = locationInformation.getFileName();
        String lineNumber = locationInformation.getLineNumber();

        String renderedMessage = event.getRenderedMessage();
        String shortMessage = "";
        if (renderedMessage.length() > MAX_SHORT_MESSAGE_LENGTH) {
            shortMessage = renderedMessage.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        } else {
            shortMessage = renderedMessage;
        }

        if (extractStacktrace) {
            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                renderedMessage += "\n\r" + extractStacktrace(throwableInformation);
            }
        }
        GelfMessage gelfMessage = new GelfMessage(shortMessage, renderedMessage, timeStamp, level.getSyslogEquivalent() + "", lineNumber, file);
        gelfSender.sendMessage(gelfMessage);
    }

    private String extractStacktrace(ThrowableInformation throwableInformation) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwableInformation.getThrowable().printStackTrace(pw);
        return sw.toString();
    }

    public void close() {
        gelfSender.close();
    }

    public boolean requiresLayout() {
        return false;
    }
}
