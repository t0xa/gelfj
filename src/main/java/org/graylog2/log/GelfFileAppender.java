package org.graylog2.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfMessageFactory;

import java.io.IOException;

public class GelfFileAppender extends FileAppender {
    
    // first, a bunch of useless overrides.
    
    public GelfFileAppender() {
        super();
    }

    public GelfFileAppender(Layout layout, String filename, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        super(layout, filename, append, bufferedIO, bufferSize);
    }

    public GelfFileAppender(Layout layout, String filename, boolean append) throws IOException {
        super(layout, filename, append);
    }

    public GelfFileAppender(Layout layout, String filename) throws IOException {
        super(layout, filename);
    }
    
    // now the main part.


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
