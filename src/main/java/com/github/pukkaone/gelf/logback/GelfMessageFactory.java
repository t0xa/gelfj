package com.github.pukkaone.gelf.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.pukkaone.gelf.protocol.GelfMessage;

/**
 * Converts log event to GELF message.
 */
public interface GelfMessageFactory {

    /**
     * Converts log event to GELF message.
     *
     * @param appender
     *     GELF appender
     * @param event
     *     log event
     * @return GELF message
     */
    GelfMessage createMessage(GelfAppender appender, ILoggingEvent event);
}
