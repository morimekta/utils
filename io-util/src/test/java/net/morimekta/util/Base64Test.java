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

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Tests for Base64 utility.
 */
public class Base64Test {
    private static final String lorem =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
            "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
            "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
            "culpa qui officia deserunt mollit anim id est laborum.";

    @Test
    public void testEncodeSizes() {
        assertEquals("", Base64.encodeToString(new byte[]{}));
        assertEquals("YQ", Base64.encodeToString(Strings.times("a", 1).getBytes(UTF_8)));
        assertEquals("YWE", Base64.encodeToString(Strings.times("a", 2).getBytes(UTF_8)));
        assertEquals("YWFh", Base64.encodeToString(Strings.times("a", 3).getBytes(UTF_8)));
        assertEquals("YWFhYQ", Base64.encodeToString(Strings.times("a", 4).getBytes(UTF_8)));
        assertEquals("YWFhYWE", Base64.encodeToString(Strings.times("a", 5).getBytes(UTF_8)));
        assertEquals("YWFhYWFh", Base64.encodeToString(Strings.times("a", 6).getBytes(UTF_8)));
        assertEquals("YWFhYWFhYQ", Base64.encodeToString(Strings.times("a", 7).getBytes(UTF_8)));
        assertEquals("YWFhYWFhYWE", Base64.encodeToString(Strings.times("a", 8).getBytes(UTF_8)));

        assertEquals("YWFhYWFhYWE", new String(Base64.encode(Strings.times("a", 8).getBytes(UTF_8))));
    }

    @Test
    public void testEncodeBadInput() {
        assertBadEncodeInput("Cannot serialize a null array.", null, 0, 1);
        assertBadEncodeInput("Cannot have negative offset: -1", lorem.getBytes(UTF_8), -1, 1);
        assertBadEncodeInput("Cannot have negative length: -1", lorem.getBytes(UTF_8), 1, -1);
        assertBadEncodeInput("Cannot have offset of 100 and length of 500 with array of length 445",
                             lorem.getBytes(UTF_8), 100, 500);
    }

    @Test
    public void testDecodeSizes() {
        assertEquals("", new String(Base64.decode("")));
        assertEquals("a", new String(Base64.decode("YQ")));
        assertEquals("aa", new String(Base64.decode("YWE")));
        assertEquals("aaa", new String(Base64.decode("YWFh")));
        assertEquals("aaaa", new String(Base64.decode("YWFhYQ")));
        assertEquals("aaaaa", new String(Base64.decode("YWFhYWE")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFhYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode("YWFhYWFhYQ")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE")));
    }

    @Test
    public void testDecodeSizesWithPadding() {
        assertEquals("a", new String(Base64.decode("YQ==")));
        assertEquals("aa", new String(Base64.decode("YWE=")));
        assertEquals("aaa", new String(Base64.decode("YWFh")));
        assertEquals("aaaa", new String(Base64.decode("YWFhYQ==")));
        assertEquals("aaaaa", new String(Base64.decode("YWFhYWE=")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFhYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode("YWFhYWFhYQ==")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE=")));
    }

    @Test
    public void testDecodeSizesWithSpaces() {
        assertEquals("a", new String(Base64.decode("YQ")));
        assertEquals("aa", new String(Base64.decode("Y W E")));
        assertEquals("aaa", new String(Base64.decode("YW Fh")));
        assertEquals("aaaa", new String(Base64.decode("YWFh\nYQ==")));
        assertEquals("aaaaa", new String(Base64.decode("YWFh\r\nYWE=")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFh\tYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode(" YWFhYWFhYQ = =")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE =")));
    }

    @Test
    public void testDecodeBytes() {
        byte[] bytes = "YWFhYWE".getBytes(UTF_8);
        assertEquals("aaaaa", new String(Base64.decode(bytes)));
        assertEquals("aaaaa", new String(Base64.decode(bytes, 0, bytes.length)));
        assertEquals("aaa", new String(Base64.decode(bytes, 0, 4)));
    }

    @Test
    public void testDecodeBad() {
        assertBad("Bad Base64 input character decimal 46 in array position 16",
                  "With punctuation.");
        assertBad("Invalid base64 character \\u003d",
                  "Mis=matched padding=");
        assertBad("Invalid base64 character \\u003d",
                  // Too much padding
                  "YWFhYWFhYWE==");
        assertBad("Input string was null.",
                  null);

        byte[] bytes = "YWFhYWE".getBytes(UTF_8);
        assertBad("Cannot have negative offset: -1", bytes, -1, 3);
        assertBad("Cannot have negative length: -1", bytes, 1, -1);
        assertBad("Source array with length 7 cannot have offset of 0 and process 8 bytes.", bytes, 0, 8);
        assertBad("Cannot decode null source array.", null, 0, 2);
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = Base64.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    private void assertBadEncodeInput(String message, byte[] source, int off, int len) {
        try {
            Base64.encode(source, off, len);
            fail("Missing exception on bad input: " + Binary.wrap(source).toHexString());
        } catch (IllegalArgumentException|NullPointerException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertBad(String message, byte[] encoded, int offset, int len) {
        try {
            Base64.decode(encoded, offset, len);
            fail("Missing exception on bad input: \"" + new String(encoded, UTF_8) + "\"");
        } catch (IllegalArgumentException|NullPointerException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertBad(String message, String encoded) {
        try {
            Base64.decode(encoded);
            fail("Missing exception on bad input: \"" + encoded + "\"");
        } catch (IllegalArgumentException|NullPointerException e) {
            assertEquals(message, e.getMessage());
        }
    }
}
