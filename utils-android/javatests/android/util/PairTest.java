/*
 * Copyright (c) 2016, Stein Eldar johnsen
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

package android.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(BlockJUnit4ClassRunner.class)
public class PairTest {
    @Test
    public void testEquals() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b1 = Pair.create(4, "b");
        Pair<Integer, String> b2 = Pair.create(4, "b");

        assertNotEquals(a, b1);
        assertEquals(b1, b2);

        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b1 = Pair.create(4, "b");
        Pair<Integer, String> b2 = Pair.create(4, "b");
        Pair<UUID, String> c1 = Pair.create(null, "c");
        Pair<String, UUID> c2 = Pair.create("c", null);

        assertNotEquals(a.hashCode(), b1.hashCode());
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testToString() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b = Pair.create(4, "b");
        Pair<UUID, String> c = Pair.create(null, "c");

        assertEquals("(4,a)", a.toString());
        assertEquals("(4,b)", b.toString());
        assertEquals("(null,c)", c.toString());
    }
}
