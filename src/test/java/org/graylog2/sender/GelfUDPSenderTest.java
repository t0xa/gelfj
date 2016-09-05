package org.graylog2.sender;

import junit.framework.TestCase;

import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSenderResult;
import org.graylog2.sender.GelfUDPSender;
import org.graylog2.sender.GelfUDPSender.UDPBufferBuilder;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GelfUDPSenderTest extends TestCase {
	@Test
	public void testReopenOfChannel() throws IOException {
		GelfUDPSender gelfUDPSender = new GelfUDPSender("localhost", 1234, 0);
		assertThat(gelfUDPSender.getChannel().isOpen(), is(true));

		GelfMessage error = new GelfMessage("Test short", "Test long", new Date().getTime(), "ERROR");
		error.setHost("localhost");
		error.setVersion("1.3");
		error.setFacility("F");

		GelfSenderResult result = gelfUDPSender.sendMessage(error);

		assertThat(result, is(GelfSenderResult.OK));

		gelfUDPSender.getChannel().close();

		assertThat(gelfUDPSender.getChannel().isOpen(), is(false));

		GelfSenderResult secondMessage = gelfUDPSender.sendMessage(error);
		assertThat(secondMessage, is(GelfSenderResult.OK));
		assertThat(gelfUDPSender.getChannel().isOpen(), is(true));
	}

	@Test
	public void testLongMessage() throws Exception {
		String longString = "01234567890123456789 ";
		for (int i = 0; i < 15; i++) {
			longString += longString;
		}
		UDPBufferBuilder bufferBuilder = new UDPBufferBuilder();
		ByteBuffer[] bytes = bufferBuilder.toUDPBuffers(longString);
		assertEquals(2, bytes.length);
	}

	@Test
	public void testShortMessage() throws Exception {
		UDPBufferBuilder bufferBuilder = new UDPBufferBuilder();
		ByteBuffer[] bytes = bufferBuilder.toUDPBuffers("very short");
		assertEquals(1, bytes.length);
	}
}