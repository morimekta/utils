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

package android.os;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import android.util.Pair;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(BlockJUnit4ClassRunner.class)
public class BaseBundleTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        BaseBundle bundle = new BaseBundleImpl(100);
        assertEquals(0, bundle.size());

        // Check that its writable.
        bundle.putString("KEY", "value");
        assertEquals("value", bundle.getString("KEY"));
    }

    @Test
    public void testEmptyBunble() {
        @SuppressWarnings("unchecked")
        BaseBundle empty = new BaseBundleImpl(Collections.EMPTY_MAP);
        assertEquals(0, empty.size());

        assertTrue(empty.isEmpty());
        assertFalse(empty.containsKey("KEY"));

        // Check that its NOT writable.
        thrown.expect(UnsupportedOperationException.class);
        empty.putString("KEY", "value");
    }

    @Test
    public void testKeySet() {
        BaseBundle bundle = new BaseBundleImpl(10);
        bundle.putBoolean("A", true);
        bundle.putBoolean("B", true);
        bundle.putBoolean("C", false);
        bundle.putBoolean("D", false);
        bundle.putBoolean("E", true);

        Set<String> set = bundle.keySet();

        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertTrue(set.contains("C"));
        assertTrue(set.contains("D"));
        assertTrue(set.contains("E"));
        assertEquals(5, set.size());

        assertTrue(bundle.containsKey("A"));
        assertTrue(bundle.containsKey("B"));
        assertTrue(bundle.containsKey("C"));
        assertTrue(bundle.containsKey("D"));
        assertTrue(bundle.containsKey("E"));
        assertEquals(5, bundle.size());
    }

    @Test
    public void testBoolean() {
        BaseBundle bundle = new BaseBundleImpl(10);
        bundle.putBoolean("A", true);
        bundle.putBoolean("B", false);

        assertTrue(bundle.getBoolean("A"));
        assertFalse(bundle.getBoolean("B"));
        assertTrue(bundle.getBoolean("A", false));
        assertFalse(bundle.getBoolean("B", true));
        assertTrue(bundle.getBoolean("C", true));
        assertFalse(bundle.getBoolean("D", false));
        assertFalse(bundle.getBoolean("E"));

        // wrong type.
        assertEquals(4, bundle.getInt("B", 4));
    }

    @Test
    public void testSimple() {
        BaseBundle bundle = new BaseBundleImpl(100);

        bundle.putBoolean("bool", true);
        bundle.putBooleanArray("boolA", new boolean[]{true, false});
        bundle.putDouble("double", 4.1234d);
        bundle.putDoubleArray("doubleA", new double[]{1.44, 2.71, 3.14, 6.674});
        bundle.putInt("int", 1234567890);
        bundle.putIntArray("intA", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
        bundle.putLong("long", 1234567890123456789L);
        bundle.putLongArray("longA", new long[]{12L, 34L, 56L, 78L, 90L, 123L, 456L, 789L});
        bundle.putString("string", "String");
        bundle.putStringArray("stringA", new String[]{"S", "t", "r", "i", "ng"});
    }

    private class BaseBundleImpl
            extends BaseBundle {
        public BaseBundleImpl(Map<String, Pair<Type, Object>> map) {
            super(map);
        }

        public BaseBundleImpl(int capacity) {
            super(capacity);
        }
    }
}
