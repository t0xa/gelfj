package com.github.pukkaone.gelf.logback;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.pukkaone.gelf.protocol.GelfMessage;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultGelfMessageFactoryTest {

    private static final String HOST = "host";
    private static final String MDC_KEY = "mdcKey";
    private static final String MDC_VALUE = "mdcValue";
    private static final String FACILITY_VALUE = "facility";

    @Mock
    private GelfAppender appender;

    @Mock
    private ILoggingEvent event;

    private GelfMessageFactory marshaller;

    @Before
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(event.getMDCPropertyMap())
                .thenReturn(Collections.singletonMap(MDC_KEY, MDC_VALUE));

        marshaller = new DefaultGelfMessageFactory();
    }

    @Test
    public void should_include_host() {
        when(appender.getOriginHost()).thenReturn(HOST);

        GelfMessage message = marshaller.createMessage(appender, event);

        assertThat(message.getHost(), is(HOST));
    }

    @Test
    public void should_exclude_MDC() {
        when(appender.isMdcIncluded()).thenReturn(false);

        GelfMessage message = marshaller.createMessage(appender, event);

        assertNull(message.getField(MDC_KEY));
    }

    @Test
    public void should_include_MDC() {
        when(appender.isMdcIncluded()).thenReturn(true);

        GelfMessage message = marshaller.createMessage(appender, event);

        assertEquals(MDC_VALUE, message.getField(MDC_KEY));
    }

    @Test
    public void should_include_facility() {
        when(appender.getFacility()).thenReturn(FACILITY_VALUE);

        GelfMessage message = marshaller.createMessage(appender, event);

        assertEquals(FACILITY_VALUE, message.getField(GelfMessage.FACILITY));
    }
}
