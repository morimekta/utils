/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
            return buffer.get() % 0x100;
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
            throw new IOException("No mark set on stream", ime);
        }
    }
}
