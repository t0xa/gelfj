package org.graylog2.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ErrorManager;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSender;
import org.graylog2.sender.GelfSenderConfigurationException;
import org.graylog2.sender.GelfSenderFactory;
import org.graylog2.sender.GelfSenderResult;
import org.json.simple.JSONValue;

/**
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 */
public class GelfAppender extends AppenderSkeleton implements GelfMessageProvider {
	private Log4jGelfSenderConfiguration senderConfiguration;
    private static String originHost;
    private String facility;
    private GelfSender gelfSender;
    private boolean extractStacktrace;
    private boolean addExtendedInformation;
    private boolean includeLocation = true;
    private Map<String, String> fields;

    public GelfAppender() {
        super();
    	senderConfiguration = new Log4jGelfSenderConfiguration();
    }

    @SuppressWarnings("unchecked")
    public void setAdditionalFields(String additionalFields) {
        fields = (Map<String, String>) JSONValue.parse(additionalFields.replaceAll("'", "\""));
    }

    public String getGraylogHost() {
        return senderConfiguration.getGraylogHost();
    }

    public void setGraylogHost(String graylogHost) {
        senderConfiguration.setGraylogHost(graylogHost);
    }
    
    public int getGraylogPort() {
        return senderConfiguration.getGraylogPort();
    }

    public void setGraylogPort(int graylogPort) {
    	senderConfiguration.setGraylogPort(graylogPort);
    }
    
    public int getSocketSendBufferSize() {
		return senderConfiguration.getSocketSendBufferSize();
	}
    
    public void setSocketSendBufferSize(int socketSendBufferSize) {
    	senderConfiguration.setSocketSendBufferSize(socketSendBufferSize);
	}

    public String getAmqpURI() {
        return senderConfiguration.getAmqpURI();
    }

    public void setAmqpURI(String amqpURI) {
    	senderConfiguration.setAmqpURI(amqpURI);
    }

    public String getAmqpExchangeName() {
        return senderConfiguration.getAmqpExchangeName();
    }

    public void setAmqpExchangeName(String amqpExchangeName) {
    	senderConfiguration.setAmqpExchangeName(amqpExchangeName);
    }

    public String getAmqpRoutingKey() {
        return senderConfiguration.getAmqpRoutingKey();
    }

    public void setAmqpRoutingKey(String amqpRoutingKey) {
    	senderConfiguration.setAmqpRoutingKey(amqpRoutingKey);
    }

    public int getAmqpMaxRetries() {
        return senderConfiguration.getAmqpMaxRetries();
    }

    public void setAmqpMaxRetries(int amqpMaxRetries) {
    	senderConfiguration.setAmqpMaxRetries(amqpMaxRetries);
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
    
    public Object transformExtendedField(String field, Object object) {
        if (object != null)
            return object.toString();
        return null;
    }

    @Override
    public void activateOptions() {
		try {
			if (gelfSender != null) {
				gelfSender.close();
			}
			gelfSender = GelfSenderFactory.getInstance().createSender(senderConfiguration);
		} catch (GelfSenderConfigurationException exception) {
			errorHandler.error(exception.getMessage(), exception.getCauseException(), ErrorManager.WRITE_FAILURE);
		} catch (Exception exception) {
			errorHandler.error("Could not activate GELF appender", exception, ErrorManager.WRITE_FAILURE);
		}    	
    }

    @Override
    protected void append(LoggingEvent event) {
        GelfMessage gelfMessage = GelfMessageFactory.makeMessage(layout, event, this);
        if(getGelfSender() == null) {
            errorHandler.error("Could not send GELF message. Gelf Sender is not initialised and equals null");
        } else {
            GelfSenderResult gelfSenderResult = getGelfSender().sendMessage(gelfMessage);
            if (!GelfSenderResult.OK.equals(gelfSenderResult)) {
                errorHandler.error("Error during sending GELF message. Error code: " + gelfSenderResult.getCode() + ".", gelfSenderResult.getException(), ErrorCode.WRITE_FAILURE);
            }
        }
    }

    public GelfSender getGelfSender() {
        return gelfSender;
    }

    public void close() {
        GelfSender x = this.getGelfSender();
        if (x != null) {
            x.close();
        }
    }

    public boolean requiresLayout() {
        return true;
    }
}
