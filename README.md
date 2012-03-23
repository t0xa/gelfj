GELFJ - A GELF Appender for Log4j and a GELF Handler for JDK Logging
====================================================================

### WARNING: Latest version of this library is not compatible with graylog2-server < 0.9.6. 

What is GELFJ
-------------

It's very simple GELF implementation in pure Java with the Log4j appender and JDK Logging Handler. It supports chunked messages which allows you to send large log messages (stacktraces, environment variables, additional fields, etc.) to a [Graylog2](http://www.graylog2.org/) server.

How to use GELFJ
----------------

Drop the latest JAR into your classpath and configure Log4j to use it.

Log4j appender
--------------

GelfAppender will use the log message as a short message and a stacktrace (if exception available) as a long message if "extractStacktrace" is true.

To use GELF Facility as appender in Log4j (XML configuration format):

    <appender name="graylog2" class="org.graylog2.log.GelfAppender">
        <param name="graylogHost" value="192.168.0.201"/>
        <param name="originHost" value="my.machine.example.com"/>
        <param name="extractStacktrace" value="true"/>
        <param name="addExtendedInformation" value="true"/>
        <param name="facility" value="gelf-java"/>
        <param name="Threshold" value="INFO"/>
        <param name="additionalFields" value="{'environment': 'DEV', 'application': 'MyAPP'}"/>
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
    log4j.appender.graylog2.addExtendedInformation=true
    log4j.appender.graylog2.additionalFields={'environment': 'DEV', 'application': 'MyAPP'}

    # Send all INFO logs to graylog2
    log4j.rootLogger=INFO, graylog2


Options
-------

GelfAppender supports the following options:

- **graylogHost**: Graylog2 server where it will send the GELF messages
- **graylogPort**: Port on which the Graylog2 server is listening; default 12201 (*optional*)
- **originHost**: Name of the originating host; defaults to the local hostname (*optional*)
- **extractStacktrace** (true/false): Add stacktraces to the GELF message; default false (*optional*)
- **addExtendedInformation** (true/false): Add extended information like Log4j's NDC/MDC; default false (*optional*)
- **facility**: Facility which to use in the GELF message; default "gelf-java"

Logging Handler
---------------

Configured via properties as a standard Handler like

  handlers = org.graylog2.logging.GelfHandler

  .level = ALL

  org.graylog2.logging.GelfHandler.level = ALL
  org.graylog2.logging.GelfHandler.graylogHost = syslog.example.com
  #org.graylog2.logging.GelfHandler.graylogPort = 12201
  #org.graylog2.logging.GelfHandler.extractStacktrace = true
  #org.graylog2.logging.GelfHandler.additionalField.0 = foo=bah
  #org.graylog2.logging.GelfHandler.additionalField.1 = foo2=bah2
  #org.graylog2.logging.GelfHandler.facility = local0

  .handlers=org.graylog2.logging.GelfHandler

What is GELF
------------

The Graylog Extended Log Format (GELF) avoids the shortcomings of classic plain syslog:

- Limited to length of 1024 byte
- Not much space for payloads like stacktraces
- Unstructured. You can only build a long message string and define priority, severity etc.

You can get more information here: [http://www.graylog2.org/about/gelf](http://www.graylog2.org/about/gelf)