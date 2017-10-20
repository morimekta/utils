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

import java.util.Objects;

/**
 * A slice of a byte array.
 *
 * Note that all the helper methods assume UTF_8 encoding.
 */
public class CharSlice implements Comparable<CharSlice>, Stringable, CharSequence {
    protected final char[] fb;
    protected final int    off;
    protected final int    len;

    /**
     * Create a slice instance. The slice is only meant to be internal state
     * immutable, and not representing an immutable byte content.
     *
     * @param fb The buffer to wrap.
     * @param off The start offset to wrap.
     * @param len The length to represent.
     */
    @SuppressFBWarnings(justification = "Wrapping of char array is intentional.",
                        value = {"EI_EXPOSE_REP2"})
    public CharSlice(char[] fb, int off, int len) {
        if (off < 0) throw new IllegalArgumentException("Negative offset: " + off);
        if (off + len > fb.length) throw new IllegalArgumentException("Too large slice: " + off + "+" + len + " of " + fb.length);
        this.fb = fb;
        this.off = off;
        this.len = len;
    }

    /**
     * Get the offset of the buffer.
     *
     * @return The slice offset.
     */
    public final int offset() {
        return off;
    }

    /**
     * Get the total length of the slice.
     *
     * @return The slice length.
     */
    @Override
    public final int length() {
        return len;
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return substring(i, i1);
    }

    /**
     * Get the whole slice as a string.
     *
     * @return Slice decoded as UTF_8 string.
     */
    @Override
    public final String asString() {
        return new String(fb, off, len);
    }

    /**
     * Create a substring slice based on the current slice.
     *
     * @param start The internal start position, relative to the slice's offset.
     * @param end The internal end position, relative to the slice's offset. If end
     *            is negative, then it is relative to the slice's end position.
     * @return The substring slice.
     */
    public final CharSlice substring(int start, int end) {
        if (start < 0 || end > len || end < -len) {
            throw new IllegalArgumentException(
                    String.format("[%d,%d] of slice length %d is not valid.", start, end, len));
        }
        int l = end < 0 ? (len - start) + end : end - start;
        if (l < 0 || l > (len - start)) {
            throw new IllegalArgumentException(
                    String.format("[%d,%d] of slice length %d is not valid.", start, end, len));
        }
        return new CharSlice(fb, off + start, l);
    }

    /**
     * Get character at slice relative position.
     * @param i The position to get. If negative is relative to the slice's end position.
     * @return The char at given position.
     */
    @Override
    public final char charAt(int i) {
        if (i < -len || len <= i) {
            throw new IllegalArgumentException(
                    String.format("position %d of slice length %d is not valid.", i, len));
        }
        if (i < 0) {
            i = len + i;
        }
        return fb[off + i];
    }

    /**
     * Get the whole slice as a simple integer.
     *
     * @return Integer long value.
     */
    public final long parseInteger() {
        int pos = off, radix = 10;
        boolean neg = false;
        if (len > 2 && charAt(0) == '0' && charAt(1) == 'x') {
            pos += 2;
            radix = 16;
        } else if (len > 1 && charAt(0) == '0') {
            pos += 1;
            radix = 8;
        } else if (len > 1 && charAt(0) == '-') {
            neg = true;
            pos += 1;
        }
        long res = 0;
        for (; pos < off + len; ++pos) {
            res *= radix;
            res += validate(fb[pos], valueOfHex(fb[pos]), radix);
        }
        return neg ? -res : res;
    }

    /**
     * Get the whole slice as a real number.
     *
     * @return Real double value.
     */
    public final double parseDouble() {
        return Double.parseDouble(asString());
    }

    /**
     * Checks if the slice is equal to given byte array.
     *
     * @param a The array to compare with.
     * @return True if equal.
     */
    public final boolean strEquals(char[] a) {
        return strEquals(a, 0, a.length);
    }

    /**
     * Checks if the slice is equal to a portion of a given byte array.
     *
     * @param a The array to compare with.
     * @param aOff The offset to compare with in the array.
     * @param aLen The length to compare with.
     * @return True if equal.
     */
    public final boolean strEquals(char[] a, int aOff, int aLen) {
        if (aLen != len) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (a[aOff + i] != fb[off + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any of the provided bytes is contained in the slice.
     *
     * @param a Bytes to find in the slice.
     * @return True if any of the bytes were found.
     */
    public final boolean containsAny(char... a) {
        for (int i = 0; i < len; ++i) {
            for (char b : a) {
                if (b == fb[off + i]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the byte array is contained in the slice.
     *
     * @param a The byte array to find.
     * @return True if the byte array was found.
     */
    public final boolean contains(char[] a) {
        final int last_pos = off + len - a.length;
        outer:
        for (int pos = off; pos <= last_pos; ++pos) {
            for (int a_off = 0; a_off < a.length; ++a_off) {
                if (a[a_off] != fb[pos + a_off]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a single byte can be found in the slice.
     *
     * @param a The byte to find.
     * @return True of the byte was found.
     */
    public final boolean contains(char a) {
        for (int i = off; i < (off + len); ++i) {
            if (fb[i] == a) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new String(fb, off, len);
    }

    @Override
    public int hashCode() {
        return Objects.hash(off, len);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }
        CharSlice other = (CharSlice) o;
        return other.fb == fb &&
               other.off == off &&
               other.len == len;
    }

    /**
     * Compare slice with other slice.
     *
     * Slice ordering:
     *  - Firstly ordered by start (offset) position.
     *  - Secondly ordered by reverse length (longest slice first).
     *
     * Result is undefined of the two slices point to different byte buffers.
     *
     * @param o The other slice.
     * @return Compared value.
     */
    @Override
    public int compareTo(CharSlice o) {
        if (o.off != off) {
            return Integer.compare(off, o.off);
        }
        return Integer.compare(o.len, len);
    }

    private static int validate(char c, int value, int radix) {
        if (value < 0 || value >= radix) {
            if (Strings.isConsolePrintable(c)) {
                throw new IllegalArgumentException(
                        String.format("Char \'%c\' not valid value for radix %d",
                                      c,
                                      radix));
            } else {
                throw new IllegalArgumentException(
                        String.format("Char \'\\u%04x\' not valid value for radix %d",
                                      (int) c,
                                      radix));
            }
        }
        return value;
    }

    private static int valueOfHex(char b) {
        if (b <= '9') {
            return (b - '0');
        }
        if (b >= 'a') {
            return (b - 'a') + 10;
        }
        return (b - 'A') + 10;
    }
}
