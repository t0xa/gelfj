package org.graylog2.log;

import org.apache.log4j.spi.LoggingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * anton 11:00 2011:04:10
 */
public class Log4jVersionChecker {

    private final LoggingEvent event;

    public Log4jVersionChecker(LoggingEvent event) {
        this.event = event;
    }

    public long getTimeStamp() {
        Method[] declaredMethods = event.getClass().getDeclaredMethods();
        for(Method m : declaredMethods) {
            if (m.getName().equals("getTimeStamp")) {
                try {
                    m.invoke(event);
                } catch (IllegalAccessException e) {
                    return 0;
                } catch (InvocationTargetException e) {
                    return 0;
                }
            }
        }
        return System.currentTimeMillis();
    }
}
