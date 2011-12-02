package org.graylog2.log;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;
import org.graylog2.GelfMessageProvider;
import org.json.simple.JSONValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configure it this way:
 * 
 * log4j.appender.console.additionalFields={'environment': 'DEV', 'application': 'MyAPP'}
 * log4j.appender.console.extractStacktrace=true
 * log4j.appender.console.addExtendedInformation=true
 * log4j.appender.console.originHost=www.example.com
 * log4j.appender.console=org.graylog2.log.GelfConsoleAppender
 * log4j.appender.console.layout=org.apache.log4j.PatternLayout
 * 
 */
public class GelfConsoleAppender extends ConsoleAppender implements GelfMessageProvider{
    
    private static String originHost;
    private boolean extractStacktrace;
    private boolean addExtendedInformation;
    private Map<String, String> fields;
    
    // parent overrides.
    
    public GelfConsoleAppender() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GelfConsoleAppender(Layout layout) {
        super(layout);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GelfConsoleAppender(Layout layout, String target) {
        super(layout, target);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    // GelfMessageProvider interface.
    
    public void setAdditionalFields(String additionalFields) {
        fields = (Map<String, String>) JSONValue.parse(additionalFields.replaceAll("'", "\""));
    }
    
    public boolean isExtractStacktrace() {
        return extractStacktrace;
    }

    public void setExtractStacktrace(boolean extractStacktrace) {
        this.extractStacktrace = extractStacktrace;
    }
    
    public boolean isAddExtendedInformation() {
        return addExtendedInformation;
    }

    public void setAddExtendedInformation(boolean addExtendedInformation) {
        this.addExtendedInformation = addExtendedInformation;
    }
    
    public static String getOriginHost() {
        return originHost;
    }

    public static void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public String getFacility() {
        return null;
    }

    public Map<String, String> getFields() {
        if (fields == null) {
            fields = new HashMap<String, String>();
        }
        return Collections.unmodifiableMap(fields);
    }

    // the important parts.
    
    @Override
    protected void subAppend(LoggingEvent event) {
        GelfMessage gelf = GelfMessageFactory.makeMessage(event, this);
        this.qw.write(gelf.toJson());
        this.qw.write(Layout.LINE_SEP);

        if (this.immediateFlush) {
            this.qw.flush();
        }
    }
}
