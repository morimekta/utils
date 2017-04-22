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
package net.morimekta.config.util;

import com.google.common.collect.ImmutableList;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.JsonConfigParser;
import net.morimekta.config.format.PropertiesConfigParser;
import net.morimekta.config.format.TomlConfigParser;
import net.morimekta.config.impl.ImmutableConfig;
import net.morimekta.config.impl.SimpleConfig;
import net.morimekta.util.Numeric;
import net.morimekta.util.Stringable;
import net.morimekta.util.Strings;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.function.Function;

import static net.morimekta.config.util.ConfigUtil.asBoolean;
import static net.morimekta.config.util.ConfigUtil.asBooleanArray;
import static net.morimekta.config.util.ConfigUtil.asCollection;
import static net.morimekta.config.util.ConfigUtil.asDate;
import static net.morimekta.config.util.ConfigUtil.asDouble;
import static net.morimekta.config.util.ConfigUtil.asDoubleArray;
import static net.morimekta.config.util.ConfigUtil.asInteger;
import static net.morimekta.config.util.ConfigUtil.asIntegerArray;
import static net.morimekta.config.util.ConfigUtil.asLong;
import static net.morimekta.config.util.ConfigUtil.asLongArray;
import static net.morimekta.config.util.ConfigUtil.asString;
import static net.morimekta.config.util.ConfigUtil.asStringArray;
import static net.morimekta.config.util.ConfigUtil.getParserForName;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for ConfigUtil
 */
public class ConfigUtilTest {
    @Test
    public void testGetParserForName() {
        assertThat(getParserForName(".toml"), instanceOf(TomlConfigParser.class));
        assertThat(getParserForName(".ini"), instanceOf(TomlConfigParser.class));
        assertThat(getParserForName(".json"), instanceOf(JsonConfigParser.class));
        assertThat(getParserForName(".properties"), instanceOf(PropertiesConfigParser.class));

        assertThat(getParserForName("config.toml"), instanceOf(TomlConfigParser.class));
        assertThat(getParserForName("/home/test.ini"), instanceOf(TomlConfigParser.class));
        assertThat(getParserForName("c:\\my.properties.json"), instanceOf(JsonConfigParser.class));
        assertThat(getParserForName("/home/toml/json.properties"), instanceOf(PropertiesConfigParser.class));

        try {
            getParserForName("abba");
            fail("no exception");
        } catch (ConfigException e) {
            assertThat(e.getMessage(), is("No file suffix in name: abba"));
        }
    }

    @Test
    public void testHashCode() {
        SimpleConfig a = new SimpleConfig();
        a.putString("a", "1234");

        ImmutableConfig b = ImmutableConfig.copyOf(new SimpleConfig().putInteger("a", 1234));

        assertEquals(ConfigUtil.hashCode(a), ConfigUtil.hashCode(b));
    }

    @Test
    public void testEquals() {
        SimpleConfig a = new SimpleConfig();
        a.putString("a", "1234");

        ImmutableConfig b = ImmutableConfig.copyOf(new SimpleConfig().putInteger("a", 1234));

        assertTrue(ConfigUtil.equals(a, b));
    }

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

