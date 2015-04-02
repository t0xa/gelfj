package com.github.pukkaone.gelf.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import com.github.pukkaone.gelf.protocol.GelfMessage;
import java.util.Map;
import org.slf4j.Marker;

public class GelfMessageFactory {

    private PatternLayout shortPatternLayout;
    private PatternLayout fullPatternLayout;

    public GelfMessageFactory() {
        // Short message contains event message and no stack trace.
        shortPatternLayout = new PatternLayout();
        shortPatternLayout.setContext(new LoggerContext());
        shortPatternLayout.setPattern("%m%nopex");
        shortPatternLayout.start();

        // Full message contains stack trace.
        fullPatternLayout = new PatternLayout();
        fullPatternLayout.setContext(new LoggerContext());
        fullPatternLayout.setPattern("%xEx");
        fullPatternLayout.start();
    }

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
                    message.addField(entry.getKey(), entry.getValue());
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
            message.addField(entry.getKey(), entry.getValue());
        }

        return message;
    }
}
