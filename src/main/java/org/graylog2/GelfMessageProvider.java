package org.graylog2;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Map;

public interface GelfMessageProvider {
    public boolean isExtractStacktrace();
    public String getOriginHost();
    public String getFacility();
    public Map<String, String> getFields();
    public boolean isAddExtendedInformation();
}
