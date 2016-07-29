package org.graylog2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

import org.graylog2.constants.TCPDelimiter;

public class GelfTCPSender implements GelfSender {
	private boolean shutdown = false;
	private InetAddress host;
	private int port;
	private Socket socket;
    private OutputStream os;
    private TCPDelimiter delimiter;

    public GelfTCPSender(String tcpGraylogHost, int graylogPort, TCPDelimiter delimiter) throws IOException
    {
    	this.host = InetAddress.getByName(tcpGraylogHost);
		this.port = graylogPort;
		this.socket = new Socket(this.host, port);
        this.os = socket.getOutputStream();
        this.delimiter = getTcpDelimiter(delimiter);
    }

	public GelfTCPSender(String host, int port) throws IOException {
		this(host,port,TCPDelimiter.NULL);
	}
	
	 public GelfTCPSender() {
	    }
	 
	 private TCPDelimiter getTcpDelimiter(TCPDelimiter delimiter)
	    {
	    	if(delimiter == null)
	    	{
	    		return TCPDelimiter.NULL;
	    	}
	    	
	    	return delimiter;
	    }

	public GelfSenderResult sendMessage(GelfMessage message) {
		if (shutdown || !message.isValid()) {
			return GelfSenderResult.MESSAGE_NOT_VALID_OR_SHUTTING_DOWN;
		}

		try {
			// reconnect if necessary
			if (socket == null || os == null) {
				socket = new Socket(host, port);
                os = socket.getOutputStream();
			}

            os.write(message.toTCPBuffer(delimiter).array());

			return GelfSenderResult.OK;
		} catch (IOException e) {
			// if an error occours, signal failure
			socket = null;
			return new GelfSenderResult(GelfSenderResult.ERROR_CODE, e);
		}
	}

	public void close() {
		shutdown = true;
		try {
            if (os != null){
                os.close();
            }
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
