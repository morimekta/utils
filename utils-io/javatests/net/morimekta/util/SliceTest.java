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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Stein Eldar Johnsen
 * @since 16.01.16.
 */
public class SliceTest {
    private byte[] data;

    @Before
    public void setUp() {
        data = "This is a -123.45 long \"test ł→Þħ úñí©øð€.\"".getBytes(UTF_8);
    }

    @Test
    public void testSlice() {
        Slice slice = new Slice(data, 0, 7);

        assertEquals(7, slice.length());
        assertEquals("This is", slice.asString());
        assertEquals("slice([0..7>/56)", slice.toString());
        assertEquals('s', slice.charAt(6));

        Slice other = new Slice(data, 0, 22);
        assertEquals(22, other.length());
        assertEquals("This is a -123.45 long", other.asString());
        assertEquals("slice([0..22>/56)", other.toString());
        assertEquals('s', other.charAt(6));

        assertNotEquals(slice, other);

        other = new Slice(data, 0, 7);

        assertEquals(slice, other);
    }

    @Test
    public void testParseInteger() {
        Slice slice = new Slice(data, 11, 3);

        assertEquals(3, slice.length());
        assertEquals("123", slice.asString());
        assertEquals("slice([11..14>/56)", slice.toString());
        assertEquals('1', slice.charAt(0));

        assertEquals(123L, slice.parseInteger());
        assertEquals(123.0, slice.parseDouble(), 0.0001);

        slice = new Slice(data, 10, 4);
        assertEquals(-123L, slice.parseInteger());
        assertEquals(-123.0, slice.parseDouble(), 0.0001);

        String hex = "0x7f";
        slice = new Slice(hex.getBytes(), 0, 4);
        assertEquals(127L, slice.parseInteger());

        hex = "0x7F";
        slice = new Slice(hex.getBytes(), 0, 4);
        assertEquals(127L, slice.parseInteger());

        String oct = "0757";
        slice = new Slice(oct.getBytes(), 0, 4);
        assertEquals(495L, slice.parseInteger());

        try {
            new Slice("boo".getBytes(), 0, 3).parseInteger();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Char 'b' not valid value for radix 10", e.getMessage());
        }

        try {
            new Slice("099".getBytes(), 0, 3).parseInteger();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Char '9' not valid value for radix 8", e.getMessage());
        }

        try {
            new Slice("7f".getBytes(), 0, 2).parseInteger();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Char 'f' not valid value for radix 10", e.getMessage());
        }

        try {
            new Slice("0 ".getBytes(), 0, 2).parseInteger();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Char ' ' not valid value for radix 8", e.getMessage());
        }

        try {
            new Slice("0\033".getBytes(), 0, 2).parseInteger();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Char '0x1b' not valid value for radix 8", e.getMessage());
        }
    }

    @Test
    public void testParseDouble() {
        Slice slice = new Slice(data, 11, 6);

        assertEquals(6, slice.length());
        assertEquals("123.45", slice.asString());
        assertEquals("slice([11..17>/56)", slice.toString());
        assertEquals('1', slice.charAt(0));

        assertEquals(123.45, slice.parseDouble(), 0.0001);
    }

    @Test
    public void testSubstring() {
        Slice slice = new Slice(data, 11, 6);

        assertEquals("123", slice.substring(0, 3).asString());
        assertEquals("23.4", slice.substring(1, -1).asString());

        // substring cannot cut outside the boundaries of the original, only
        // trim down, even though it would have become a valid slice.
        try {
            slice.substring(-1, 2);
            fail();
        } catch (IllegalArgumentException e) {
            // pass()
        }
        try {
            slice.substring(0, 10);
            fail();
        } catch (IllegalArgumentException e) {
            // pass()
        }

        try {
            slice.substring(3, -4);
            fail();
        } catch (IllegalArgumentException e) {
            // pass()
        }
    }

    @Test
    public void testCharAt() {
        Slice slice = new Slice(data, 11, 6);

        assertEquals('1', slice.charAt(0));
        assertEquals('.', slice.charAt(-3));

        assertEquals('5', slice.charAt(5));
        assertEquals('1', slice.charAt(-6));

        try {
            slice.charAt(-7);
            fail();
        } catch (IllegalArgumentException e) {
            // pass()
        }

        try {
            slice.charAt(6);
            fail();
        } catch (IllegalArgumentException e) {
            // pass()
        }
    }

    @Test
    public void testStrEquals() {
        Slice slice = new Slice(data, 11, 6);

        byte[] eq = new byte[]{'1', '2', '3', '.', '4', '5'};
        byte[] pre = new byte[]{'1', '2', '3'};
        byte[] no = new byte[]{'1', '2', '4', '.', '3', '5'};

        assertTrue(slice.strEquals(eq));
        assertFalse(slice.strEquals(eq, 0, 3));
        assertFalse(slice.strEquals(pre));
        assertFalse(slice.strEquals(no));
    }

    @Test
    public void testContainsAny() {
        Slice slice = new Slice(data, 11, 6);

        assertTrue(slice.containsAny((byte) '3', (byte) 'b', (byte) '7'));
        assertTrue(slice.containsAny((byte) 'b', (byte) '7', (byte) '3'));
        assertFalse(slice.containsAny((byte) 'b', (byte) 'c', (byte) 'd'));
    }

    @Test
    public void testContains_byte() {
        Slice slice = new Slice(data, 11, 6);

        assertTrue(slice.contains((byte) '.'));
        assertTrue(slice.contains((byte) '5'));
        assertFalse(slice.contains((byte) 'b'));
    }

    @Test
    public void testContains_byteArray() {
        Slice slice = new Slice(data, 11, 6);

        byte[] eq = new byte[]{'1', '2', '3', '.', '4', '5'};
        byte[] pre = new byte[]{'1', '2', '3'};
        byte[] mid = new byte[]{'3', '.', '4'};
        byte[] post = new byte[]{'.', '4', '5'};

        byte[] no1 = new byte[]{'1', '2', '4', '.', '3', '5'};
        byte[] no2 = new byte[]{'b', 'c'};

        assertTrue(slice.contains(eq));
        assertTrue(slice.contains(pre));
        assertTrue(slice.contains(mid));
        assertTrue(slice.contains(post));

        assertFalse(slice.contains(no1));
        assertFalse(slice.contains(no2));
    }

    @Test
    public void testHashCode() {
        Slice slice = new Slice(data, 11, 6);

        int hc1 = slice.hashCode();
        data[12] = 'c';
        // Hash code is not reading data content, only position.
        assertEquals(hc1, slice.hashCode());
    }

    @Test
    public void testEquals() {
        Slice slice = new Slice(data, 11, 6);
        Slice equal = new Slice(data, 11, 6);

        Slice diffOff = new Slice(data, 5, 6);
        Slice diffLen = new Slice(data, 11, 3);
        Slice diffFb = new Slice(Arrays.copyOf(data, data.length), 11, 6);

        assertTrue(slice.equals(equal));
        assertTrue(slice.equals(slice));
        assertFalse(slice.equals(null));
        assertFalse(slice.equals(new Object()));
        assertFalse(slice.equals(diffFb));
        assertFalse(slice.equals(diffLen));
        assertFalse(slice.equals(diffOff));
    }

    @Test
    public void testCompareTo() {
        Slice a = new Slice(data, 11, 6);
        Slice a2 = new Slice(data, 11, 6);
        Slice b = new Slice(data, 11, 5);
        Slice c = new Slice(data, 14, 3);

        assertEquals(0, a.compareTo(a2));

        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, b.compareTo(c));

        assertEquals(1, b.compareTo(a));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
    }
}
