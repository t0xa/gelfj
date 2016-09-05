package org.graylog2.log;

import java.util.Map;

import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.message.GelfMessage;
import org.graylog2.sender.GelfSenderResult;
import org.json.simple.JSONValue;

/**
 * A GelfAppender which will parse the given JSON message into additional fields in GELF
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 * @author the-james-burton
 */
public class GelfJsonAppender extends GelfAppender {

  @Override
  protected void append(final LoggingEvent event) {
    GelfMessage gelfMessage = GelfMessageFactory.makeMessage(layout, event, this);

    @SuppressWarnings("unchecked")
    Map<String, String> fields = (Map<String, String>) JSONValue.parse(event.getMessage().toString());

    if (fields != null) {
      for (String key : fields.keySet()) {
        gelfMessage.getAdditonalFields().put(key, fields.get(key));
      }
    }

    if (getGelfSender() == null) {
      errorHandler.error("Could not send GELF message. Gelf Sender is not initialised and equals null");
    } else {
      GelfSenderResult gelfSenderResult = getGelfSender().sendMessage(gelfMessage);
      if (!GelfSenderResult.OK.equals(gelfSenderResult)) {
        errorHandler.error("Error during sending GELF message. Error code: " + gelfSenderResult.getCode() + ".",
            gelfSenderResult.getException(), ErrorCode.WRITE_FAILURE);
      }
    }
  }

}
