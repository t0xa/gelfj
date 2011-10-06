package org.graylog2.log;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;

public class GelfConsoleAppender extends ConsoleAppender {
    
    // parent overrides.
    
    public GelfConsoleAppender() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GelfConsoleAppender(Layout layout) {
        super(layout);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GelfConsoleAppender(Layout layout, String target) {
        super(layout, target);    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    // the important part.
    
    @Override
    protected void subAppend(LoggingEvent event) {
        GelfMessage gelf = GelfMessageFactory.makeMessage(event, !layout.ignoresThrowable());
        this.qw.write(gelf.toJson());
        this.qw.write(Layout.LINE_SEP);

        if (this.immediateFlush) {
            this.qw.flush();
        }
    }
}
