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
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for JsonWriter.
 */
public class JsonWriterTest {
    @Test
    public void testPrimitiveValues() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(baos);
        writer.value(true);
        writer.flush();
        assertJsonEquals("true", baos);

        writer.reset();
        baos.reset();
        writer.value((short) -8096);
        writer.flush();
        assertJsonEquals("-8096", baos);

        writer.reset();
        baos.reset();
        writer.value(8096000);
        writer.flush();
        assertJsonEquals("8096000", baos);

        writer.reset();
        baos.reset();
        writer.value(-1234567890123456789L);
        writer.flush();
        assertJsonEquals("-1234567890123456789", baos);

        writer.reset();
        baos.reset();
        writer = new JsonWriter(baos);
        writer.value(-8096.9876);
        writer.flush();
        assertJsonEquals("-8096.9876", baos);

        // normal string.
        writer.reset();
        baos.reset();
        writer.value("Shub Niggurath");
        writer.flush();
        assertJsonEquals("\"Shub Niggurath\"", baos);

        // UTF-8 string.
        writer.reset();
        baos.reset();
        writer.value("ßħ↓b\u0bfcÑıŋú®äþ\n");
        writer.flush();
        assertJsonEquals("\"ßħ↓b\\u0bfcÑıŋú®äþ\\n\"", baos);

        // String escape chars
        writer.reset();
        baos.reset();
        writer.value("\u0bfc\n\r\f\t\b\"\\");
        writer.flush();
        assertJsonEquals("\"\\u0bfc\\n\\r\\f\\t\\b\\\"\\\\\"", baos);

        // Empty string.
        writer.reset();
        baos.reset();
        writer.value("");
        writer.flush();
        assertJsonEquals("\"\"", baos);

        writer.reset();
        baos.reset();
        writer.value(Binary.wrap(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0}));
        writer.flush();
        assertJsonEquals("\"AAECAwQFBgcICQA\"", baos);
    }

    @Test
    public void testLiteral() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(baos);

        // Literal values break the JSON standard, as they are free-form
        // strings inserted unquoted (and unescaped).
        writer.object();
        writer.keyLiteral("the_key");
        writer.valueLiteral("the_value");
        writer.endObject();
        writer.flush();
        assertJsonEquals("{the_key:the_value}", baos);
    }

    @Test
    public void testNullValues() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(baos);
        writer.value((String) null);
        writer.flush();
        assertJsonEquals("null", baos);

        // String escape chars
        writer.reset();
        baos.reset();
        writer.value((Binary) null);
        writer.flush();
        assertJsonEquals("null", baos);

        // String escape chars
        writer.reset();
        baos.reset();
        writer.valueLiteral(null);
        writer.flush();
        assertJsonEquals("null", baos);
    }

    @Test
    public void testObject() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);
        writer.object();

        writer.key(true);
        writer.value(true);

        writer.key(false);
        writer.value(false);

        writer.key((byte) 1);
        writer.value((byte) 1);

        writer.key((short) 1);
        writer.value((short) 1);

        writer.key(1);
        writer.value(1);

        writer.key(1L);
        writer.value(1L);

        writer.key(1.1d);
        writer.value(1.1d);

        writer.key("str");
        writer.value("str");

        Binary bin = Binary.wrap(new byte[]{0, 1});
        writer.key(bin);
        writer.value(bin);

        writer.endObject();
        writer.flush();

        assertJsonEquals(
                "{\"true\":true,\"false\":false,\"1\":1,\"1\":1,\"1\":1,\"1\":1,\"1.1\":1.1,\"str\":\"str\",\"AAE\":\"AAE\"}",
                baos);
    }

    @Test
    public void testArray() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);
        writer.array();

        writer.value(true);
        writer.value(false);
        writer.value((byte) 1);
        writer.value((short) 1);
        writer.value(1);
        writer.value(1L);
        writer.value(1.1d);
        writer.value("str");

        Binary bin = Binary.wrap(new byte[]{0, 1});
        writer.value(bin);

        writer.endArray();
        writer.flush();

        assertJsonEquals(
                "[true,false,1,1,1,1,1.1,\"str\",\"AAE\"]",
                baos);
    }

    @Test
    public void testDoubles() throws JsonException {
        // double values have many types of formatting.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);
        writer.object();

        writer.key(0d);
        writer.value(0d);

        writer.key(1234567890d);
        writer.value(1234567890d);

        writer.key(-1234567890.1234567d);
        writer.value(-1234567890.1234567d);

        writer.key(1234567890.1234567890d);
        writer.value(1234567890.1234567890d);

        writer.key(754.123d);
        writer.value(754.123d);

        writer.endObject();
        writer.flush();

        assertJsonEquals(
                "{" +
                        "\"0\":0," +
                        "\"1234567890\":1234567890," +
                        "\"-1.2345678901234567E9\":-1.2345678901234567E9," +
                        "\"1.2345678901234567E9\":1.2345678901234567E9," +
                        "\"754.123\":754.123" +
                "}",
                baos);
    }

    @Test
    public void testInvalidKeys() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);
        writer.object();
        assertJsonException("Expected map key, but got value.", () -> writer.value((String) null));

        writer.reset();
        writer.object();
        assertJsonException("Expected map key, but got value.", writer::object);

        writer.reset();
        writer.object();
        assertJsonException("Expected map key, but got value.", writer::array);

        writer.reset();
        writer.object();
        assertJsonException("Expected map key, but got null.", () -> writer.key((String) null));

        writer.reset();
        writer.object();
        assertJsonException("Expected map key, but got null.", () -> writer.key((Binary) null));

        writer.reset();
        writer.object();
        assertJsonException("Expected map key, but got null.", () -> writer.keyLiteral(null));

        writer.object();
        assertJsonException("Expected map key, but got null.", () -> writer.keyLiteral(null));

        writer.reset();
        writer.object();
        writer.key("key");

        assertJsonException("Unexpected map key, expected value or end.", () -> writer.key("another"));
    }

    @Test
    public void testInvalidValues() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);

        assertJsonException("Unexpected map key outside map.", () -> writer.key("key"));

        writer.value("value");
        assertJsonException("Value already written, and not in container.", () -> writer.value("another"));
    }

    @Test
    public void testInvalidEnds() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonWriter writer = new JsonWriter(baos);

        // Ending without container state.
        assertJsonException("Unexpected end, not in object.", writer::endObject);

        assertJsonException("Unexpected end, not in list.", writer::endArray);

        // Ending wrong container state.
        writer.array();
        assertJsonException("Unexpected end, not in object.", writer::endObject);

        writer.reset();
        writer.object();
        assertJsonException("Unexpected end, not in list.", writer::endArray);

        // Ending after map key (not allowed).
        writer.key("key");
        assertJsonException("Expected map value but got end.", writer::endObject);
    }

    // --- Helper Methods ---

    private static void assertJsonException(String message, Runnable f) {
        try {
            f.run();
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertEquals(message, e.getMessage());
            assertEquals("java.lang.IllegalArgumentException: " + message, e.toString());
        } catch (IllegalStateException e) {
            assertEquals(message, e.getMessage());
            assertEquals("java.lang.IllegalStateException: " + message, e.toString());
        }
    }

    private static void assertJsonEquals(String expected, ByteArrayOutputStream actual) {
        String actualString = new String(actual.toByteArray(), UTF_8);
        assertEquals(expected, actualString);
    }
}
