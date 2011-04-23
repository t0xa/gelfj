What is GELFJ
------------

It's very simple GELF implementation in pure Java with the log4j appender. It supports chunked messages which allows you to send large log messages (stacktraces, environment variables, additional fields, etc.) to [Graylog2 Server](http://www.graylog2.org/).

How to use GELFJ
----------------

Grab latest JAR from the [downloads section](https://github.com/t0xa/gelfj/downloads) and drop it into your classpath.

Examples
--------

To send a GELF message:
   
    GelfMessage message = new GelfMessage("Short message", "Long message", new Date(), "1");
    GelfSender gelfSender = new GelfSender("localhost");
    if (message.isValid()) {
       gelfSender.sendMessage(message)
    }

To send a GELF message with additional fields:
   
    GelfMessage message = new GelfMessage("Short message", "Long message", new Date(), "1");
    message.addField("id", "LOLCAT").addField("_id", "typos in my closet");
    GelfSender gelfSender = new GelfSender("localhost");
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

Or, in the log4j.properties format:

    # Define the graylog2 destination
    log4j.appender.graylog2=org.graylog2.log.GelfAppender
    log4j.appender.graylog2.graylogHost=graylog2.example.com
    log4j.appender.graylog2.layout=org.apache.log4j.PatternLayout
    log4j.appender.graylog2.layout.ConversionPattern=%d |%t|%c{2}| %-5p - %m%n

    # Send all INFO logs to graylog2
    log4j.rootLogger=INFO, graylog2

  
What is GELF
------------

The Graylog Extended Log Format (GELF) avoids the shortcomings of classic plain syslog:

- Limited to length of 1024 byte
- Not much space for payloads like backtraces
- Unstructured. You can only build a long message string and define priority, severity etc.

You can get more information here: [http://www.graylog2.org/about/gelf](http://www.graylog2.org/about/gelf)