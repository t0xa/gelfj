package org.graylog2.log;

import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.*;
import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSender;
import org.graylog2.sender.GelfSenderResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Anton Yakimov
 * @author Jochen Schalanda
 */
public class GelfAppenderTest {

	private static final String CLASS_NAME = GelfAppenderTest.class.getCanonicalName();
	private TestGelfSender gelfSender;
	private GelfAppender gelfAppender;
	private boolean rawExtended = false;

	@Before
	public void setUp() throws IOException {
		gelfSender = new TestGelfSender();

		gelfAppender = new GelfAppender() {

			@Override
			public GelfSender getGelfSender() {
				return gelfSender;
			}

			@Override
			public void append(LoggingEvent event) {
				super.append(event);
			}

			@Override
			public Object transformExtendedField(String field, Object object) {
				if (rawExtended) {
					return object;
				} else {
					return super.transformExtendedField(field, object);
				}
			}
		};
	}

	@After
	public void tearDown() {
		if (gelfAppender.isAddExtendedInformation()) {
			NDC.clear();
		}
	}

	@Test
	public void ensureHostnameForMessage() {

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(GelfAppenderTest.class), 123L,
				Level.INFO, "Das Auto", new RuntimeException("Volkswagen"));
		gelfAppender.append(event);

		assertThat("Message hostname", gelfSender.getLastMessage().getHost(), notNullValue());

		gelfAppender.setOriginHost("example.com");
		gelfAppender.append(event);
		assertThat(gelfSender.getLastMessage().getHost(), is("example.com"));
	}

	@Test
	public void handleNullInAppend() {

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, null,
				new RuntimeException("LOL"));
		gelfAppender.append(event);

		assertThat("Message short message", gelfSender.getLastMessage().getShortMessage(), notNullValue());
		assertThat("Message full message", gelfSender.getLastMessage().getFullMessage(), notNullValue());
	}

	@Test
	public void handleMDC() {

		gelfAppender.setAddExtendedInformation(true);

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, "",
				new RuntimeException("LOL"));
		MDC.put("foo", "bar");

		gelfAppender.append(event);

		assertEquals("bar", gelfSender.getLastMessage().getAdditonalFields().get("foo"));
		assertNull(gelfSender.getLastMessage().getAdditonalFields().get("non-existent"));
	}

	@Test
	public void handleMDCTransform() {

		gelfAppender.setAddExtendedInformation(true);

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, "",
				new RuntimeException("LOL"));
		MDC.put("foo", 200);

		gelfAppender.append(event);

		assertEquals("200", gelfSender.getLastMessage().getAdditonalFields().get("foo"));
		assertNull(gelfSender.getLastMessage().getAdditonalFields().get("non-existent"));

		rawExtended = true;
		event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, "",
				new RuntimeException("LOL"));
		gelfAppender.append(event);

		assertEquals(new Integer(200), gelfSender.getLastMessage().getAdditonalFields().get("foo"));
		assertNull(gelfSender.getLastMessage().getAdditonalFields().get("non-existent"));

	}

	@Test
	public void handleNDC() {

		gelfAppender.setAddExtendedInformation(true);

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, "",
				new RuntimeException("LOL"));
		NDC.push("Foobar");

		gelfAppender.append(event);

		assertEquals("Foobar", gelfSender.getLastMessage().getAdditonalFields().get("loggerNdc"));
	}

	@Test
	public void disableExtendedInformation() {

		gelfAppender.setAddExtendedInformation(false);

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(this.getClass()), 123L, Level.INFO, "",
				new RuntimeException("LOL"));

		MDC.put("foo", "bar");
		NDC.push("Foobar");

		gelfAppender.append(event);

		assertNull(gelfSender.getLastMessage().getAdditonalFields().get("loggerNdc"));
		assertNull(gelfSender.getLastMessage().getAdditonalFields().get("foo"));
	}

	@Test
	public void checkExtendedInformation() throws UnknownHostException, SocketException {

		gelfAppender.setAddExtendedInformation(true);

		LoggingEvent event = new LoggingEvent(CLASS_NAME, Category.getInstance(GelfAppenderTest.class), 123L,
				Level.INFO, "Das Auto", new RuntimeException("LOL"));

		gelfAppender.append(event);

		assertEquals(gelfSender.getLastMessage().getAdditonalFields().get("logger"), CLASS_NAME);
	}

	private class TestGelfSender implements GelfSender {
		private GelfMessage lastMessage;

		public GelfSenderResult sendMessage(GelfMessage message) {
			this.lastMessage = message;
			return GelfSenderResult.OK;
		}

		public void close() {
		}

		public GelfMessage getLastMessage() {
			return lastMessage;
		}
	}
}
