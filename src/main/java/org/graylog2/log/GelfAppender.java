package org.graylog2.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;
import org.json.simple.JSONValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 */
public class GelfAppender extends AppenderSkeleton {

    private String graylogHost;
    private String originHost;
    private int graylogPort = 12201;
    private String facility;
    private GelfSender gelfSender;
    private boolean extractStacktrace;
    private boolean addExtendedInformation;
    private Map<String, String> fields;

    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;
    private static final String ORIGIN_HOST_KEY = "originHost";
    private static final String LOGGER_NAME = "logger";
    private static final String LOGGER_NDC = "loggerNdc";
    private static final String JAVA_TIMESTAMP = "timestampMs";

    public GelfAppender() {
        super();
        this.originHost = getLocalHostName();
    }

    private String getLocalHostName() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            errorHandler.error("Unknown local hostname", e, ErrorCode.GENERIC_FAILURE);
        }

        return hostName;
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

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public boolean isAddExtendedInformation() {
        return addExtendedInformation;
    }

    public void setAddExtendedInformation(boolean addExtendedInformation) {
        this.addExtendedInformation = addExtendedInformation;
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

        long timeStamp = getTimestamp(event);

        Level level = event.getLevel();

        LocationInfo locationInformation = event.getLocationInformation();
        String file = locationInformation.getFileName();
        String lineNumber = locationInformation.getLineNumber();

        String renderedMessage = event.getRenderedMessage();
        String shortMessage;

        if(renderedMessage == null) {
            renderedMessage = "";
        }

        if (renderedMessage.length() > MAX_SHORT_MESSAGE_LENGTH) {
            shortMessage = renderedMessage.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        } else {
            shortMessage = renderedMessage;
        }

        if (isExtractStacktrace()) {
            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                renderedMessage += "\n\r" + extractStacktrace(throwableInformation);
            }
        }

        GelfMessage gelfMessage = new GelfMessage(shortMessage, renderedMessage, timeStamp,
                String.valueOf(level.getSyslogEquivalent()), lineNumber, file);

        if (getOriginHost() != null) {
            gelfMessage.setHost(getOriginHost());
        }

        if (getFacility() != null) {
            gelfMessage.setFacility(getFacility());
        }

        if (fields != null && !fields.isEmpty()) {

            if (fields.containsKey(ORIGIN_HOST_KEY) && gelfMessage.getHost() == null) {
                gelfMessage.setHost(fields.get(ORIGIN_HOST_KEY));
                fields.remove(ORIGIN_HOST_KEY);
            }

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                gelfMessage.addField(entry.getKey(), entry.getValue());
            }
        }

        if (isAddExtendedInformation()) {

            gelfMessage.addField(LOGGER_NAME, event.getLoggerName());
            gelfMessage.addField(JAVA_TIMESTAMP, Long.toString(timeStamp));

            // Get MDC and add a GELF field for each key/value pair
            Map<String, Object> mdc = MDC.getContext();

            if(mdc != null) {
                for(Map.Entry<String, Object> entry : mdc.entrySet()) {

                    gelfMessage.addField(entry.getKey(), entry.getValue().toString());
                }
            }

            // Get NDC and add a GELF field
            String ndc = event.getNDC();

            if(ndc != null) {

                gelfMessage.addField(LOGGER_NDC, ndc);
            }
        }

        if(getGelfSender() == null || !getGelfSender().sendMessage(gelfMessage)) {
            errorHandler.error("Could not send GELF message");
        }
    }

    public GelfSender getGelfSender() {
        return gelfSender;
    }

    private long getTimestamp(LoggingEvent event) {
        return Log4jVersionChecker.getTimeStamp(event);
    }

    private String extractStacktrace(ThrowableInformation throwableInformation) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwableInformation.getThrowable().printStackTrace(pw);
        return sw.toString();
    }

    public void close() {
        getGelfSender().close();
    }

    public boolean requiresLayout() {
        return false;
    }
}
