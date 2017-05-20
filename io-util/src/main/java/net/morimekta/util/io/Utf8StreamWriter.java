/*
 * Copyright (c) 2017, Stein Eldar Johnsen
 *
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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.toCodePoint;

/**
 * Similar to java native {@link java.io.OutputStreamWriter}, but locked to
 * utf-8, and explicitly with no buffering whatsoever, with one exception only, which
 * is catching surrogate pair chars.
 * <p>
 * In order to make this writer more efficient, rather wrap the output stream in
 * a BufferedOutputStream, which can handle all the byte-level buffering.
 * E.g.:
 * </p>
 * <pre>
 *     Writer writer = new Utf8StreamWriter(new BufferedOutputStream(out));
 * </pre>
 */
public class Utf8StreamWriter extends Writer {
    private final int[] buffer;
    private final boolean strict;

    private OutputStream out;
    private char surrogate;

    public Utf8StreamWriter(OutputStream out) {
        this(out, true);
    }

    public Utf8StreamWriter(OutputStream out, boolean strict) {
        this.out = out;
        this.buffer = new int[6];
        this.surrogate = 0;
        this.strict = strict;
    }

    @Override
    public void write(char[] chars, int off, int len) throws IOException {
        if (out == null) {
            throw new IOException("Writing to a closed stream.");
        }

        for (int i = 0; i < len; ++i) {
            final char c = chars[off + i];
            if (isHighSurrogate(c)) {
                if (surrogate != 0) {
                    if (strict) {
                        throw new UnsupportedEncodingException("High surrogate " + Integer.toHexString(c) +
                                                               " after high: " +
                                                               Integer.toHexString(surrogate));
                    }
                    out.write('?');  // for the bad high surrogate
                }
                surrogate = c;
            } else if (isLowSurrogate(c)) {
                if (surrogate != 0) {
                    writeCodePoint(toCodePoint(surrogate, c));
                    surrogate = 0;
                } else {
                    if (strict) {
                        throw new UnsupportedEncodingException("Missing high surrogate before low: " +
                                                               Integer.toHexString(c));
                    }
                    out.write('?');
                }
            } else {
                if (surrogate != 0) {
                    if (strict) {
                        throw new UnsupportedEncodingException("Missing low surrogate after high: " +
                                                               Integer.toHexString(surrogate));
                    }
                    out.write('?');  // for the bad high surrogate
                }
                writeCodePoint(c);
            }
        }
    }

    private void writeCodePoint(int cp) throws IOException {
        int cp0 = cp;
        if (cp < 0x80) {
            // ASCII
            out.write((byte) cp);
        } else {
            // UTF-8 entity.
            int c = 0;
            int lastOverLimit = 0x40;
            while (cp >= lastOverLimit) {
                buffer[c++] = (cp & 0x3f) | 0x80;
                cp >>>= 6;
                lastOverLimit >>>= 1;
            }
            switch (c) {
                case 1: buffer[c] = 0xC0 | cp; break;
                case 2: buffer[c] = 0xE0 | cp; break;
                case 3: buffer[c] = 0xF0 | cp; break;
                case 4: buffer[c] = 0xF8 | cp; break;
                case 5: buffer[c] = 0xFC | cp; break;
                // Should be impossible, fix write() methods if it isn't
                default: throw new IOException("Unreachable code reached: " + Integer.toHexString(cp0));
            }

            // Write the bytes in reverse order to make it big-endian like.
            for (int i = c; i >= 0; --i) {
                out.write(buffer[i]);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (out != null) {
            if (strict && surrogate != 0) {
                throw new IOException("Surrogate high pair written, but no low");
            }
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            try {
                flush();
                out.close();
            } finally {
                out = null;
            }
        }
    }
}
