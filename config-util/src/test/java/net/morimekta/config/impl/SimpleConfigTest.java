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

import net.morimekta.config.Config;
import net.morimekta.config.ConfigBuilder;
import net.morimekta.config.ConfigException;
import net.morimekta.config.KeyNotFoundException;

import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
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
        SimpleConfig builder = new SimpleConfig();

        assertSame(builder, builder.putString("a", "b"));
        assertEquals("b", builder.getString("a"));

        assertSame(builder, builder.putString("b.c", "d"));
        assertEquals("d", builder.getString("b.c"));
    }

    @Test
    public void testLongValue() throws ConfigException {
        SimpleConfig builder = new SimpleConfig();

        assertSame(builder, builder.putLong("a", 1234567890L));
        assertEquals(1234567890L, builder.getLong("a"));

        assertSame(builder, builder.putLong("b.c", 9876543210L));
        assertEquals(9876543210L, builder.getLong("b.c"));
    }

    @Test
    public void testGetDefault() {
        Config config = new SimpleConfig()
                .putBoolean("bool", false)
                .putInteger("int", 3)
                .putLong("long", 3L)
                .putDouble("double", 3.3)
                .putString("str", "not default");

        assertEquals(true, config.getBoolean("not.found", true));
        assertEquals(1, config.getInteger("not.found", 1));
        assertEquals(1L, config.getLong("not.found", 1));
        assertEquals(1.1, config.getDouble("not.found", 1.1), 0.001);
        assertEquals("default", config.getString("not.found", "default"));

        assertEquals(false, config.getBoolean("bool", true));
        assertEquals(3, config.getInteger("int", 1));
        assertEquals(3L, config.getLong("long", 1));
        assertEquals(3.3, config.getDouble("double", 1.1), 0.001);
        assertEquals("not default", config.getString("str", "default"));
    }

    @Test
    public void testConstructor() {
        Config config = new SimpleConfig()
                .putBoolean("bool", false)
                .putInteger("int", 3)
                .putLong("long", 3L)
                .putDouble("double", 3.3)
                .putString("str", "not default");

        SimpleConfig copy = new SimpleConfig(config);

        assertEquals(config, copy);

        copy.remove("bool");

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

    private void assertNotFound(Function<String, Object> func) {
        try {
            func.apply("not.found");
            fail("No exception on not found");
        } catch (KeyNotFoundException ke) {
            // pass.
        }
    }
}
