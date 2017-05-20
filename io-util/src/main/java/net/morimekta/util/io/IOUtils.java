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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;

/**
 * IO Stream static utilities.
 */
public class IOUtils {
    /**
     * Skip all bytes in stream until (and including) given separator is found.
     *
     * @param in Input stream to read from.
     * @param separator Separator bytes to skip until.
     * @return True iff the separator was encountered.
     * @throws IOException if unable to read from stream.
     */
    public static boolean skipUntil(InputStream in, byte[] separator) throws IOException {
        if (separator == null) {
            throw new NullPointerException("Null separator given");
        }
        final int len = separator.length;
        if(len > 0) {
            if(len == 1) { return skipUntil(in, separator[0]); }
            if(len > 4) { return skipUntilInternal(in, separator, new byte[len]); }

            int mask = len == 2 ? 0xffff : len == 3 ? 0xffffff : 0xffffffff;
            int sep = (separator[0] % 0x100) << 8 | ((int) separator[1] % 0x100);
            if(len > 2) {
                sep = sep << 8 | ((int) separator[2] % 0x100);
            }
            if(len > 3) {
                sep = sep << 8 | ((int) separator[3] % 0x100);
            }
            int r, p = 0;
            int tmp = 0;
            while((r = in.read()) >= 0) {
                ++p;
                tmp = tmp << 8 | r;
                if(p >= len && (tmp & mask) == sep) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Skip all bytes in stream until (and including) given byte is found.
     *
     * @param in Input stream to read from.
     * @param separator Byte to skip until.
     * @return True iff the separator was encountered.
     * @throws IOException if unable to read from stream.
     */
    public static boolean skipUntil(InputStream in, byte separator) throws IOException {
        int r;
        while((r = in.read()) >= 0) {
            if(((byte) r) == separator) { return true; }
        }
        return false;
    }

    /**
     * Copy all available data from one stream to another.
     *
     * @param in The stream to read fromn.
     * @param out The stream to write to.
     * @throws IOException If unable to read from or write to streams.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUF_SIZE];
        int r;
        while((r = in.read(buffer)) >= 0) {
            out.write(buffer, 0, r);
        }
    }

    /**
     * Read next string from input stream.
     *
     * @param is The input stream to read.
     * @return The resulting string.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(InputStream is) throws IOException {
        return readString(new Utf8StreamReader(is), '\0');
    }

    /**
     * Read next string from input stream.
     *
     * @param is   The input stream to read.
     * @param term Terminator character.
     * @return The string up until, but not including the terminator.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(InputStream is, String term) throws IOException {
        return readString(new Utf8StreamReader(is), term);
    }

    /**
     * Read next string from input stream.
     *
     * @param is   The input stream to read.
     * @param term Terminator character.
     * @return The string up until, but not including the terminator.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(InputStream is, char term) throws IOException {
        return readString(new Utf8StreamReader(is), term);
    }

    /**
     * Read next string from input stream. The terminator is read but not
     * included in the resulting string.
     *
     * @param is The input stream to read.
     * @return The string up until, but not including the terminator.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(Reader is) throws IOException {
        return readString(is, '\0');
    }

    /**
     * Read next string from input stream.
     *
     * @param is   The reader to read characters from.
     * @param term Terminator character.
     * @return The string up until, but not including the terminator.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(Reader is, char term) throws IOException {
        CharArrayWriter baos = new CharArrayWriter();

        int ch_int;
        while ((ch_int = is.read()) >= 0) {
            final char ch = (char) ch_int;
            if (ch == term) {
                break;
            }
            baos.write(ch);
        }

        return baos.toString();
    }

    /**
     * Read next string from input stream.
     *
     * @param is   The reader to read characters from.
     * @param term Terminator character.
     * @return The string up until, but not including the terminator.
     * @throws IOException when unable to read from stream.
     */
    public static String readString(Reader is, String term) throws IOException {
        CharArrayWriter baos = new CharArrayWriter();

        int ch_int;
        char last = term.charAt(term.length() - 1);
        while ((ch_int = is.read()) >= 0) {
            final char ch = (char) ch_int;
            baos.write(ch);
            if (ch == last && baos.size() >= term.length()) {
                String tmp = baos.toString();
                if (tmp.substring(tmp.length() - term.length())
                       .equals(term)) {
                    return tmp.substring(0, tmp.length() - term.length());
                }
            }
        }

        return baos.toString();
    }

    /* -- PRIVATE METHODS -- */

    private static final int BUF_SIZE = 4096;

    private IOUtils() {}

    private static boolean skipUntilInternal(InputStream in, byte[] separator, byte[] buffer) throws IOException {
        int r;
        int l = 0;
        while((r = in.read()) >= 0) {
            System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
            buffer[buffer.length - 1] = (byte) r;
            ++l;
            if(l >= buffer.length && Arrays.equals(separator, buffer)) { return true; }
        }
        return false;
    }
}
