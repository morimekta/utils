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
package net.morimekta.console.args;

import net.morimekta.config.impl.SimpleConfig;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static net.morimekta.console.util.Parser.i32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for the Property argument.
 */
public class PropertyTest {
    @Test
    public void testConstructor() {
        Map<String, String> map = new TreeMap<>();

        Property p = new Property('D', "Help", map::put);

        assertEquals("-Dkey=val", p.getPrefix());
        assertEquals("Help", p.getUsage());
        assertEquals("[-Dkey=val ...]", p.getSingleLineUsage());
        assertEquals("key", p.getMetaKey());

        p.validate();
    }

    @Test
    public void testApplyShort() {
        Map<String, String> map = new TreeMap<>();
        Property p = new Property('D', "Help", map::put);

        p.applyShort("Dkey=val", new ArgumentList("-Dkey=val"));

        assertEquals("val", map.get("key"));

        try {
            p.applyShort("Dkey", new ArgumentList("-Dkey", "val"));
            fail("No exception on invalid applyShort");
        } catch (ArgumentException e) {
            assertEquals("No key value sep for properties: key", e.getMessage());
        }

        try {
            p.applyShort("D=val", new ArgumentList("-D=val"));
            fail("No exception on invalid applyShort");
        } catch (ArgumentException e) {
            assertEquals("Empty property key: \"-D=val\"", e.getMessage());
        }
    }

    @Test
    public void testApply() {
        Map<String, String> map = new TreeMap<>();
        Property p = new Property("--def", 'D', "Help", map::put);
        assertEquals(2, p.apply(new ArgumentList("--def", "key=val")));
        assertEquals("val", map.get("key"));
   }

    @Test
    public void testApplyParser() {
        SimpleConfig config = new SimpleConfig();
        Property straight = new Property("--def", 'D', "Help", config::put);
        Property typed = new Property('I', "Int", i32().andPut(config::putInteger));
        Option typedInto = new Option("--i32", "i", "num", "I2", i32().andPutAs(config::putInteger, "i2"));

        assertEquals(2, straight.apply(new ArgumentList("--def", "key=val")));
        assertEquals("val", config.getString("key"));

        typed.applyShort("Ik=32", new ArgumentList("-I32"));
        assertEquals(32, config.get("k"));

        typedInto.apply(new ArgumentList("--i32", "1234"));
        assertEquals(1234, config.get("i2"));
    }
}
