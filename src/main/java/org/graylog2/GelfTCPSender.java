package org.graylog2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GelfTCPSender implements GelfSender {

	public static final int DEFAULT_PORT = 12201;
	private InetAddress host;
	private int port;
	private BlockingQueue<ByteBuffer> blockingQueue = new ArrayBlockingQueue<ByteBuffer>(10000);
	private SenderThread senderThread;
	private boolean shutdown = false;

	public GelfTCPSender(String host) throws IOException {
		this(host, DEFAULT_PORT);
	}

	public GelfTCPSender(String host, int port) throws IOException {
		this.host = InetAddress.getByName(host);
		this.port = port;
		this.senderThread = new SenderThread();
		this.senderThread.setDaemon(true);
		this.senderThread.setPriority(Thread.MIN_PRIORITY);
		this.senderThread.start();
	}

	public boolean sendMessage(GelfMessage message) {
		if (!shutdown && message.isValid()) {
			// are we having connection problems?
			if (senderThread.socket == null) {
				return false;
			}

			blockingQueue.offer(message.toBuffer());
			return true;
		}

		return false;
	}

	public void close() {
		try {
			shutdown = true;

			while(true) {
				if (blockingQueue.isEmpty()) {
					break;
				}

				// sleep to let the queue be emptied
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		senderThread.close();
	}

	private class SenderThread extends Thread {
		private Socket socket;
		private int counter;
		private ByteBuffer currentMessage;

		public SenderThread() throws IOException {
			socket = new Socket(GelfTCPSender.this.host, GelfTCPSender.this.port);
		}

		@Override
		public void run() {
			while (true) {
				try {
					// prior message failed?
					if (currentMessage != null) {
						if (!sendMessage(currentMessage)) {
							continue;
						}

						currentMessage = null;
					}

					ByteBuffer message = blockingQueue.take();
					if (!sendMessage(message)) {
						currentMessage = message;
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void init() {
			close();
			try {
				socket = new Socket(GelfTCPSender.this.host, GelfTCPSender.this.port);
			} catch (IOException e) {
				socket = null;
				e.printStackTrace();
			}
		}

		public void close() {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private boolean sendMessage(ByteBuffer message) {
			// it no socket is available, signal failure
			if (socket == null) {
				return false;
			}

			try {
				counter++;
				socket.getOutputStream().write(message.array());

				if (counter % 5000 == 0) {
					init();
					counter = 0;
				}

				return true;
			} catch (IOException e) {
				// it write error occours, signal failure
				socket = null;
				e.printStackTrace();
				init();
			}

			return false;
		}
	}
}
