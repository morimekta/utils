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

import javax.annotation.Nonnull;
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
    public void write(@Nonnull byte[] bytes) throws IOException {
        if (buffer.remaining() < bytes.length) {
            throw new IOException("Buffer overflow");
        }
        buffer.put(bytes, 0, bytes.length);
    }

    @Override
    public void write(@Nonnull byte[] bytes, int off, int len) throws IOException {
        if (buffer.remaining() < len) {
            throw new IOException("Buffer overflow");
        }
        buffer.put(bytes, off, len);
    }
}
