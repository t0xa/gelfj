package org.graylog2.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;
import org.graylog2.GelfMessageProvider;
import org.graylog2.GelfSender;
import org.json.simple.JSONValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 */
public class GelfAppender extends AppenderSkeleton implements GelfMessageProvider {

    private String graylogHost;
    private static String originHost;
    private int graylogPort = 12201;
    private String facility;
    private GelfSender gelfSender;
    private boolean extractStacktrace;
    private boolean addExtendedInformation;
    private Map<String, String> fields;

    public GelfAppender() {
        super();
    }

    public void setAdditionalFields(String additionalFields) {
        fields = (Map<String, String>) JSONValue.parse(additionalFields.replaceAll("'", "\""));
    }

    public int getGraylogPort() {
        return graylogPort;
    }

    public void setGraylogPort(int graylogPort) {
        this.graylogPort = graylogPort;
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public void setGraylogHost(String graylogHost) {
        this.graylogHost = graylogHost;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public boolean isExtractStacktrace() {
        return extractStacktrace;
    }

    public void setExtractStacktrace(boolean extractStacktrace) {
        this.extractStacktrace = extractStacktrace;
    }

    public String getOriginHost() {
        return originHost;
    }

    public static void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public boolean isAddExtendedInformation() {
        return addExtendedInformation;
    }

    public void setAddExtendedInformation(boolean addExtendedInformation) {
        this.addExtendedInformation = addExtendedInformation;
    }
    
    public Map<String, String> getFields() {
        if (fields == null) {
            fields = new HashMap<String, String>();
        }
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public void activateOptions() {
        try {
            gelfSender = new GelfSender(graylogHost, graylogPort);
        } catch (UnknownHostException e) {
            errorHandler.error("Unknown Graylog2 hostname:" + getGraylogHost(), e, ErrorCode.WRITE_FAILURE);
        } catch (SocketException e) {
            errorHandler.error("Socket exception", e, ErrorCode.WRITE_FAILURE);
        }
    }

    @Override
    protected void append(LoggingEvent event) {
        GelfMessage gelfMessage = GelfMessageFactory.makeMessage(event, this);

        if(getGelfSender() == null || !getGelfSender().sendMessage(gelfMessage)) {
            errorHandler.error("Could not send GELF message");
        }
    }

    public GelfSender getGelfSender() {
        return gelfSender;
    }

    public void close() {
        getGelfSender().close();
    }

    public boolean requiresLayout() {
        return false;
    }
}
