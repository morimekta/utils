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

import net.morimekta.util.Binary;

import java.io.IOException;
import java.io.InputStream;

/**
 * IO-Optimized binary reader.
 */
public abstract class BinaryReader extends InputStream {
    private final InputStream in;

    public BinaryReader(InputStream in) {
        this.in = in;
    }

    /**
     * Read a single byte.
     *
     * @return the byte value, or -1 if end of stream.
     * @throws IOException if unable to read from stream.
     */
    @Override
    public int read() throws IOException {
        return in.read();
    }

    /**
     * Read binary data from stream.
     *
     * @param out The output buffer to read into.
     * @throws IOException if unable to read from stream.
     */
    @Override
    public int read(byte[] out) throws IOException {
        int i, off = 0;
        while (off < out.length && (i = in.read(out, off, out.length - off)) > 0) {
            off += i;
        }
        return off;
    }

    /**
     * Read binary data from stream.
     *
     * @param out The output buffer to read into.
     * @param off Offset in out array to writeBinary to.
     * @param len Number of bytes to read.
     * @throws IOException if unable to read from stream.
     */
    @Override
    public int read(byte[] out, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || (off + len) > out.length) {
            throw new IllegalArgumentException(String.format(
                    "Illegal arguments for read: byte[%d], off:%d, len:%d",
                    out.length, off, len));
        }

