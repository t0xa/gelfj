GELFJ - A GELF Appender for Log4j
=====================================

What is GELFJ
-------------

It's very simple GELF implementation in pure Java with the Log4j appender. It supports chunked messages which allows you to send large log messages (stacktraces, environment variables, additional fields, etc.) to a [Graylog2](http://www.graylog2.org/) server.

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
    message.addField("specialVariable", "LOLCAT").addField("anotherValue", "typos in my closet");
    GelfSender gelfSender = new GelfSender("localhost");
    if (message.isValid()) {
       gelfSender.sendMessage(message)
    }

Log4j appender
--------------

GelfAppender will use the log message as a short message and a stacktrace (if exception available) as a long message if "extractStacktrace" is true.

To use GELF Facility as appender in Log4j (XML configuration format):

    <appender name="graylog2" class="org.graylog2.log.GelfAppender">
        <param name="graylogHost" value="192.168.0.201"/>
        <param name="originHost" value="my.machine.example.com"/>
        <param name="extractStacktrace" value="true"/>
        <param name="useDiagnosticContext" value="true"/>
        <param name="facility" value="gelf-java"/>
        <param name="Threshold" value="INFO"/>
    </appender>

and then add it as a one of appenders:

    <root>
        <priority value="INFO"/>
        <appender-ref ref="graylog2"/>
    </root>

Or, in the log4j.properties format:

    # Define the graylog2 destination
    log4j.appender.graylog2=org.graylog2.log.GelfAppender
    log4j.appender.graylog2.graylogHost=graylog2.example.com
    log4j.appender.graylog2.originHost=my.machine.example.com
    log4j.appender.graylog2.facility=gelf-java
    log4j.appender.graylog2.layout=org.apache.log4j.PatternLayout
    log4j.appender.graylog2.extractStacktrace=true
    log4j.appender.graylog2.useDiagnosticContext=true

    # Send all INFO logs to graylog2
    log4j.rootLogger=INFO, graylog2


Options
-------

GelfAppender supports the following options:

- **graylogHost**: Graylog2 server where it will send the GELF messages
- **graylogPort**: Port on which the Graylog2 server is listening; default 12201 (*optional*)
- **originHost**: Name of the originating host; defaults to the local hostname (*optional*)
- **extractStacktrace** (true/false): Add stacktraces to the GELF message; default false (*optional*)
- **useDiagnosticContext** (true/false): Use additional information from Log4j's NDC/MDC; default false (*optional*)
- **facility**: Facility which to use in the GELF message; default "gelf-java"

What is GELF
------------

The Graylog Extended Log Format (GELF) avoids the shortcomings of classic plain syslog:

- Limited to length of 1024 byte
- Not much space for payloads like stacktraces
- Unstructured. You can only build a long message string and define priority, severity etc.

You can get more information here: [http://www.graylog2.org/about/gelf](http://www.graylog2.org/about/gelf)