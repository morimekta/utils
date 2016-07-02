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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TODO(morimekta): Make a real class description.
 */
public class OptionTest {
    @Test
    public void testConstrictor() {
        AtomicReference<String> ref = new AtomicReference<>();
        Option opt = new Option("--opt", null, "o", "Option", ref::set);

        assertEquals("--opt", opt.getName());
        assertEquals("", opt.getShortNames());
        assertEquals("[--opt o]", opt.getSingleLineUsage());
        assertEquals("--opt o", opt.getPrefix());
        assertEquals("Option", opt.getUsage());
        assertFalse(opt.isHidden());
        assertFalse(opt.isRequired());
        assertFalse(opt.isRepeated());

        opt = new Option("--opt", "O", "o", "Option", ref::set, null, true, true, false);
        assertEquals("--opt", opt.getName());
        assertEquals("O", opt.getShortNames());
        assertEquals("-O o", opt.getSingleLineUsage());
        assertEquals("--opt (-O) o", opt.getPrefix());
        assertFalse(opt.isHidden());
        assertTrue(opt.isRequired());
        assertTrue(opt.isRepeated());

        opt = new Option("--opt", "O", "o", "Option", ref::set, null, false, false, true);
        assertEquals("--opt", opt.getName());
        assertEquals("O", opt.getShortNames());
        assertEquals(null, opt.getSingleLineUsage());
        assertEquals("--opt (-O) o", opt.getPrefix());
        assertEquals("--opt", opt.nameOrShort());
        assertTrue(opt.isHidden());
        assertFalse(opt.isRequired());
        assertFalse(opt.isRepeated());

        opt = new Option(null, "Oof", "o", "Option", ref::set);
        assertEquals(null, opt.getName());
        assertEquals("Oof", opt.getShortNames());
        assertEquals("[-O o]", opt.getSingleLineUsage());
        assertEquals("-O (-o, -f) o", opt.getPrefix());
        assertEquals("-O", opt.nameOrShort());
        assertFalse(opt.isHidden());
        assertFalse(opt.isRequired());
        assertFalse(opt.isRepeated());
    }

    @Test
    public void testBasConstructor() {
        AtomicReference<String> ref = new AtomicReference<>();
        try {
            new Option(null, "", "o", "Option", ref::set);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Option must have name or short name", e.getMessage());
        }

        try {
            new Option("-foo", "", "o", "Option", ref::set);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Option name \"-foo\" does not start with '--'", e.getMessage());
        }
    }

    @Test
    public void testApplyShort() {
        AtomicReference<String> ref = new AtomicReference<>();
        Option opt = new Option("--opt", "O", "o", "Option", ref::set);

        opt.applyShort("Oval", new ArgumentList("-Oval", "not"));
        assertEquals("val", ref.get());

        opt = new Option("--opt", "O", "o", "Option", ref::set);
        opt.applyShort("O", new ArgumentList("-O", "other", "not"));
        assertEquals("other", ref.get());

        try {
            opt.applyShort("O", new ArgumentList("-O", "other", "not"));
            fail("no exception");
        } catch (ArgumentException e) {
            assertEquals("Option --opt already applied", e.getMessage());
        }

        opt = new Option("--opt", "O", "o", "Option", ref::set);
        try {
            opt.applyShort("O", new ArgumentList("-O"));
            fail("no exception");
        } catch (ArgumentException e) {
            assertEquals("Missing value after -O", e.getMessage());
        }
    }

    @Test
    public void testApply() {
        AtomicReference<String> ref = new AtomicReference<>();
        Option opt = new Option("--opt", "O", "o", "Option", ref::set);

        opt.apply(new ArgumentList("--opt=val", "not"));
        assertEquals("val", ref.get());

        opt = new Option("--opt", "O", "o", "Option", ref::set);
        opt.apply(new ArgumentList("--opt", "other", "not"));
        assertEquals("other", ref.get());

        opt.validate();
    }

    @Test
    public void testApplyFails() {
        AtomicReference<String> ref = new AtomicReference<>();
        Option opt = new Option("--opt", "O", "o", "Option", ref::set);

        opt.apply(new ArgumentList("--opt", "other", "not"));
        try {
            opt.apply(new ArgumentList("--opt", "other", "not"));
            fail("no exception");
        } catch (ArgumentException e) {
            assertEquals("Option --opt already applied", e.getMessage());
        }

        opt = new Option("--opt", "O", "o", "Option", ref::set);
        try {
            opt.apply(new ArgumentList("--not", "other", "not"));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument not matching option --opt: --not", e.getMessage());
        }

        opt = new Option("--opt", "O", "o", "Option", ref::set);
        try {
            opt.apply(new ArgumentList("--opt"));
            fail("no exception");
        } catch (ArgumentException e) {
            assertEquals("Missing value after --opt", e.getMessage());
        }

        opt = new Option(null, "O", "o", "Option", ref::set);
        try {
            opt.apply(new ArgumentList("--opt", "value"));
            fail("no exception");
        } catch (IllegalStateException e) {
            assertEquals("No long option for -[O]", e.getMessage());
        }
    }

    @Test
    public void testApplyRepeated() {
        LinkedList<String> values = new LinkedList<>();
        Option opt = new Option("--opt", "O", "o", "Option", values::add, null, true, false, false);

        opt.applyShort("Oval", new ArgumentList("-Oval", "not"));
        opt.applyShort("O", new ArgumentList("-O", "other", "not"));
        opt.apply(new ArgumentList("--opt", "third", "not"));

        assertEquals((List) ImmutableList.of("val", "other", "third"), values);

        opt.validate();
    }

    @Test
    public void testApplyRequired() {
        AtomicReference<String> ref = new AtomicReference<>();
        Option opt = new Option("--opt", "O", "o", "Option", ref::set, null, false, true, false);

        try {
            opt.validate();
        } catch (ArgumentException e) {
            assertEquals("Option --opt is required", e.getMessage());
        }

        opt.apply(new ArgumentList("--opt", "val"));
        opt.validate();
    }
}
