package com.github.pukkaone.gelf.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import com.github.pukkaone.gelf.protocol.GelfMessage;
import org.slf4j.Marker;
import java.util.Map;

/**
 * Default message factory implementation.
 */
public class DefaultGelfMessageFactory implements GelfMessageFactory {

    private static final String DEFAULT_SHORT_MESSAGE_PATTERN = "%m%nopex";
    private static final String DEFAULT_FULL_MESSAGE_PATTERN = "%xEx";

    private PatternLayout shortPatternLayout;
    private PatternLayout fullPatternLayout;

    public DefaultGelfMessageFactory() {
        // Short message contains event message and no stack trace.
        shortPatternLayout = new PatternLayout();
        shortPatternLayout.setContext(new LoggerContext());
        shortPatternLayout.setPattern(DEFAULT_SHORT_MESSAGE_PATTERN);
        shortPatternLayout.start();

        // Full message contains stack trace.
        fullPatternLayout = new PatternLayout();
        fullPatternLayout.setContext(new LoggerContext());
        fullPatternLayout.setPattern(DEFAULT_FULL_MESSAGE_PATTERN);
        fullPatternLayout.start();
    }

    @Override
    public GelfMessage createMessage(GelfAppender appender, ILoggingEvent event) {
        GelfMessage message = new GelfMessage()
                .setTimestampMillis(event.getTimeStamp());

        String originHost = appender.getOriginHost();
        if (originHost != null) {
            message.setHost(originHost);
        }

        if (appender.isLevelIncluded()) {
            message.setLevel(LevelToSyslogSeverity.convert(event));
        }

        if (appender.isLocationIncluded()) {
            StackTraceElement locationInformation = event.getCallerData()[0];
            message.setFile(locationInformation.getFileName());
            message.setLine(locationInformation.getLineNumber());
        }

        if (appender.isLoggerIncluded()) {
            message.setLogger(event.getLoggerName());
        }

        if (appender.isMarkerIncluded()) {
            Marker marker = event.getMarker();
            if (marker != null) {
                message.setMarker(marker.getName());
            }
        }

        if (appender.isMdcIncluded()) {
            Map<String, String> mdc = event.getMDCPropertyMap();
            if (mdc != null) {
                for (Map.Entry<String, String> entry : mdc.entrySet()) {
                    addMessageField(mdc, message, entry.getKey(), entry.getValue());
                }
            }
        }

        if (appender.isThreadIncluded()) {
            message.setThread(event.getThreadName());
        }

        message.setShortMessage(shortPatternLayout.doLayout(event));

        String fullMessage = fullPatternLayout.doLayout(event);
        if (!fullMessage.isEmpty()) {
            message.setFullMessage(fullMessage);
        }

        message.setFacility(appender.getFacility());

        Map<String, String> fields = appender.getAdditionalFields();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            addMessageField(fields, message, entry.getKey(), entry.getValue());
        }

        return message;
    }

    protected void addMessageField(Map<String, String> map, GelfMessage message, String key, Object value) {
        message.addField(key, value);
    }

    private void setMessagePattern(PatternLayout patternLayout, String messagePattern) {
        patternLayout.stop();
        patternLayout.setPattern(messagePattern);
        patternLayout.start();
    }

    public void setShortMessagePattern(String shortMessagePattern) {
        setMessagePattern(shortPatternLayout, shortMessagePattern);
    }

    public void setFullMessagePattern(String fullMessagePattern) {
        setMessagePattern(fullPatternLayout, fullMessagePattern);
    }
}
