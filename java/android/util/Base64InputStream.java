/*
 * Copyright (c) 2016, Providence Authors
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
package android.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link Base64InputStream} will read data from another
 * <tt>InputStream</tt>, given in the constructor, and encode
 * to Base64 notation on the fly.
 *
 * @see Base64
 */
public class Base64InputStream extends FilterInputStream {
    private final byte[] decodabet;      // Local copies to avoid extra method calls
    private final byte[] buffer3;         // Small buffer holding converted data
    private final int options;        // Record options used to create the stream.
    private final byte[] buffer4;

    private int position;       // Current position in the buffer
    private int numSigBytes;    // Number of meaningful bytes in the buffer

    /**
     * An InputStream that performs Base64 decoding on the data read from the wrapped stream.
     *
     * @param in      the InputStream to read the source data from
     * @param options bit flags for controlling the decoder; see the constants in Base64
     */
    public Base64InputStream(InputStream in, int options) {
        super(in);
        this.options = options; // Record for later
        this.buffer3 = new byte[3];
        this.buffer4 = new byte[4];
        this.position = -1;
        this.numSigBytes = 0;
        this.decodabet = Base64.getDecodabet(options);
    }   // end constructor

    @Override
    public int read() throws IOException {
        if (in == null) {
            throw new IOException("Reading from a closed Base64InputStream");
        }

        if (position < 0) {
            int i;

            // Read four "meaningful" bytes:
            for (i = 0; i < 4; i++) {
                int b;
                do {
                    b = in.read();
                    // treat bytes as ASCII, skip white space only.
                } while ((b >= 0) && (decodabet[b & 0x7f] == Base64.WHITE_SPACE_ENC));

                if (b < 0) {
                    break;
                }

                buffer4[i] = (byte) b;
            }

            position = 0;
            if (i == 0) {
                numSigBytes = 0;
                return -1;
            }
            numSigBytes = Base64.decode4to3(buffer4, 0, i, buffer3, 0, decodabet);
        }   // end else: get data

        // Got data?
        if (position >= 0) {
            if (position >= numSigBytes) {
                return -1;
            }

            int b = buffer3[position++] & 0xFF;

            if (position >= 3) {
                position = -1;
                numSigBytes = 0;
            }

            return b;
        } else {
            throw new IOException("Error in Base64 code reading stream.");
        }
    }

    @Override
    public int read(byte[] dest, int off, int len) throws IOException {
        if (len == 0) return 0;

        int i;
        int b;
        for (i = 0; i < len; i++) {
            b = read();
            if (b < 0) break;
            dest[off + i] = (byte) b;
        }

        return i > 0 ? i : -1;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            try {
                in.close();
            } finally {
                in = null;
            }
        }
    }

    @Override
    public int available() {
        return (position >= 0 ? numSigBytes - position : 0);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("skip/reset not supported");
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
