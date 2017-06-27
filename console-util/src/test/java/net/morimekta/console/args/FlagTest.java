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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for flag arguments.
 */
public class FlagTest {
    @Test
    public void testConstructor() {
        AtomicReference<Boolean> ref = new AtomicReference<>();
        Flag flag = new Flag("--stuff", "S", "Do stuff", ref::set);

        assertNull(flag.getSingleLineUsage());
        assertNull(flag.getNegateName());
        assertEquals("Do stuff", flag.getUsage());
        assertNull(flag.getDefaultValue());
        assertFalse(flag.isHidden());
        assertFalse(flag.isRepeated());
        assertFalse(flag.isRequired());

        flag.validate();

        flag = new Flag("--stuff", null, "Do stuff", ref::set, false, "--no-stuff");
        assertEquals("[--stuff]", flag.getSingleLineUsage());
        assertEquals("--no-stuff", flag.getNegateName());
        assertEquals("false", flag.getDefaultValue());
        assertFalse(flag.isHidden());
        assertFalse(flag.isRepeated());
        assertFalse(flag.isRequired());

        flag.validate();

        flag = new Flag(null, "S", "Do stuff", ref::set);
        assertNull(flag.getSingleLineUsage());

        try {
            new Flag(null, null, "Fail", ref::set);
            fail("No exception on invalid constructor");
        } catch (IllegalArgumentException e){
            assertEquals("Option must have name or short name", e.getMessage());
        }
    }

    @Test
    public void testApply() {
        AtomicReference<Boolean> ref = new AtomicReference<>();

        Flag flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.applyShort("S", new ArgumentList("-S"));
        assertTrue(ref.get());

        flag = new Flag("--stuff", "S", "Do stuff", ref::set, true, "--no_stuff");
        flag.apply(new ArgumentList("--no_stuff"));
        assertFalse(ref.get());

        flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.apply(new ArgumentList("--stuff"));
        assertTrue(ref.get());

        flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.apply(new ArgumentList("--stuff=FALSE"));
        assertFalse(ref.get());

        flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.apply(new ArgumentList("--stuff=TRUE"));
        assertTrue(ref.get());
    }

    @Test
    public void testApplyFails() {
        AtomicReference<Boolean> ref = new AtomicReference<>();

        Flag flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.applyShort("SS", new ArgumentList("-SS"));
        try {
            flag.applyShort("S", new ArgumentList("-SS"));
            fail("No exception on over-applying");
        } catch (ArgumentException e) {
            assertEquals("--stuff is already applied", e.getMessage());
        }

        flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        flag.apply(new ArgumentList("--stuff"));
        try {
            flag.apply(new ArgumentList("--no_stuff"));
            fail("No exception on over-applying");
        } catch (ArgumentException e) {
            assertEquals("--stuff is already applied", e.getMessage());
        }

        flag = new Flag(null, "S", "Do stuff", ref::set);
        try {
            flag.apply(new ArgumentList("--S"));
            fail("No exception on mis-applying");
        } catch (IllegalStateException e) {
            assertEquals("No long option for -[S]", e.getMessage());
        }

        flag = new Flag("--stuff", "S", "Do stuff", ref::set);
        try {
            flag.apply(new ArgumentList("--S"));
            fail("No exception on mis-applying");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument not matching flag --stuff: --S", e.getMessage());
        }
    }
}
