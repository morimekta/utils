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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Stein Eldar Johnsen
 * @since 28.12.15.
 */
public class Utf8StreamReaderTest {
    // TODO: test unicode U+2A6B2.
    // It is the highest unicode codepoint that also has a valid printable
    // character on most platforms.
    // https://en.wikipedia.org/wiki/List_of_CJK_Unified_Ideographs_Extension_B_(Part_7_of_7)

    @Test
    public void testReady() throws IOException {
        byte[] data = new byte[]{'a', 'b', 'c'};

        Utf8StreamReader reader = new Utf8StreamReader(new ByteArrayInputStream(data));

        assertThat(reader.ready(), is(true));

        reader.close();

        assertThat(reader.ready(), is(false));
    }

    @Test
    public void testRead_ASCII() throws IOException {
        byte[] data = new byte[]{'a', 'b', 'b', 'a', '\t', 'x'};

        Utf8StreamReader reader = new Utf8StreamReader(new ByteArrayInputStream(data));

        char[] out = new char[6];
        assertEquals(6, reader.read(out));
        assertEquals("abba\tx", String.valueOf(out));
    }

    @Test
    public void testRead_UTF8_longString() throws IOException {
        String original = "ü$Ѹ~OӐW| \\rBֆc}}ӂဂG3>㚉EGᖪǙ\\t;\\tၧb}H(πи-ˁ&H59XOqr/,?DרB㡧-Үyg9i/?l+ႬЁjZr=#DC+;|ԥ'f9VB5|8]cOEሹrĐaP.ѾҢ/^nȨޢ\\\"u";
        byte[] data = original.getBytes(UTF_8);
        char[] out = new char[data.length];

        Utf8StreamReader reader = new Utf8StreamReader(new ByteArrayInputStream(data));

        assertEquals(original.length(), reader.read(out));
        assertEquals(original, String.valueOf(out, 0, original.length()));
    }

    @Test
    public void testRead_UTF8_singleRead() throws IOException {
        String original = "ü$Ѹ~";
        byte[] data = original.getBytes(UTF_8);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        assertEquals('ü', (char) new Utf8StreamReader(bais).read());
        assertEquals('$', (char) new Utf8StreamReader(bais).read());
        assertEquals('Ѹ', (char) new Utf8StreamReader(bais).read());
        assertEquals('~', (char) new Utf8StreamReader(bais).read());
    }

    @Test
    public void testReadSurrogatePair() throws IOException {
        byte[] src = "輸".getBytes(UTF_8);

        ByteArrayInputStream bais = new ByteArrayInputStream(src);

        Utf8StreamReader reader = new Utf8StreamReader(bais);

        char a = (char) reader.read();
        char b = (char) reader.read();

        assertThat(reader.read(), is(-1));

        assertTrue(Character.isHighSurrogate(a));
        assertTrue(Character.isLowSurrogate(b));
        assertEquals(195039, Character.toCodePoint(a, b));

        reader = new Utf8StreamReader(new ByteArrayInputStream(new byte[]{-4, -81, -81, -81, -81, -81}));
        assertThat(Character.toCodePoint((char) reader.read(),
                                         (char) reader.read()),
                   is(-4260881));
        reader = new Utf8StreamReader(new ByteArrayInputStream(new byte[]{-8, -81, -81, -81, -81}));
        assertThat(Character.toCodePoint((char) reader.read(),
                                         (char) reader.read()),
                   is(-54592529));
    }

    @Test
    public void testBadUnicode() throws IOException {
        assertBadUnicode_IO(new byte[]{-16, -81, -89},
                            "End of stream inside utf-8 encoded entity.");
        assertBadUnicode_UE(new byte[]{-16, 81, 0, 0, 0},
                            "Unexpected non-entity utf-8 char in entity extra bytes: 0x51");
        assertBadUnicode_UE(new byte[]{-81},
                            "Unexpected utf-8 entity char: 0xaf");
        assertBadUnicode_UE(new byte[]{-2},
                            "Unexpected utf-8 non-entity char: 0xfe");
    }

    private void assertBadUnicode_IO(byte[] src, String message) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(src);
            new Utf8StreamReader(bais).read();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is(message));
        }
    }

    private void assertBadUnicode_UE(byte[] src, String message) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(src);
            new Utf8StreamReader(bais).read();
            fail("no exception");
        } catch (UnsupportedEncodingException e) {
            assertThat(e.getMessage(), is(message));
        }
    }
}
