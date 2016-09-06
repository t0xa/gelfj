package org.graylog2.sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

public abstract class BufferBuilder {
	protected byte[] gzipMessage(String message) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			GZIPOutputStream stream = new GZIPOutputStream(bos);
			byte[] bytes;
			try {
				bytes = message.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("No UTF-8 support available.", e);
			}
			stream.write(bytes);
			stream.finish();
			stream.close();
			byte[] zipped = bos.toByteArray();
			bos.close();
			return zipped;
		} catch (IOException e) {
			return null;
		}
	}
}
