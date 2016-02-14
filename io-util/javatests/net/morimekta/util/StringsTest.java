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

import net.morimekta.util.io.Utf8StreamReader;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Stein Eldar Johnsen
 * @since 18.10.15
 */
public class StringsTest {
    byte[] mArray;
    byte[] mArray_withEscaping;
    byte[] mArray_withNullbyte;
    byte[] mArray_withUtf8;
    String mString;
    String mString_withEscaping;
    String mString_withUtf8;

    @Before
    public void setUp() {
        mArray = new byte[]{'1', '2', '3'};
        mArray_withNullbyte = new byte[]{'1', '2', '3', '\0'};
        mArray_withEscaping = new byte[]{'1', '2', '3', '\t'};
        mArray_withUtf8 = new byte[]{'1', '2', '3', (byte) 0xc3, (byte) 0xa1};

        mString = "123";
        mString_withEscaping = "123\t";
        mString_withUtf8 = "123á";
    }

    @Test
    public void testJoin() {
        assertEquals("a,b", Strings.join(",", 'a', 'b'));
        assertEquals("a,b", Strings.join(",", "a", "b"));
        List<String> tmp = new LinkedList<>();
        Collections.addAll(tmp, "a", "b");
        assertEquals("a;b", Strings.join(";", tmp));
    }

    @Test
    public void testIsInteger() {
        assertTrue(Strings.isInteger("0"));
        assertTrue(Strings.isInteger("1"));
        assertTrue(Strings.isInteger("1234567890"));
        assertTrue(Strings.isInteger("-1234567890"));

        assertFalse(Strings.isInteger("+2"));
        assertFalse(Strings.isInteger("beta"));
        assertFalse(Strings.isInteger("    -5 "));
        assertFalse(Strings.isInteger("0x44"));  // hex not supported.
        assertFalse(Strings.isInteger(""));
    }

    @Test
    public void testReadString() throws IOException {
        assertEquals(mString, TSU_readString(mArray));
        assertEquals(mString, TSU_readString(mArray_withNullbyte));
        assertEquals(mString_withEscaping, TSU_readString(mArray_withEscaping));
        assertEquals(mString_withUtf8, TSU_readString(mArray_withUtf8));
    }

    @Test(expected = IOException.class)
    public void testReadString_ioException() throws IOException {
        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                // TODO Auto-generated method stub
                throw new IOException();
            }
        };
        fail("unexpected output: " + Strings.readString(is));
    }

    @Test
    public void testReadString_partialRead() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 'c', '\0', 'x', 'y', 'z'};
        // BufferedInputStream supports marks.
        InputStream is = new ByteArrayInputStream(buffer);

        assertEquals("abc", Strings.readString(is));
        assertEquals("xyz", Strings.readString(is));
    }

    @Test
    public void testReadString_partialReadWithTerminator() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 'c', '\r', '\n', 'x', 'y', 'z'};
        // BufferedInputStream supports marks.
        InputStream is = new ByteArrayInputStream(buffer);

        assertEquals("abc", Strings.readString(is, "\r\n"));
        assertEquals("xyz", Strings.readString(is, "\r\n"));
    }

    @Test
    public void testReadString_partialReader() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 'c', '\0', 'x', 'y', 'z'};
        // BufferedInputStream supports marks.
        Reader is = new Utf8StreamReader(new ByteArrayInputStream(buffer));

        assertEquals("abc", Strings.readString(is));
        assertEquals("xyz", Strings.readString(is));
    }

    @Test
    public void testReadString_partialReaderWithTerminator() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 'c', '\r', '\n', 'x', 'y', 'z'};
        // BufferedInputStream supports marks.
        Reader is = new Utf8StreamReader(new ByteArrayInputStream(buffer));

        assertEquals("abc", Strings.readString(is, "\r\n"));
        assertEquals("xyz", Strings.readString(is, "\r\n"));
    }

    @Test
    public void testTimes() {
        assertEquals("bbbbb", Strings.times("b", 5));
    }

    @Test
    public void testCamelCase() {
        assertEquals("", Strings.camelCase("", ""));
        assertEquals("getMyThing", Strings.camelCase("get", "my_thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my.thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my-thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my...thing"));
        assertEquals("MyThing", Strings.camelCase("", "my_thing"));
    }

    @Test
    public void testC_case() {
        assertEquals("", Strings.c_case("", ""));
        assertEquals("", Strings.c_case("", "", ""));

        assertEquals("get_my_thing_now", Strings.c_case("get_", "MyThing", "_now"));
        assertEquals("get_abbr_now", Strings.c_case("get_", "ABBR", "_now"));
        assertEquals("get_pascal_is_not_nice_now", Strings.c_case("get_", "Pascal_Is_Not_Nice", "_now"));

        // TODO: This case should be possible to split to: "get_abbr_and_more_now".
        assertEquals("get_abbrand_more_now", Strings.c_case("get_", "ABBRAndMore", "_now"));
    }

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Strings> c = Strings.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());

        c.setAccessible(true);
        c.newInstance();  // to make code coverage 100%.
        c.setAccessible(false);
    }

    private String TSU_readString(byte[] bytes) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        return Strings.readString(is);
    }
}
