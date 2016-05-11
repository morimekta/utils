package net.morimekta.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Simple output stream backed by a byte buffer. If the
 */
public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer buffer;

    public ByteBufferOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int i) throws IOException {
        if (!buffer.hasRemaining()) {
            throw new IOException("Buffer overflow");
        }
        buffer.put((byte) i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (buffer.remaining() < bytes.length) {
            throw new IOException("Buffer overflow");
        }
        buffer.put(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        if (buffer.remaining() < len) {
            throw new IOException("Buffer overflow");
        }
        buffer.put(bytes, off, len);
    }
}
