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

package android.os;

import android.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(BlockJUnit4ClassRunner.class)
public class BaseBundleTest {
    @Test
    public void testConstructor() {
        BaseBundle bundle = new BaseBundleImpl(100);
        assertEquals(0, bundle.size());

        // Check that its writable.
        bundle.putString("KEY", "value");
        assertEquals("value", bundle.getString("KEY"));
    }

    @Test
    public void testEmptyBundle() {
        @SuppressWarnings("unchecked")
        BaseBundle empty = new BaseBundleImpl(Collections.EMPTY_MAP);
        assertEquals(0, empty.size());

        assertTrue(empty.isEmpty());
        assertFalse(empty.containsKey("KEY"));

        try {
            empty.putString("KEY", "value");
            fail("no exception");
        } catch (UnsupportedOperationException ignore) {
        }
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

        assertThat(bundle.get("bool"), is((Object) Boolean.TRUE));
        assertThat(bundle.get("noop"), is(nullValue()));

        assertThat(bundle.getBoolean("bool"), is(true));
        assertThat(bundle.getBoolean("bool", false), is(true));
        assertThat(bundle.getBoolean("noop", false), is(false));

        assertThat(bundle.getDouble("double"), is(4.1234d));
        assertThat(bundle.getDouble("double", 0.0), is(4.1234d));
        assertThat(bundle.getDouble("noop", 1234.5), is(1234.5));

        assertThat(bundle.getDoubleArray("doubleA"), is(new double[]{1.44,2.71,3.14,6.674}));

        assertThat(bundle.getInt("int"), is(1234567890));
        assertThat(bundle.getInt("int", 54321), is(1234567890));
        assertThat(bundle.getInt("boop", 12345), is(12345));

        assertThat(bundle.getIntArray("intA"), is(new int[]{1,2,3,4,5,6,7,8,9,0}));

        assertThat(bundle.getLong("long"), is(1234567890123456789L));
        assertThat(bundle.getLong("long", 987654321098765432L), is(1234567890123456789L));
        assertThat(bundle.getLong("noop", 123456789876543210L), is(123456789876543210L));

        assertThat(bundle.getLongArray("longA"), is(new long[]{12,34,56,78,90,123,456,789}));

        assertThat(bundle.getStringArray("stringA"), is(new String[]{"S", "t", "r", "i", "ng"}));

        BaseBundle other = new BaseBundleImpl(100);
        other.map.putAll(bundle.map);

        assertThat(bundle, is(other));
        assertThat(bundle, is(bundle));
        assertThat(bundle, is(not(new Object())));

        assertThat(bundle.toString(), is(
                "BaseBundleImpl(" +
                "intA=[1,2,3,4,5,6,7,8,9,0]," +
                "stringA=[\"S\",\"t\",\"r\",\"i\",\"ng\"]," +
                "boolA=[true,false]," +
                "double=4.1234," +
                "bool=true," +
                "string=String," +
                "long=1234567890123456789," +
                "longA=[12,34,56,78,90,123,456,789]," +
                "doubleA=[1.44,2.71,3.14,6.674]," +
                "int=1234567890)"));

        assertThat(bundle.hashCode(), is(other.hashCode()));

        assertThat(bundle.containsKey("long"), is(true));
        bundle.remove("long");
        assertThat(bundle.containsKey("long"), is(false));

        assertThat(bundle.hashCode(), is(not(other.hashCode())));

        bundle.clear();
        assertThat(bundle.containsKey("int"), is(false));
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
