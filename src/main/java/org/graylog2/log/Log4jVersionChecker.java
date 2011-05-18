package org.graylog2.log;

import org.apache.log4j.spi.LoggingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * anton 11:00 2011:04:10
 */
public class Log4jVersionChecker {

    private static boolean hasGetTimeStamp = true;
    private static Method methodGetTimeStamp = null;

    public static long getTimeStamp(LoggingEvent event) {

        long timeStamp = System.currentTimeMillis();

        if(hasGetTimeStamp && methodGetTimeStamp == null) {

            hasGetTimeStamp = false;

            Method[] declaredMethods = event.getClass().getDeclaredMethods();
            for(Method m : declaredMethods) {
                if (m.getName().equals("getTimeStamp")) {
                    methodGetTimeStamp = m;
                    hasGetTimeStamp = true;

                    break;
                }
            }
        }

        if(hasGetTimeStamp) {

            try {
                timeStamp = (Long) methodGetTimeStamp.invoke(event);
            } catch (IllegalAccessException e) {
                // Just return the current timestamp
            } catch (InvocationTargetException e) {
                // Just return the current timestamp
            }
        }

        return timeStamp;
    }
}
