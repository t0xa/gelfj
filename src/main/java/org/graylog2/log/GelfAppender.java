package org.graylog2.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;
import org.graylog2.GelfMessageProvider;
import org.graylog2.GelfSender;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.graylog2.*;

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
    private boolean includeLocation = true;
    private Map<String, String> fields;

    public GelfAppender() {
        super();
    }

    @SuppressWarnings("unchecked")
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
        if (originHost == null) {
            originHost = getLocalHostName();
        }
        return originHost;
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

    public void setOriginHost(String originHost) {
        GelfAppender.originHost = originHost;
    }

    public boolean isAddExtendedInformation() {
        return addExtendedInformation;
    }

    public void setAddExtendedInformation(boolean addExtendedInformation) {
        this.addExtendedInformation = addExtendedInformation;
    }

    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    public void setIncludeLocation(boolean includeLocation) {
        this.includeLocation = includeLocation;
    }

    public Map<String, String> getFields() {
        if (fields == null) {
            fields = new HashMap<String, String>();
        }
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public void activateOptions() {
		if (graylogHost == null) {
			errorHandler.error("Graylog2 hostname is empty!", null, ErrorCode.WRITE_FAILURE);
		} else {
			try {
				if (graylogHost.startsWith("tcp:")) {
					String tcpGraylogHost = graylogHost.substring(4);
					gelfSender = getGelfTCPSender(tcpGraylogHost, graylogPort);
				} else if (graylogHost.startsWith("udp:")) {
					String udpGraylogHost = graylogHost.substring(4);
					gelfSender = getGelfUDPSender(udpGraylogHost, graylogPort);
				} else {
					gelfSender = getGelfUDPSender(graylogHost, graylogPort);
				}
			} catch (UnknownHostException e) {
				errorHandler.error("Unknown Graylog2 hostname:" + getGraylogHost(), e, ErrorCode.WRITE_FAILURE);
			} catch (SocketException e) {
				errorHandler.error("Socket exception", e, ErrorCode.WRITE_FAILURE);
			} catch (IOException e) {
				errorHandler.error("IO exception", e, ErrorCode.WRITE_FAILURE);
			}
		}
    }

    protected GelfUDPSender getGelfUDPSender(String udpGraylogHost, int graylogPort) throws IOException {
        return new GelfUDPSender(udpGraylogHost, graylogPort);
    }

    protected GelfTCPSender getGelfTCPSender(String tcpGraylogHost, int graylogPort) throws IOException {
        return new GelfTCPSender(tcpGraylogHost, graylogPort);
    }

    @Override
    protected void append(LoggingEvent event) {
        GelfMessage gelfMessage = GelfMessageFactory.makeMessage(layout, event, this);

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
        return true;
    }
}
