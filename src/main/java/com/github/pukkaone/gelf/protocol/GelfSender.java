package com.github.pukkaone.gelf.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public abstract class GelfSender {

    public static final int DEFAULT_PORT = 12201;

    protected byte[] gzipMessage(String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(message.length());
        try {
            GZIPOutputStream stream = new GZIPOutputStream(bos);
            stream.write(message.getBytes(StandardCharsets.UTF_8));
            stream.close();

            byte[] zipped = bos.toByteArray();
            bos.close();
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }

    public abstract boolean sendMessage(GelfMessage message);

    public abstract void close();
}
