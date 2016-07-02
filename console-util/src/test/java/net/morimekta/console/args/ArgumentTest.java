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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for Argument.
 */
public class ArgumentTest {
    @Test
    public void testConstructor() {
        AtomicReference<String> ref = new AtomicReference<>();
        Argument arg = new Argument("name", "Set name", ref::set);

        assertEquals("name", arg.getName());
        assertEquals("name", arg.getPrefix());
        assertEquals("name", arg.getSingleLineUsage());
        assertEquals("Set name", arg.getUsage());
        assertEquals(null, arg.getDefaultValue());

        try {
            arg.validate();
            fail("no exception");
        } catch (ArgumentException e) {
            assertEquals("Argument \"name\" is required", e.getMessage());
        }

        arg = new Argument("name", "Set name", ref::set, "foo");

        assertEquals("name", arg.getName());
        assertEquals("name", arg.getPrefix());
        assertEquals("[name]", arg.getSingleLineUsage());
        assertEquals("Set name", arg.getUsage());
        assertEquals("foo", arg.getDefaultValue());

        arg.validate();

        arg = new Argument("name", "Set name", ref::set, "foo", null, false, true, false);
        assertEquals("name", arg.getPrefix());
        assertEquals("name", arg.getSingleLineUsage());

        arg = new Argument("name", "Set name", ref::set, "foo", null, true, false, false);
        assertEquals("name", arg.getPrefix());
        assertEquals("[name...]", arg.getSingleLineUsage());

        arg = new Argument("name", "Set name", ref::set, "foo", null, true, false, true);
        assertEquals("name", arg.getPrefix());
        assertEquals(null, arg.getSingleLineUsage());

    }
}
