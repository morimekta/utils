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
import java.io.OutputStream;

/**
 * IO-Optimized binary writer.
 */
public abstract class BinaryWriter extends OutputStream {
    protected final OutputStream out;

    public BinaryWriter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        out.write(bytes, off, len);
    }

    @Override
    public void close() {}

    /**
     * Write a signed byte to the output stream.
     *
     * @param integer The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeByte(byte integer) throws IOException {
        out.write(integer);
        return 1;
    }

    /**
     * Write a signed short to the output stream.
     *
     * @param integer The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeShort(short integer) throws IOException;

    /**
     * Write a signed int to the output stream.
     *
     * @param integer The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeInt(int integer) throws IOException;

    /**
     * Write a signed long to the output stream.
     *
     * @param integer The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeLong(long integer) throws IOException;

    /**
     * Write a double value to stream.
     *
     * @param value The double value to writeBinary.
     * @return The number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeDouble(double value) throws IOException {
        return writeLong(Double.doubleToLongBits(value));
    }

    /**
     * Write the content of binary to output stream.
     *
     * @param value Binary to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeBinary(Binary value) throws IOException {
        return value.write(out);
    }

    /**
     * @param number Unsigned byte to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeUInt8(int number) throws IOException {
        out.write(number);
        return 1;
    }

    /**
     * @param number Unsigned short to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeUInt16(int number) throws IOException;

    /**
     * @param number Unsigned short to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeUInt24(int number) throws IOException;

    /**
     * @param number Unsigned short to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public abstract int writeUInt32(int number) throws IOException;

    /**
     * @param number Unsigned integer to writeBinary.
     * @param bytes  Number of bytes to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeUnsigned(int number, int bytes) throws IOException {
        switch (bytes) {
            case 4:
                return writeUInt32(number);
            case 3:
                return writeUInt24(number);
            case 2:
                return writeUInt16(number);
            case 1:
                return writeUInt8(number);
        }
        throw new IllegalArgumentException("Unsupported byte count for unsigned: " + bytes);
    }

    /**
     * @param number Signed integer to writeBinary.
     * @param bytes  Number of bytes to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeSigned(int number, int bytes) throws IOException {
        switch (bytes) {
            case 8:
                return writeLong(number);
            case 4:
                return writeInt(number);
            case 2:
                return writeShort((short) number);
            case 1:
                return writeByte((byte) number);
        }
        throw new IllegalArgumentException("Unsupported byte count for signed: " + bytes);
    }

    /**
     * @param number Signed integer to writeBinary.
     * @param bytes  Number of bytes to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeSigned(long number, int bytes) throws IOException {
        switch (bytes) {
            case 8:
                return writeLong(number);
            case 4:
                return writeInt((int) number);
            case 2:
                return writeShort((short) number);
            case 1:
                return writeByte((byte) number);
        }
        throw new IllegalArgumentException("Unsupported byte count for signed: " + bytes);
    }

    /**
     * Write a long number as zigzag encoded to the stream. The least
     * significant bit becomes the sign, and the actual value is mad absolute
     * and shifted one bit. This makes it maximum compressed both when positive
     * and negative.
     *
     * @param number The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeZigzag(int number) throws IOException {
        return writeVarint((number << 1) ^ (number >> 31));
    }

    /**
     * Write a long number as zigzag encoded to the stream. The least
     * significant bit becomes the sign, and the actual value is mad absolute
     * and shifted one bit. This makes it maximum compressed both when positive
     * and negative.
     *
     * @param number The number to writeBinary.
     * @return Number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeZigzag(long number) throws IOException {
        return writeVarint((number << 1) ^ (number >> 63));
    }

    /**
     * Write a signed number as varint (integer with variable number of bytes,
     * determined as part of the bytes themselves.
     *
     * @param varint The number to writeBinary.
     * @return The number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeVarint(int varint) throws IOException {
        int b = 1;
        boolean c = (varint ^ (varint & 0x7f)) != 0;
        out.write((c ? 0x80 : 0x00) | (varint & 0x7f));
        while (c) {
            ++b;
            varint >>>= 7;
            c = (varint ^ (varint & 0x7f)) != 0;
            out.write((c ? 0x80 : 0x00) | (varint & 0x7f));
        }
        return b;
    }

    /**
     * Write a signed number as varint (integer with variable number of bytes,
     * determined as part of the bytes themselves.
     *
     * @param varint The number to writeBinary.
     * @return The number of bytes written.
     * @throws IOException if unable to write to stream.
     */
    public int writeVarint(long varint) throws IOException {
        int b = 1;
        boolean c = (varint ^ (varint & 0x7f)) != 0;
        out.write((c ? 0x80 : 0x00) | (int) (varint & 0x7f));
        while (c) {
            ++b;
            varint >>>= 7;
            c = (varint ^ (varint & 0x7f)) != 0;
            out.write((c ? 0x80 : 0x00) | (int) (varint & 0x7f));
        }
        return b;
    }
}
