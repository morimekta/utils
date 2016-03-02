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

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the JSON tokenizer.
 */
public class JsonTokenizerTest {
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
        assertBad("Illegal character in JSON structure: '\\u00c3'", "æ");
        assertBad("Unexpected end of file while __string__", "");
    }


    private void assertBad(String message, String str) {
        try {
            makeTokenizer(str).expect("__string__");
            fail("no exception on bad string: \"" + str + "\"");
        } catch (JsonException|IOException e) {
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
    public void testBadString() {
        assertBadString("Unexpected end of stream while __string__", "");
        assertBadString("Expected string literal, but found number while __string__", "11");
        assertBadString("Unexpected end of stream in string literal.", "\"11");
    }

    private void assertBadString(String message, String str) {
        try {
            makeTokenizer(str).expectString("__string__");
            fail("no exception on bad string: \"" + str + "\"");
        } catch (JsonException|IOException e) {
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
        assertBadNumber("Unexpected end of stream while __string__", "");
        assertBadNumber("Expected number, but found token while __string__", "false");
        assertBadNumber("Expected number, but found literal while __string__", "\"string\"");
        assertBadNumber("Wrongly terminated JSON number: x.", "1x");
        assertBadNumber("Negative indicator without number.", "-");
        assertBadNumber("No decimal after negative indicator.", "-,");
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
        assertBadSymbol("Expected one of \"]\", but found \"SYMBOL('[',1:1-2)\" while __string__", "[", ']');
        assertBadSymbol("Unexpected end of stream while __string__", "", ']');
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
            assertEquals("Expected number, but found literal while __number__", e.getMessage());
            assertEquals(2, e.getLineNo());
            assertEquals("    \"first\": \"value\",", e.getLine());
            assertEquals(5, e.getLinePos());
            assertEquals(7, e.getLen());
            assertEquals("JsonException(JSON Error on line 2: Expected number, but found literal while __number__\n" +
                         "#     \"first\": \"value\",\n" +
                         "#-----^^^^^^^)", e.toString());

            UncheckedJsonException ue = new UncheckedJsonException(e);
            assertEquals(e.getMessage(), ue.getMessage());
            assertEquals(e.getLocalizedMessage(), ue.getLocalizedMessage());
            assertEquals(e.getLine(), ue.getLine());
            assertEquals(e.getLineNo(), ue.getLineNo());
            assertEquals(e.getLinePos(), ue.getLinePos());
            assertEquals(e.getLen(), ue.getLen());
            assertEquals(e.describe(), ue.describe());
            assertEquals("Unchecked" + e.toString(), ue.toString());
        }

        try {
            tokenizer.getLine(0);
            fail("No exception on invalid input.");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid line number requested: 0", e.getMessage());
        }

        assertEquals("{", tokenizer.getLine(1));
    }

    @Test
    @Ignore("Not implemented yet.")
    public void testLargeBuffer() {
        // TODO: test that we can consolidate lines both:
        //  - Part of normal JSON structure parsing.
        //  - Part of string parsing.
    }

    @Test
    @Ignore("Not implemented yet.")
    public void testUtf8Strings() {
        // TODO: Test that we handle utf-8 in strings correctly.
    }

    private JsonTokenizer makeTokenizer(String content) {
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(UTF_8));
        return new JsonTokenizer(bais);
    }
}
