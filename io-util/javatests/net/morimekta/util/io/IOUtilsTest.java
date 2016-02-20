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
 */package net.morimekta.util.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for IOUtils functions.
 */
public class IOUtilsTest {
    private static final String lorem =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
            "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
            "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
            "culpa qui officia deserunt mollit anim id est laborum.";

    @Test
    public void testSkipUntil() throws IOException {
        InputStream in = new ByteArrayInputStream(lorem.getBytes(UTF_8));

        // skipUntil has 3 modes: 1 byte, 2-4 bytes (using int buffer), and 5+
        // bytes (using byte[] buffer). The tests is based uppon: skip until
        // given string, then read the next N bytes, and verify that content
        // to be correct. Each call to skipUntil wil propagate upon the previous
        // test.
        assertSkipUntil("ipsum", " ", in);         // 1
        assertSkipUntil("consectetur", ", ", in);  // 2
        assertSkipUntil(" eiusmod", " do", in);    // 3
        assertSkipUntil("dolore", " et ", in);     // 4
        assertSkipUntil(" veniam", "minim", in);   // 5

        // skipping nothing but the separator.
        assertSkipUntil("quis", ", ", in);         // 2
        assertSkipUntil("nostrud", " ", in);       // 1
        assertSkipUntil("rcitation ", "exe", in);  // 3
        assertSkipUntil("mco ", "ulla", in);       // 4
        assertSkipUntil("is", "labor", in);        // 5

        // Skipping with a 0-length array does nothing, and accepts input.
        assertSkipUntil(" nisi ut ", "", in);
    }

    @Test
    public void testSkipUntil_end() throws IOException {
        InputStream in = new ByteArrayInputStream(lorem.getBytes(UTF_8));
        in.mark(lorem.length() + 10);

        assertFalse(IOUtils.skipUntil(in, "_".getBytes(UTF_8)));
        assertEquals(-1, in.read());

        in.reset();
        assertFalse(IOUtils.skipUntil(in, "NO".getBytes(UTF_8)));
        assertEquals(-1, in.read());

        in.reset();
        assertFalse(IOUtils.skipUntil(in, "123".getBytes(UTF_8)));
        assertEquals(-1, in.read());

        in.reset();
        assertFalse(IOUtils.skipUntil(in, "abba".getBytes(UTF_8)));
        assertEquals(-1, in.read());

        in.reset();
        assertFalse(IOUtils.skipUntil(in, "fiver".getBytes(UTF_8)));
        assertEquals(-1, in.read());

        byte[] tmp = new byte[]{'4', '5'};
        byte[] sep = new byte[]{'\0', '4'};

        assertFalse(IOUtils.skipUntil(new ByteArrayInputStream(tmp), sep));
    }

    @Test
    public void testSkipUntil_badInput() throws IOException {
        InputStream in = new ByteArrayInputStream(lorem.getBytes(UTF_8));

        assertException("Null separator given", null, in);

        in = mock(InputStream.class);
        when(in.read()).thenThrow(new IOException("Don't do this!"));

        assertException("Don't do this!", " ", in);
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = IOUtils.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    private void assertSkipUntil(String expected, String skipUntil, InputStream in) throws IOException {
        byte[] skip = skipUntil.getBytes(UTF_8);

        assertTrue(IOUtils.skipUntil(in, skip));
        byte[] next = new byte[expected.length()];
        in.read(next);

        assertEquals(expected, new String(next, UTF_8));
    }

    private void assertException(String expected, String skipUntil, InputStream in) {
        try {
            IOUtils.skipUntil(in, skipUntil == null ? null : skipUntil.getBytes(UTF_8));
        } catch (IOException|NullPointerException e) {
            assertEquals(expected, e.getMessage());
        }
    }
}
