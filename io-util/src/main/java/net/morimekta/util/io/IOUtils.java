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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            int sep = (separator[0] % 0x100) << 8 | separator[1];
            if(len > 2) {
                sep = sep << 8 | separator[2];
            }
            if(len > 3) {
                sep = sep << 8 | separator[3];
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
