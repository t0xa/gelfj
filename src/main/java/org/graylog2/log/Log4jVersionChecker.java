package org.graylog2.log;

import org.apache.log4j.spi.LoggingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 */
public class Log4jVersionChecker {

    private static Method methodGetTimeStamp = null;

    static {
        Method[] declaredMethods = LoggingEvent.class.getDeclaredMethods();
        for(Method m : declaredMethods) {
            if (m.getName().equals("getTimeStamp")) {
                methodGetTimeStamp = m;
                break;
            }
        }
    }

    public static long getTimeStamp(LoggingEvent event) {

        long timeStamp = 0;
        if(methodGetTimeStamp != null) {

            try {
                timeStamp = (Long) methodGetTimeStamp.invoke(event);
            } catch (IllegalAccessException e) {
                // Just return the current timestamp
            } catch (InvocationTargetException e) {
                // Just return the current timestamp
            }
        }

        return timeStamp == 0 ? System.currentTimeMillis() : timeStamp;
    }
}