        final int end = off + len;
        int pos = off;
        int i;
        while (pos < end && (i = in.read(out, off, end - off)) >= 0) {
            pos += i;
        }
        return pos - off;
    }

    /**
     * Read binary data from stream.
     *
     * @param out The output buffer to read into.
     * @throws IOException if unable to read from stream.
     */
    public void expect(byte[] out) throws IOException {
        int i, off = 0;
        while (off < out.length && (i = in.read(out, off, out.length - off)) > 0) {
            off += i;
        }
        if (off < out.length) {
            throw new IOException("Not enough data available on stream: " + off + " < " + out.length);
        }
    }

    @Override
    public void close() {}

    /**
     * Read a byte from the input stream.
     *
     * @return The number read.
     * @throws IOException If no byte to read.
     */
    public byte expectByte() throws IOException {
        int read = in.read();
        if (read < 0) {
            throw new IOException("Missing expected byte");
        }
        return (byte) read;
    }

    /**
     * Read a short from the input stream.
     *
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public short expectShort() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            throw new IOException("Missing byte 1 to expected short");
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to expected short");
        }
        return (short) unshift2bytes(b1, b2);
    }

    /**
     * Read an int from the input stream.
     *
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public int expectInt() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            throw new IOException("Missing byte 1 to expected int");
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to expected int");
        }
        int b3 = in.read();
        if (b3 < 0) {
            throw new IOException("Missing byte 3 to expected int");
        }
        int b4 = in.read();
        if (b4 < 0) {
            throw new IOException("Missing byte 4 to expected int");
        }
        return unshift4bytes(b1, b2, b3, b4);
    }

    /**
     * Read a long int from the input stream.
     *
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public long expectLong() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            throw new IOException("Missing byte 1 to expected long");
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to expected long");
        }
        int b3 = in.read();
        if (b3 < 0) {
            throw new IOException("Missing byte 3 to expected long");
        }
        long b4 = in.read();
        if (b4 < 0) {
            throw new IOException("Missing byte 4 to expected long");
        }
        long b5 = in.read();
        if (b5 < 0) {
            throw new IOException("Missing byte 5 to expected long");
        }
        long b6 = in.read();
        if (b6 < 0) {
            throw new IOException("Missing byte 6 to expected long");
        }
        long b7 = in.read();
        if (b7 < 0) {
            throw new IOException("Missing byte 7 to expected long");
        }
        long b8 = in.read();
        if (b8 < 0) {
            throw new IOException("Missing byte 8 to expected long");
        }

        return unshift8bytes(b1, b2, b3, b4, b5, b6, b7, b8);
    }

    /**
     * Read a double from the input stream.
     *
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public double expectDouble() throws IOException {
        return Double.longBitsToDouble(expectLong());
    }

    /**
     * Read binary data from stream.
     *
     * @param bytes Number of bytes to read.
     * @return The binary wrapper.
     * @throws IOException if unable to read from stream.
     */
    public Binary expectBinary(int bytes) throws IOException {
        return Binary.wrap(expectBytes(bytes));
    }

    /**
     * Read binary data from stream.
     *
     * @param bytes Number of bytes to read.
     * @return The binary wrapper.
     * @throws IOException if unable to read from stream.
     */
    public byte[] expectBytes(final int bytes) throws IOException {
        byte[] out = new byte[bytes];
        expect(out);
        return out;
    }

    /**
     * Read an unsigned byte from the input stream.
     *
     * @return Unsigned byte.
     * @throws IOException If no number to read.
     */
    public int expectUInt8() throws IOException {
        int read = in.read();
        if (read < 0) {
            throw new IOException("Missing unsigned byte");
        }
        return read;
    }

    /**
     * Read an unsigned short from the input stream.
     *
     * @return The number read.
     * @throws IOException If no number to read.
     */
    public int expectUInt16() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            throw new IOException("Missing byte 1 to expected uint16");
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to expected uint16");
        }
        return unshift2bytes(b1, b2);
    }

    /**
     * Read an unsigned short from the input stream.
     *
     * @return The number read.
     * @throws IOException If no number to read.
     */
    public int readUInt16() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            return 0;
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to read uint16");
        }
        return unshift2bytes(b1, b2);
    }

    /**
     * Read an unsigned short from the input stream.
     *
     * @return The number read.
     * @throws IOException If no number to read.
     */
    public int expectUInt24() throws IOException {
        int b1 = in.read();
        if (b1 < 0) {
            throw new IOException("Missing byte 1 to expected uint24");
        }
        int b2 = in.read();
        if (b2 < 0) {
            throw new IOException("Missing byte 2 to expected uint24");
        }
        int b3 = in.read();
        if (b3 < 0) {
            throw new IOException("Missing byte 3 to expected uint24");
        }
        return unshift3bytes(b1, b2, b3);
    }

    /**
     * Read an unsigned int from the input stream.
     *
     * @return The number read.
     * @throws IOException If no number to read.
     */
    public int expectUInt32() throws IOException {
        return expectInt();
    }

    /**
     * Read an unsigned number from the input stream.
     *
     * @param bytes Number of bytes to read.
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public int expectUnsigned(int bytes) throws IOException {
        switch (bytes) {
            case 4:
                return expectUInt32();
            case 3:
                return expectUInt24();
            case 2:
                return expectUInt16();
            case 1:
                return expectUInt8();
        }
        throw new IllegalArgumentException("Unsupported byte count for unsigned: " + bytes);
    }

    /**
     * Read an signed number from the input stream.
     *
     * @param bytes Number of bytes to read.
     * @return The number read.
     * @throws IOException if unable to read from stream.
     */
    public long expectSigned(int bytes) throws IOException {
        switch (bytes) {
            case 8:
                return expectLong();
            case 4:
                return expectInt();
            case 2:
                return expectShort();
            case 1:
                return expectByte();
        }
        throw new IllegalArgumentException("Unsupported byte count for signed: " + bytes);
    }

    /**
     * Read a long number as zigzag encoded from the stream. The least
     * significant bit becomes the sign, and the actual value is absolute and
     * shifted one bit. This makes it maximum compressed both when positive and
     * negative.
     *
     * @return The zigzag decoded value.
     * @throws IOException if unable to read from stream.
     */
    public int readIntZigzag() throws IOException {
        int value = readIntVarint();
        return (value & 1) != 0 ? ~(value >>> 1) : value >>> 1;
    }

    /**
     * Read a long number as zigzag encoded from the stream. The least
     * significant bit becomes the sign, and the actual value is absolute and
     * shifted one bit. This makes it maximum compressed both when positive and
     * negative.
     *
     * @return The zigzag decoded value.
     * @throws IOException if unable to read from stream.
     */
    public long readLongZigzag() throws IOException {
        long value = readLongVarint();
        return (value & 1) != 0 ? ~(value >>> 1) : value >>> 1;
    }

    /**
     * Write a signed number as varint (integer with variable number of bytes,
     * determined as part of the bytes themselves.
     *
     * NOTE: Reading varint accepts end of stream as '0'.
     *
     * @return The varint read from stream.
     * @throws IOException if unable to read from stream.
     */
    public int readIntVarint() throws IOException {
        int i = in.read();
        if (i < 0) {
            return 0;
        }

        boolean c = (i & 0x80) > 0;
        int out = (i & 0x7f);

        int shift = 0;
        while (c) {
            shift += 7;
            i = expectUInt8();
            c = (i & 0x80) > 0;
            out |= ((i & 0x7f) << shift);
        }
        return out;
    }

    /**
     * Write a signed number as varint (integer with variable number of bytes,
     * determined as part of the bytes themselves.
     *
     * NOTE: Reading varint accepts end of stream as '0'.
     *
     * @return The varint read from stream.
     * @throws IOException if unable to read from stream.
     */
    public long readLongVarint() throws IOException {
        int i = in.read();
        if (i < 0) {
            return 0L;
        }

        boolean c = (i & 0x80) > 0;
        long out = (i & 0x7f);

        int shift = 0;
        while (c) {
            shift += 7;
            i = expectUInt8();
            c = (i & 0x80) > 0;
            out = out | ((long) i & 0x7f) << shift;
        }
        return out;
    }

    protected abstract int unshift2bytes(int b1, int b2);

    protected abstract int unshift3bytes(int b1, int b2, int b3);

    protected abstract int unshift4bytes(int b1, int b2, int b3, int b4);

    protected abstract long unshift8bytes(long b1, long b2, long b3, long b4, long b5, long b6, long b7, long b8);
}
