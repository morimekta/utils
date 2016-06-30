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
package net.morimekta.config;

import net.morimekta.util.Strings;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Function;

import static net.morimekta.config.ValueUtil.asBoolean;
import static net.morimekta.config.ValueUtil.asCollection;
import static net.morimekta.config.ValueUtil.asDouble;
import static net.morimekta.config.ValueUtil.asInteger;
import static net.morimekta.config.ValueUtil.asLong;
import static net.morimekta.config.ValueUtil.asString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for ValueUtil
 */
public class ValueUtilTest {
    @Test
    public void testAsBoolean() {
        assertTrue(asBoolean(true));
        assertTrue(asBoolean(1L));
        assertTrue(asBoolean("1"));
        assertTrue(asBoolean("y"));
        assertTrue(asBoolean("True"));
        assertFalse(asBoolean(false));
        assertFalse(asBoolean((short) 0));
        assertFalse(asBoolean("0"));
        assertFalse(asBoolean("n"));
        assertFalse(asBoolean("FALSE"));

        assertException("Unable to parse the string \"foo\" to boolean", "foo", ValueUtil::asBoolean);
        assertException("Unable to convert LinkedList to a boolean", new LinkedList<>(), ValueUtil::asBoolean);
        assertException("Unable to convert double value to boolean", 12.34, ValueUtil::asBoolean);
        assertException("Unable to convert number 3 to boolean", 3, ValueUtil::asBoolean);
    }

    @Test
    public void testAsInteger() {
        assertEquals(1, asInteger(1));
        assertEquals(1, asInteger(1L));
        assertEquals(1, asInteger(true));
        assertEquals(1, asInteger("1"));

        assertException("Unable to parse string \"foo\" to an int", "foo", ValueUtil::asInteger);
        assertException("Unable to convert LinkedList to an int", new LinkedList<>(), ValueUtil::asInteger);
    }

    @Test
    public void testAsLong() {
        assertEquals(1L, asLong(1));
        assertEquals(1L, asLong(1L));
        assertEquals(1L, asLong(true));
        assertEquals(1L, asLong("1"));

        assertException("Unable to parse string \"foo\" to a long", "foo", ValueUtil::asLong);
        assertException("Unable to convert LinkedList to a long", new LinkedList<>(), ValueUtil::asLong);
    }

    @Test
    public void testAsDouble() {
        assertEquals(1.0, asDouble(1L), 0.001);
        assertEquals(1.1, asDouble(1.1f), 0.001);
        assertEquals(1.1, asDouble(1.1), 0.001);
        assertEquals(1.1, asDouble("1.1"), 0.001);

        assertException("Unable to parse string \"foo\" to a double", "foo", ValueUtil::asDouble);
        assertException("Unable to convert LinkedList to a double", new LinkedList<>(), ValueUtil::asDouble);
    }

    @Test
    public void testAsString() {
        assertEquals("1", asString(1L));
        assertEquals("1.1", asString(1.1));
        assertEquals("foo", asString("foo"));

        assertException("Unable to convert LinkedList to a string", new LinkedList<>(), ValueUtil::asString);
    }

    @Test
    public void testAsCollection() {
        assertEquals(ImmutableList.of("a"),
                     asCollection(Collections.singletonList("a")));

        assertException("Unable to convert String to a collection", "not a list", ValueUtil::asCollection);
    }

    void assertException(String message, Object value, Function<Object, Object> func) {
        try {
            func.apply(value);
            fail("No exception on \"" + Strings.escape(value.toString()) + "\"");
        } catch (Exception e) {
            assertEquals(message, e.getMessage());
        }
    }
}
