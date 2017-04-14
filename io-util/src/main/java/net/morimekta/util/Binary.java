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
package net.morimekta.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.morimekta.util.io.BigEndianBinaryReader;
import net.morimekta.util.io.BigEndianBinaryWriter;
import net.morimekta.util.io.BinaryReader;
import net.morimekta.util.io.BinaryWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Simplistic byte sequence wrapper with lots of convenience methods. Used to
 * wrap byte arrays for the binary data type.
 */
public class Binary implements Comparable<Binary>, Stringable, Serializable {
    private final byte[] bytes;

    /**
     * Create a binary instance that wraps a created byte array. Exposed so
     * it can be used in places where constructors are expected.
     *
     * @param bytes The byte array to wrap.
     */
    @SuppressFBWarnings(justification = "Wrapping of byte array is intentional.",
                        value = {"EI_EXPOSE_REP2"})
    public Binary(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Convenience method to wrap a byte array into a byte sequence.
     *
     * @param bytes Bytes to wrap.
     * @return The wrapped byte sequence.
     */
    public static Binary wrap(byte[] bytes) {
        return new Binary(bytes);
    }

    /**
     * Convenience method to copy a byte array into a byte sequence.
     *
     * @param bytes Bytes to wrap.
     * @return The wrapped byte sequence.
     */
    public static Binary copy(byte[] bytes) {
        return copy(bytes, 0, bytes.length);
    }

    /**
     * Convenience method to copy a part of a byte array into a byte sequence.
     *
     * @param bytes Bytes to wrap.
     * @param off   Offset in source bytes to start reading from.
     * @param len   Number of bytes to copy.
     * @return The wrapped byte sequence.
     */
    public static Binary copy(byte[] bytes, int off, int len) {
        byte[] cpy = new byte[len];
        System.arraycopy(bytes, off, cpy, 0, len);
        return wrap(cpy);
    }

    /**
     * Method to create a Binary with 0 bytes.
     *
     * @return Empty Binary object.
     */
    public static Binary empty() {
        return new Binary(new byte[0]);
    }

    /**
     * Get the length of the backing array.
     *
     * @return Byte count.
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Get a copy of the backing array.
     *
     * @return The copy.
     */
    public byte[] get() {
        byte[] cpy = new byte[bytes.length];
        System.arraycopy(bytes, 0, cpy, 0, bytes.length);
        return cpy;
    }

    /**
     * Get a copy of the backing array.
     *
     * @param into Target ro copy into.
     * @return Number of bytes written.
     */
    public int get(byte[] into) {
        int len = Math.min(into.length, bytes.length);
        System.arraycopy(bytes, 0, into, 0, len);
        return len;
    }

    /**
     * Decode base64 string and wrap the result in a byte sequence.
     *
     * @param base64 The string to decode.
     * @return The resulting sequence.
     */
    public static Binary fromBase64(String base64) {
        byte[] arr = Base64.decode(base64);
        return Binary.wrap(arr);
    }

    /**
     * Get the sequence encoded as base64.
     *
     * @return The encoded string.
     */
    public String toBase64() {
        return Base64.encodeToString(bytes);
    }

    /**
     * Get a binary representation of a UUID. The UUID binary representation is
     * equivalent to the hexadecimal representation of the UUID (sans dashes).
     * See {@link UUID#toString()} and {@link UUID#fromString(String)}.
     *
     * @param uuid The UUID to make binary representation of.
     * @return The Binary representation.
     * @throws IllegalArgumentException If a null UUID is given.
     */
    public static Binary fromUUID(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Null UUID for binary");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(16);
        try (BinaryWriter w = new BigEndianBinaryWriter(buf)) {
            w.writeLong(uuid.getMostSignificantBits());
            w.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException ignore) {
            // Actually not possible, just hiding exception
            throw new UncheckedIOException(ignore);
        }
        return wrap(buf.toByteArray());
    }

    /**
     * Get a UUID from the binary data.The UUID binary representation is
     * equivalent to the hexadecimal representation of the UUID (sans dashes).
     * See {@link UUID#toString()} and {@link UUID#fromString(String)}.
     *
     * @return The UUID representation of the 16 bytes.
     * @throws IllegalStateException If the binary does not have the correct
     *                               size for holding a UUID, 16 bytes.
     */
    public UUID toUUID() {
        if (length() != 16) {
            throw new IllegalStateException("Length not compatible with UUID: " + length() + " != 16");
        }
        try (BinaryReader reader = new BigEndianBinaryReader(getInputStream())) {
            long mostSig = reader.expectLong();
            long leastSig = reader.expectLong();
            return new UUID(mostSig, leastSig);
        } catch (IOException ignore) {
            // Actually not possible, just hiding exception
            throw new UncheckedIOException(ignore);
        }
    }

    /**
     * Parse a hex string as bytes.
     *
     * @param hex The hex string.
     * @return The corresponding bytes.
     */
    public static Binary fromHexString(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Illegal hex string length: " + hex.length());
        }
        final int len = hex.length() / 2;
        final byte[] out = new byte[len];
        for (int i = 0; i < len; ++i) {
            int pos = i * 2;
            String part = hex.substring(pos, pos + 2);
            out[i] = (byte) Integer.parseInt(part, 16);
        }
        return Binary.wrap(out);
    }

    /**
     * Make a hex string from a byte array.
     *
     * @return The hex string.
     */
    public String toHexString() {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    /**
     * Get a byte buffer wrapping the binary data.
     *
     * @return A byte buffer.
     */
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(get());
    }

    /**
     * Get an input stream that reads from the stored bytes.
     *
     * @return An input stream.
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Read a binary buffer from input stream.
     *
     * @param in  Input stream to read.
     * @param len Number of bytes to read.
     * @return The read bytes.
     *
     * @throws IOException If unable to read completely what's expected.
     */
    public static Binary read(InputStream in, int len) throws IOException {
        byte[] bytes = new byte[len];
        int pos = 0;
        while (pos < len) {
            int i = in.read(bytes, pos, len - pos);
            if (i <= 0) {
                throw new IOException("End of stream before complete buffer read.");
            }
            pos += i;
        }
        return wrap(bytes);
    }

    /**
     * Write bytes to output stream.
     *
     * @param out Stream to write to.
     * @return Number of bytes written.
     * @throws IOException When unable to write to stream.
     */
    public int write(OutputStream out) throws IOException {
        out.write(bytes);
        return bytes.length;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }
        Binary other = (Binary) o;

        return Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public int compareTo(Binary other) {
        final int c = Math.min(bytes.length, other.bytes.length);
        for (int i = 0; i < c; ++i) {
            if (bytes[i] != other.bytes[i]) {
                return bytes[i] > other.bytes[i] ? 1 : -1;
            }
        }
        if (bytes.length == other.bytes.length) {
            return 0;
        }
        return bytes.length > other.bytes.length ? 1 : -1;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("binary(");
        for (byte b : bytes) {
            int i = b < 0 ? 0x100 + b : b;
            buffer.append(String.format(Locale.ENGLISH, "%02x", i));
        }
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public String asString() {
        return "[" + toBase64() + "]";
    }
}
