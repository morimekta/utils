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
package android.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that does Base64 encoding on the data written to it, writing
 * the resulting data to another OutputStream.
 */
public class Base64OutputStream extends FilterOutputStream {
    private final byte[] buffer;
    private final byte[] b4;         // Scratch used in a few places
    private final boolean breakLines;
    private final boolean crlf;
    private final byte[] alphabet;
    private final boolean noPadding;
    private final boolean close;

    private int position;
    private int lineLength;

    /**
     * Performs Base64 encoding on the data written to the stream, writing the
     * encoded data to another OutputStream.
     *
     * @param out     the OutputStream to write the encoded data to
     * @param options bit flags for controlling the encoder; see the constants in Base64
     */
    public Base64OutputStream(OutputStream out, int options) {
        super(out);
        this.breakLines = (options & (Base64.NO_WRAP)) == 0;
        this.alphabet = Base64.getAlphabet(options);
        this.noPadding = (options & (Base64.NO_PADDING)) != 0;
        this.close = (options & Base64.NO_CLOSE) == 0;
        this.crlf = (options & Base64.CRLF) != 0;
        this.buffer = new byte[3];
        this.position = 0;
        this.lineLength = 0;
        this.b4 = new byte[4];
    }

    /**
     * Writes the byte to the output stream after converting to/from Base64
     * notation.
     *
     * When encoding, bytes are buffered three at a time before the output
     * stream actually gets a write() call. When decoding, bytes are buffered
     * four at a time.
     *
     * @param theByte the byte to write
     */
    @Override
    public void write(int theByte)
            throws IOException {
        if (out == null) {
            throw new IOException("Writing to closed Base64OutputStream");
        }
        // Encoding suspended?
        buffer[position++] = (byte) theByte;
        if (position >= 3) { // Enough to encodeToString.
            if (breakLines && lineLength >= Base64.MAX_LINE_LENGTH) {
                if (crlf) {
                    out.write(Base64.CR);
                }
                out.write(Base64.LF);
                lineLength = 0;
            }

            Base64.encode3to4(buffer, 0, 3, b4, 0, noPadding, alphabet);
            out.write(b4, 0, 4);
            lineLength += 4;
            position = 0;
        }
    }

    /**
     * Flushed the enclosed output stream, but does not write uncompleted 4-blocks.
     * To ensure consistency that only happens on close.
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        if (out == null) {
            throw new IOException("Flushing a closed Base64OutputStream");
        }
        out.flush();
    }

    /**
     * Encodes and writes the remaining buffered bytes, adds necessary padding,
     * and closes the output stream.
     *
     * Does nothing if the stream is already closed.
     *
     * @throws IOException If unable to write the remaining bytes or close the
     * stream.
     */
    @Override
    public void close() throws IOException {
        if (out != null) {
            IOException ex = null;
            try {
                if (position > 0) {
                    if (breakLines && lineLength >= Base64.MAX_LINE_LENGTH) {
                        if (crlf) {
                            out.write(Base64.CR);
                        }
                        out.write(Base64.LF);
                    }

                    int len = Base64.encode3to4(buffer, 0, position, b4, 0, noPadding, alphabet);
                    out.write(b4, 0, len);
                }
                out.flush();
            } catch (IOException e) {
                ex = e;
            }

            try {
                // 2. Actually close the stream
                // Base class both flushes and closes.
                if (close) {
                    out.close();
                }
            } finally {
                out = null;
            }

            if (ex != null) {
                throw new IOException(ex);
            }
        }
    }
}