        assertException("Unable to parse the string \"foo\" to boolean", "foo", ConfigUtil::asBoolean);
        assertException("Unable to convert LinkedList to a boolean", new LinkedList<>(), ConfigUtil::asBoolean);
        assertException("Unable to convert double value to boolean", 12.34, ConfigUtil::asBoolean);
        assertException("Unable to convert number 3 to boolean", 3, ConfigUtil::asBoolean);
    }

    @Test
    public void testAsInteger() {
        assertEquals(1, asInteger(1));
        assertEquals(1, asInteger(1L));
        assertEquals(1, asInteger(true));
        assertEquals(1, asInteger("1"));
        assertEquals(4, asInteger((Numeric) () -> 4));
        assertEquals(1234567890, asInteger(new Date(1234567890000L)));

        assertException("Unable to parse string \"foo\" to an int", "foo", ConfigUtil::asInteger);
        assertException("Unable to convert LinkedList to an int", new LinkedList<>(), ConfigUtil::asInteger);
    }

    @Test
    public void testAsLong() {
        assertEquals(1L, asLong(1));
        assertEquals(1L, asLong(1L));
        assertEquals(1L, asLong(true));
        assertEquals(1L, asLong("1"));
        assertEquals(4L, asLong((Numeric) () -> 4));
        assertEquals(1234567890000L, asLong(new Date(1234567890000L)));

        assertException("Unable to parse string \"foo\" to a long", "foo", ConfigUtil::asLong);
        assertException("Unable to convert LinkedList to a long", new LinkedList<>(), ConfigUtil::asLong);
    }

    @Test
    public void testAsDouble() {
        assertEquals(1.0, asDouble(1L), 0.001);
        assertEquals(1.1, asDouble(1.1f), 0.001);
        assertEquals(1.1, asDouble(1.1), 0.001);
        assertEquals(1.1, asDouble("1.1"), 0.001);
        assertEquals(4.0, asDouble((Numeric) () -> 4), 0.001);

        assertException("Unable to parse string \"foo\" to a double", "foo", ConfigUtil::asDouble);
        assertException("Unable to convert LinkedList to a double", new LinkedList<>(), ConfigUtil::asDouble);
    }

    @Test
    public void testAsString() {
        assertEquals("1", asString(1L));
        assertEquals("1.1", asString(1.1));
        assertEquals("foo", asString("foo"));
        assertEquals("4", asString((Stringable) () -> "4"));

        assertException("Unable to convert LinkedList to a string", new LinkedList<>(), ConfigUtil::asString);
    }

    @Test
    public void testAsCollection() {
        assertEquals(ImmutableList.of("a"),
                     asCollection(Collections.singletonList("a")));

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asCollection);
    }

    @Test
    public void testAsDate() {
        Date date = new Date(1461357665000L);

        assertThat(asDate(asString(date)), is(date));
        assertThat(asDate(date), is(sameInstance(date)));
        assertThat(asDate("2016-04-22T20:41:05Z"), is(new Date(1461357665000L)));
        assertThat(asDate("2016-04-22T20:41:05+02:00"), is(new Date(1461357665000L)));
        assertThat(asDate(1461357665000L), is(new Date(1461357665000L)));
        assertThat(asDate(1461357665), is(new Date(1461357665000L)));

        try {
            asDate("2016-04-22T20:41:05+CET");
            fail("no exception");
        } catch (ConfigException e) {
            assertThat(e.getMessage(), is("Unable to parse date: Text '2016-04-22T20:41:05+CET' could not be parsed at index 19"));
        }

        try {
            asDate(ImmutableList.of(1, 2, 3));
            fail("no exception");
        } catch (ConfigException e) {
            assertThat(e.getMessage(), is("Unable to convert RegularImmutableList to a date"));
        }
    }

    @Test
    public void testAsStringArray() {
        String[] a = asStringArray(ImmutableList.of(1, 2, 3));
        assertArrayEquals(new String[]{"1", "2", "3"}, a);

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asStringArray);
    }

    @Test
    public void testAsBooleanArray() {
        boolean[] a = asBooleanArray(ImmutableList.of(1, "false", true));
        assertArrayEquals(new boolean[]{true, false, true}, a);

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asStringArray);
    }

    @Test
    public void testAsIntegerArray() {
        int[] a = asIntegerArray(ImmutableList.of(1, "2", 3L));
        assertArrayEquals(new int[]{1, 2, 3}, a);

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asStringArray);
    }

    @Test
    public void testAsLongArray() {
        long[] a = asLongArray(ImmutableList.of(1, "2", 3L));
        assertArrayEquals(new long[]{1L, 2L, 3L}, a);

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asStringArray);
    }

    @Test
    public void testAsDoubleArray() {
        double[] a = asDoubleArray(ImmutableList.of(1, "2.2", 3.3));
        assertArrayEquals(new double[]{1, 2.2, 3.3}, a, 0.001);

        assertException("Unable to convert String to a collection", "not a list", ConfigUtil::asStringArray);
    }

    @Test
    public void testEquals2() {
        SimpleConfig a = new SimpleConfig();
        a.putBoolean("b", true);

        SimpleConfig am = new SimpleConfig(a);
        SimpleConfig b = new SimpleConfig();
        b.putBoolean("b", false);

        assertThat(ConfigUtil.equals(a, a), is(true));
        assertThat(ConfigUtil.equals(a, am), is(true));
        assertThat(ConfigUtil.equals(a, b), is(false));
        assertThat(ConfigUtil.equals(a, null), is(false));
        assertThat(ConfigUtil.equals(null, null), is(true));
    }

    void assertException(String message, Object value, Function<Object, Object> func) {
        try {
            func.apply(value);
            fail("No exception on \"" + Strings.escape(value.toString()) + "\"");
        } catch (Exception e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testConstructor() throws
                                  NoSuchMethodException,
                                  IllegalAccessException,
                                  InvocationTargetException,
                                  InstantiationException {
        Constructor<ConfigUtil> c = ConfigUtil.class.getDeclaredConstructor();
        assertThat(c.isAccessible(), is(false));
        c.setAccessible(true);
        assertThat(c.newInstance(), is(instanceOf(ConfigUtil.class)));
        c.setAccessible(false);
    }
}
