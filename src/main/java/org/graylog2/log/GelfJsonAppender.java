package org.graylog2.log;

import java.util.Map;

import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;
import org.graylog2.GelfSenderResult;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * A GelfAppender which will parse the given JSON message into additional fields in GELF
 *
 * @author Anton Yakimov
 * @author Jochen Schalanda
 * @author the-james-burton
 */
@SuppressWarnings("unchecked")
public class GelfJsonAppender extends GelfAppender {

  @Override
  protected void append(final LoggingEvent event) {
    GelfMessage gelfMessage = GelfMessageFactory.makeMessage(layout, event, this);

    Map<String, Object> fields = null;
    try {
      fields = (Map<String, Object>) JSONValue.parseWithException(event.getMessage().toString());
    } catch (ParseException ex) {
      errorHandler.error(String.format("unable to parse as JSON: %s : %s",
          event.getMessage().toString(), ex.toString())); // UNUSUAL: the ParseException returns message in toString()!
    }

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
