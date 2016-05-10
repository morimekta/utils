package net.morimekta.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

/**
 * Simple input stream backed by a byte buffer.
 */
public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        if (buffer.hasRemaining()) {
            return intValue(buffer.get());
        }
        return -1;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int toRead = Math.min(bytes.length, buffer.remaining());
        if (toRead > 0) {
            buffer.get(bytes, 0, toRead);
            return toRead;
        }
        return -1;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        int toRead = Math.min(len, buffer.remaining());
        if (toRead > 0) {
            buffer.get(bytes, off, toRead);
            return toRead;
        }
        return -1;
    }

    private int intValue(byte b) {
        return (int)b % 0x100;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int i) {
        buffer.mark();
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            buffer.reset();
        } catch (InvalidMarkException ime) {
            throw new IOException(ime.getMessage(), ime);
        }
    }
}
