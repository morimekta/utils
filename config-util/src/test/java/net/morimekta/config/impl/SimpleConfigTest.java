/*
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
package net.morimekta.config.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.morimekta.config.Config;
import net.morimekta.config.ConfigBuilder;
import net.morimekta.config.ConfigException;
import net.morimekta.config.KeyNotFoundException;
import net.morimekta.util.Stringable;
import org.junit.Test;

import java.util.Date;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for simple config and common config methods.
 */
public class SimpleConfigTest {
    @Test
    public void testConfig() {
        SimpleConfig config = new SimpleConfig();

        assertTrue(config.isEmpty());
        assertEquals(0, config.size());
    }

    @Test
    public void testStringValue() throws ConfigException {
        ConfigBuilder builder = new SimpleConfig();

        assertSame(builder, builder.putString(Key.A, "b"));
        assertEquals("b", builder.getString(Key.A));
        assertTrue(builder.containsKey(Key.A));

        assertSame(builder, builder.putString(Key.B_C, "d"));
        assertEquals("d", builder.getString(Key.B_C));
    }

    @Test
    public void testCore() {
        ConfigBuilder builder = new SimpleConfig();
        assertSame(builder, builder.putString(Key.A, "b"));
        assertSame(builder, builder.putString(Key.B_C, "d"));
        assertThat(builder.get(Key.A), is("b"));

        ConfigBuilder other = new SimpleConfig();
        other.putAll(builder);

        assertTrue(builder.equals(other));
        assertFalse(builder.equals(null));
        assertTrue(builder.equals(builder));
        assertFalse(builder.equals(new Object()));

        assertThat(builder.toString(), is("SimpleConfig{a=b, b.c=d}"));
        assertThat(builder.hashCode(), is(other.hashCode()));

        builder.put(Key.DOUBLE, 2.2);
        assertThat(builder.hashCode(), is(not(other.hashCode())));
    }

    @Test
    public void testLongValue() throws ConfigException {
        SimpleConfig builder = new SimpleConfig();

        assertSame(builder, builder.putLong(Key.A, 1234567890L));
        assertEquals(1234567890L, builder.getLong(Key.A));

        assertSame(builder, builder.putLong(Key.B_C, 9876543210L));
        assertEquals(9876543210L, builder.getLong(Key.B_C));
    }

    @Test
    public void testDateValue() {
        Date now = new Date();
        String str = now.toInstant().toString();

        SimpleConfig builder = new SimpleConfig();
        builder.putDate(Key.D1, now);
        builder.putString(Key.D2, str);

        assertEquals(str, builder.getString(Key.D1));
        assertEquals(now, builder.getDate(Key.D2));
        assertSame(now, builder.getDate(Key.D3, now));

        try {
            builder.getDate(Key.D3);
            fail("no exception");
        } catch (ConfigException e) {
            assertEquals("No such config entry \"d3\"", e.getMessage());
        }
    }

    @Test
    public void testCollectionValue() {
        SimpleConfig builder = new SimpleConfig();
        builder.putCollection(Key.B_C, ImmutableSet.of(1, 2, 3));
        builder.putCollection(Key.D, ImmutableList.of("a", "b"));

        assertThat(builder.getCollection(Key.B_C), is(ImmutableSet.of(1, 2, 3)));
        assertThat(builder.getCollection(Key.D), is(ImmutableList.of("a", "b")));

        try {
            builder.getCollection(Key.D3);
            fail("no exception");
        } catch (ConfigException e) {
            assertEquals("No such config entry \"d3\"", e.getMessage());
        }

    }

    @Test
    public void testGetDefault() {
        Config config = new SimpleConfig()
                .putBoolean(Key.BOOL, false)
                .putInteger(Key.INT, 3)
                .putLong(Key.LONG, 3L)
                .putDouble(Key.DOUBLE, 3.3)
                .putString(Key.STR, "not default");

        assertEquals(true, config.getBoolean(Key.NOT_FOUND, true));
        assertEquals(1, config.getInteger(Key.NOT_FOUND, 1));
        assertEquals(1L, config.getLong(Key.NOT_FOUND, 1));
        assertEquals(1.1, config.getDouble(Key.NOT_FOUND, 1.1), 0.001);
        assertEquals("default", config.getString(Key.NOT_FOUND, "default"));
        assertEquals(null, config.getString(Key.NOT_FOUND, null));

        assertEquals(false, config.getBoolean(Key.BOOL, true));
        assertEquals(3, config.getInteger(Key.INT, 1));
        assertEquals(3L, config.getLong(Key.LONG, 1));
        assertEquals(3.3, config.getDouble(Key.DOUBLE, 1.1), 0.001);
        assertEquals("not default", config.getString(Key.STR, "default"));
        assertEquals("not default", config.getString(Key.STR, null));
    }

    @Test
    public void testConstructor() {
        Config config = new SimpleConfig()
                .putBoolean(Key.BOOL, false)
                .putInteger(Key.INT, 3)
                .putLong(Key.LONG, 3L)
                .putDouble(Key.DOUBLE, 3.3)
                .putString(Key.STR, "not default");

        SimpleConfig copy = new SimpleConfig(config);

        assertEquals(config, copy);

        copy.remove(Key.BOOL);

        assertNotEquals(config, copy);
    }

    @Test
    public void testNotFound() {
        Config config = new SimpleConfig();

        assertNotFound(config::getBoolean);
        assertNotFound(config::getDouble);
        assertNotFound(config::getInteger);
        assertNotFound(config::getLong);
        assertNotFound(config::getCollection);
        assertNotFound(config::getString);
    }

    @Test
    public void testToString() {
        ConfigBuilder config = new SimpleConfig();
        config.putString("a", "b");

        assertEquals("SimpleConfig{a=b}", config.toString());
    }

    private void assertNotFound(Function<Stringable, Object> func) {
        try {
            func.apply(Key.NOT_FOUND);
            fail("No exception on not found");
        } catch (KeyNotFoundException ke) {
            // pass.
        }
    }

    private enum Key implements Stringable {
        NOT_FOUND,

        A,
        B_C,
        D,

        D1,
        D2,
        D3,

        BOOL,
        INT,
        LONG,
        DOUBLE,
        STR;
        ;

        @Override
        public String asString() {
            return name().replaceAll("_", ".").toLowerCase();
        }
    }
}
