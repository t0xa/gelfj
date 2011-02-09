What is GELFJ
------------

It's very simple GELF implementation in pure Java with the log4j appender. It supports chunked messages which allows you to send large log messages (stacktraces, environment variables, additional fields, etc.) to [Graylog2 Server](http://www.graylog2.org/).

How to use GELFJ
----------------

Grab latest JAR from the [downloads section](https://github.com/t0xa/gelfj/downloads) and drop it into your classpath.

Examples
--------

To send a GELF message:
   
    GelfMessage message = new GelfMessage("Short message", "Long message", new Date().getTime()/1000L, "1");
    GelfSender gelfSender = new GelfSender("localhost", 12201);
    if (message.isValid()) {
       gelfSender.sendMessage(message)
    }

To send a GELF message with additional fields:
   
    GelfMessage message = new GelfMessage("Short message", "Long message", new Date().getTime()/1000L, "1");
    Map<String,Object> additonalFields = message.getAdditonalFields();
    additonalFields.put("cat", "LOLCAT");
    additonalFields.put("quote", "Can I haz cheezburger?");
    GelfSender gelfSender = new GelfSender("localhost", 12201);
    if (message.isValid()) {
       gelfSender.sendMessage(message)
    }

Log4j appender
--------------

GELF log for4j appender will use log message as a short message and a stacktrace (if exception available) as a long message.

To use GELF Facility as appender in Log4J (XML format):

    <appender name="graylog" class="org.graylog2.log.GelfAppender">
        <param name="graylogHost" value="192.168.0.201"/>
        <param name="Threshold" value="INFO"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d |%t|%c{2}| %-5p - %m%n"/>
        </layout>
    </appender>

and then add it as a one of appenders:

    <root>
        <priority value="INFO"/>
        <appender-ref ref="graylog"/>
    </root>    

  
What is GELF
------------

The Graylog Extended Log Format (GELF) avoids the shortcomings of classic plain syslog:

- Limited to length of 1024 byte
- Not much space for payloads like backtraces
- Unstructured. You can only build a long message string and define priority, severity etc.

You can get more information http://www.graylog2.org/about/gelf