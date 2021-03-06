/*
 * Copyright (c) 2016, Stein Eldar Johnsen
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Similar to java native {@link java.io.InputStreamReader}, but locked to
 * utf-8, and explicitly with no buffering whatsoever. It will only read one
 * byte at a time until it has a valid unicode char.
 * <p>
 * In order to make this reader more efficient, rather wrap the input stream in
 * a BufferedInputStream, which can pass on any buffered bytes to later uses.
 * E.g.:
 * </p>
 * <pre>
 *     Reader reader = new Utf8StreamReader(new BufferedInputStream(in));
 * </pre>
 */
public class Utf8StreamReader extends Reader {
    private final int[]       buffer;
    private final boolean     strict;

    private InputStream in;

    private char surrogate;

    public Utf8StreamReader(InputStream in) {
        this(in, true);
    }

    public Utf8StreamReader(InputStream in, boolean strict) {
        this.in = in;
        this.buffer = new int[6];
        this.surrogate = 0;
        this.strict = strict;
    }

    @Override
    public int read(@Nonnull char[] char_buffer, int off, int len) throws IOException {
        if (in == null) {
            throw new IOException("Reading from a closed stream.");
        }

        for (int i = 0; i < len; ++i) {
            if (surrogate != 0) {
                char_buffer[off + i] = surrogate;
                surrogate = 0;
                continue;
            }

            final int r = in.read();
            if (r < 0) {
                if (i == 0) {
                    return -1;
                }
                return i;
            } else if (r < 0x80) {
                char_buffer[off + i] = (char) r;
            } else if ((r & 0xC0) == 0x80) {
                // 10xxxxxx: This byte pattern should not be here.
                if (strict) {
                    throw new UnsupportedEncodingException(String.format(Locale.ENGLISH,
                                                                         "Unexpected utf-8 entity char: 0x%02x",
                                                                         r));
                }
                char_buffer[off + i] = '?';
            } else if ((r & 0xFE) == 0xFE) {
                // invalid utf-8 starting byte.
                if (strict) {
                    throw new UnsupportedEncodingException(String.format(Locale.ENGLISH,
                                                                         "Unexpected utf-8 non-entity char: 0x%02x",
                                                                         r));
                }
                char_buffer[off + i] = '?';
            } else {
                buffer[0] = r;
                int c = 1;

                // 110xxxxx + 1 * 10xxxxxx  = 11 bit
                if ((r & 0xC0) == 0xC0) {
                    buffer[c++] = in.read();

                    // 1110xxxx + 2 * 10xxxxxx  = 16 bit
                    if ((r & 0xE0) == 0xE0) {
                        buffer[c++] = in.read();

                        // 11110xxx + 3 * 10xxxxxx  = 21 bit
                        if ((r & 0xF0) == 0xF0) {
                            buffer[c++] = in.read();

                            // 111110xx + 4 * 10xxxxxx  = 26 bit
                            if ((r & 0xF8) == 0xF8) {
                                buffer[c++] = in.read();

                                // 1111110x + 5 * 10xxxxxx  = 31 bit
                                if ((r & 0xFC) == 0xFC) {
                                    buffer[c++] = in.read();
                                }
                            }
                        }
                    }
                }

                char_buffer[off + i] = convert(buffer, c);
            }
        }
        return len;
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            in = null;
        }
    }

    @Override
    public boolean ready() throws IOException {
        return in != null && in.available() > 0;
    }

    private char convert(final int[] arr, final int num) throws IOException {
        int cp;
        switch (num) {
            case 2:
                cp = (arr[0] & 0x1f);
                break;
            case 3:
                cp = (arr[0] & 0x0f);
                break;
            case 4:
                cp = (arr[0] & 0x07);
                break;
            case 5:
                cp = (arr[0] & 0x03);
                break;
            case 6:
                cp = (arr[0] & 0x01);
                break;
            default:
                // Should be impossible, but you never know.
                // See and fix read() method if this ever happens.
                throw new IOException("Unhandled utf-8 char length: " + num);
        }
        for (int i = 1; i < num; ++i) {
            if (arr[i] == -1) {
                throw new IOException("End of stream inside utf-8 encoded entity.");
            }
            if ((arr[i] & 0xC0) != 0x80) {
                if (strict) {
                    throw new UnsupportedEncodingException(String.format(Locale.ENGLISH,
                                                                         "Unexpected non-entity utf-8 char in entity extra bytes: 0x%02x",
                                                                         arr[i]));
                }
                return '?';
            }
            cp = (cp << 6) | (arr[i] & 0x3f);
        }

        if (Character.isBmpCodePoint(cp)) {
            return (char) cp;
        } else {
            surrogate = Character.lowSurrogate(cp);
            return Character.highSurrogate(cp);
        }
    }
}
