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
package net.morimekta.util.json;

import net.morimekta.util.Binary;
import net.morimekta.util.Strings;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the JSON tokenizer.
 */
public class JsonTokenizerTest {
    private static final String lorem =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et \n" +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut \n" +
            "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse \n" +
            "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in \n" +
            "culpa qui officia deserunt mollit anim id est laborum.\n";

    @Test
    public void testExpect() throws IOException, JsonException {
        JsonTokenizer tokenizer = makeTokenizer("a, \t\n\"abba\"");

        JsonToken a = tokenizer.expect("a");
        assertEquals("a", a.asString());

        JsonToken comma = tokenizer.expect("comma");
        assertTrue(comma.isSymbol(','));

        JsonToken abba = tokenizer.expect("abba");

        assertEquals("\"abba\"", abba.asString());
        assertEquals("abba", abba.decodeJsonLiteral());
    }

    @Test
    public void testExpectBad() {
        assertBad("Illegal character in JSON structure: '\\u00e6'", "æ");
        assertBad("Expected __string__: Got end of file", "");
    }


    private void assertBad(String message, String str) {
        try {
            makeTokenizer(str).expect("__string__");
            fail("no exception on bad string: \"" + str + "\"");
        } catch (JsonException|IOException e) {
            if (!e.getMessage().equals(message)) {
                e.printStackTrace();
            }
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testExpectString() throws IOException, JsonException {
        JsonTokenizer tokenizer = makeTokenizer("\"a long \\n\\tstring with escapes\"");

        JsonToken token = tokenizer.expectString("string");

        assertEquals("\"a long \\n\\tstring with escapes\"", token.asString());
        assertEquals("a long \n\tstring with escapes", token.decodeJsonLiteral());
    }

    @Test
    public void testExpectLongString() throws IOException, JsonException {
        String merged = Strings.times(lorem, 500);
        String escaped = Strings.escape(merged);

        String total = "{ \"field\": \"" + escaped + "\" }";
        JsonTokenizer tokenizer = makeTokenizer(total);

        tokenizer.expectSymbol("message start", '{');
        assertEquals("field", tokenizer.expectString("field name").decodeJsonLiteral());
        tokenizer.expectSymbol("", ':');

        JsonToken longString = tokenizer.expectString("long string literal");
        int len = longString.asString().length();
        assertEquals('\"', longString.asString().charAt(0));
        assertEquals('\"', longString.asString().charAt(len - 1));

        String longLiteral = longString.decodeJsonLiteral();

        String[] a = merged.split("\n");
        String[] b = longLiteral.split("\n");
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; ++i) {
            assertEquals(Binary.wrap(a[i].getBytes()), Binary.wrap(b[i].getBytes()));
        }

        assertThat(tokenizer.restOfLine(), is("}"));
    }

    @Test
    public void testBadString() {
        assertBadString("Expected __string__ (string literal): Got end of file", "");
        assertBadString("Expected __string__ (string literal): but found '11'", "11");
        assertBadString("Unexpected end of stream in string literal", "\"11");
        assertBadString("Unexpected newline in string literal", "\"11\n\"");
    }

    private void assertBadString(String message, String str) {
        try {
            makeTokenizer(str).expectString("__string__");
            fail("no exception on bad string: \"" + str + "\"");
        } catch (JsonException | IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testExpectNumber() throws IOException, JsonException {
        JsonTokenizer tokenizer = makeTokenizer("12345678901234567");

        JsonToken token = tokenizer.expectNumber("number");
        assertEquals(12345678901234567L, token.parseInteger());

        tokenizer = makeTokenizer("0.4");
        token = tokenizer.expectNumber("number");
        assertEquals(0.4, token.parseDouble(), 0.001);

        tokenizer = makeTokenizer("0.4e-5,");
        token = tokenizer.expectNumber("number");
        assertEquals(4e-6, token.parseDouble(), 0.001);

        tokenizer = makeTokenizer("0.4e-5");
        token = tokenizer.expectNumber("number");
        assertEquals(4e-6, token.parseDouble(), 0.001);

        tokenizer = makeTokenizer("-0.4");
        token = tokenizer.expectNumber("number");
        assertEquals(-0.4, token.parseDouble(), 0.001);

        tokenizer = makeTokenizer("-.4");
        token = tokenizer.expectNumber("number");
        assertEquals(-0.4, token.parseDouble(), 0.001);

        tokenizer = makeTokenizer("-4.");
        token = tokenizer.expectNumber("number");
        assertEquals(-4.0, token.parseDouble(), 0.001);
    }

    @Test
    public void testBadNumber() {
        assertBadNumber("Expected __string__ (number): Got end of file", "");
        assertBadNumber("Expected __string__ (number): but found 'false'", "false");
        assertBadNumber("Expected __string__ (number): but found '\"string\"'", "\"string\"");
        assertBadNumber("Wrongly terminated JSON number: '1x'", "1x");
        assertBadNumber("Negative indicator without number", "-");
        assertBadNumber("No decimal after negative indicator", "-,");
        assertBadNumber("Badly terminated JSON exponent: '1e'", "1e");
        assertBadNumber("Badly terminated JSON exponent: '1e-'", "1e-");
        assertBadNumber("Badly terminated JSON exponent: '1eb'", "1eb");
    }

    private void assertBadNumber(String message, String str) {
        try {
            makeTokenizer(str).expectNumber("__string__");
            fail("no exception on bad string: \"" + str + "\"");
        } catch (JsonException|IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testExpectSymbol() throws IOException, JsonException {
        JsonTokenizer tokenizer = makeTokenizer("{},:[]");

        assertEquals('{', tokenizer.expectSymbol("one", '{'));
        assertEquals('}', tokenizer.expectSymbol("two", '{', ':', ',', '}'));
        assertEquals(',', tokenizer.expectSymbol("thr", '[', ',', ']'));
        assertEquals(':', tokenizer.expectSymbol("fou", ':'));
        assertEquals('[', tokenizer.expectSymbol("fiv", '[', ']'));
        assertEquals(']', tokenizer.expectSymbol("six", '[', ']'));
    }

    @Test
    public void testExpectBadSymbol() {
        assertBadSymbol("No symbols to match.", "[");
        assertBadSymbol("Expected __string__ (']'): but found '['", "[", ']');
        assertBadSymbol("Expected __string__ (']'): Got end of file", "", ']');
        assertBadSymbol("Expected __string__ (one of [']', '}']): but found '['", "[", ']', '}');
        assertBadSymbol("Expected __string__ (one of [']', '}']): Got end of file", "", ']', '}');
    }

    private void assertBadSymbol(String message, String str, char... symbols) {
        try {
            makeTokenizer(str).expectSymbol("__string__", symbols);
            fail("no exception on bad symbol: \"" + str + "\", not matching " + Arrays.toString(symbols));
        } catch (JsonException|IOException|IllegalArgumentException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testPeek() throws IOException, JsonException {
        JsonTokenizer tokenizer = makeTokenizer("{\n" +
                                                "    \"first\": \"value\"\n" +
                                                "}");
        assertTrue(tokenizer.peek("Fail").isSymbol(JsonToken.kMapStart));
        assertTrue(tokenizer.next().isSymbol(JsonToken.kMapStart));
        assertEquals("first", tokenizer.peek("Fail").decodeJsonLiteral());
        assertEquals("first", tokenizer.expect("Fail").decodeJsonLiteral());

        tokenizer.expectSymbol("Fail", JsonToken.kKeyValSep);
        tokenizer.expectString("Fail");
        assertTrue(tokenizer.expect("Fail").isSymbol(JsonToken.kMapEnd));

        try {
            tokenizer.peek("__message__");
            fail("No exception on peek on end.");
        } catch (JsonException e) {
            assertEquals("Expected __message__: Got end of file", e.getMessage());
            assertEquals("JSON Error on line 3: Expected __message__: Got end of file\n" +
                         "}\n" +
                         "-^", e.asString());
        }
    }

    @Test
    public void testGetLine() throws IOException {
        JsonTokenizer tokenizer = makeTokenizer("{\n" +
                                                "    \"first\": \"value\",\n" +
                                                "    \"second\": 12345,\n" +
                                                "}");
        try {
            tokenizer.expectSymbol("", '{');
            tokenizer.expectNumber("__number__");

            fail("No exception on bad expectation.");
        } catch (JsonException e) {
            assertEquals("Expected __number__ (number): but found '\"first\"'", e.getMessage());
            assertEquals(2, e.getLineNo());
            assertEquals("    \"first\": \"value\",", e.getLine());
            assertEquals(5, e.getLinePos());
            assertEquals(7, e.getLen());
            assertEquals("JSON Error on line 2: Expected __number__ (number): but found '\"first\"'\n" +
                         "    \"first\": \"value\",\n" +
                         "----^^^^^^^", e.asString().replaceAll("\\r\\n", "\n"));

            UncheckedJsonException ue = new UncheckedJsonException(e);
            assertEquals(e.getMessage(), ue.getMessage());
            assertEquals(e.getLocalizedMessage(), ue.getLocalizedMessage());
            assertEquals(e.getLine(), ue.getLine());
            assertEquals(e.getLineNo(), ue.getLineNo());
            assertEquals(e.getLinePos(), ue.getLinePos());
            assertEquals(e.getLen(), ue.getLen());
            assertEquals(e.asString(), ue.asString());
            assertEquals("Unchecked " + e.toString(), ue.toString());
        }
    }

    @Test
    public void testLargeBuffer() throws IOException, JsonException {
        // Test that we can consolidate lines both:
        //  - Part of normal JSON structure parsing.
        //  - Part of string parsing.
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 500; ++i) {
            content.append("\"");
            content.append(Strings.escape(lorem));
            content.append("\"");
            content.append(",");
        }

        JsonTokenizer tokenizer = makeTokenizer(content.toString());

        for (int i = 0; i < 500; ++i) {
            assertThat(tokenizer.expectString("").decodeJsonLiteral(), is(lorem));
            assertThat(tokenizer.expectSymbol("", ','), is(','));
        }
    }

    @Test
    public void testUtf8Strings() throws IOException, JsonException {
        // Test that we handle utf-8 in strings correctly.
        JsonTokenizer tokenizer = makeTokenizer("\"æ優\\u00d6\"");
        assertThat(tokenizer.expectString("").decodeJsonLiteral(), is("æ優Ö"));
    }

    private JsonTokenizer makeTokenizer(String content) throws IOException {
        byte[] src = content.getBytes(UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(src);
        return new JsonTokenizer(bais, 1 << 10);
    }
}
