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
package net.morimekta.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Stein Eldar Johnsen
 * @since 18.10.15
 */
public class StringsTest {
    @Test
    public void testJavaEscape() {
        assertEquals("abcde", Strings.escape("abcde"));
        assertEquals("a\\nb\\rc\\fd\\te\\b.", Strings.escape("a\nb\rc\fd\te\b."));
        assertEquals("\\u2002.䀂.\\\".\\'.", Strings.escape("\u2002.\u4002.\".\'."));
        assertEquals("\\000\\177\\033",
                     Strings.escape("\000\177\033"));
    }

    @Test
    public void testJoin() {
        assertEquals("a,b", Strings.join(",", 'a', 'b'));
        assertEquals("a,b", Strings.join(",", "a", "b"));
        List<String> tmp = new LinkedList<>();
        Collections.addAll(tmp, "a", "b");
        assertEquals("a;b", Strings.join(";", tmp));
    }

    @Test
    public void testIsInteger() {
        assertTrue(Strings.isInteger("0"));
        assertTrue(Strings.isInteger("1"));
        assertTrue(Strings.isInteger("1234567890"));
        assertTrue(Strings.isInteger("-1234567890"));

        assertFalse(Strings.isInteger("+2"));
        assertFalse(Strings.isInteger("beta"));
        assertFalse(Strings.isInteger("    -5 "));
        assertFalse(Strings.isInteger("0x44"));  // hex not supported.
        assertFalse(Strings.isInteger(""));
    }

    @Test
    public void testTimes() {
        assertEquals("bbbbb", Strings.times("b", 5));
    }

    @Test
    public void testCamelCase() {
        assertEquals("", Strings.camelCase("", ""));
        assertEquals("getMyThing", Strings.camelCase("get", "my_thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my.thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my-thing"));
        assertEquals("getMyThing", Strings.camelCase("get", "my...thing"));
        assertEquals("MyThing", Strings.camelCase("", "my_thing"));
    }

    @Test
    public void testC_case() {
        assertEquals("", Strings.c_case("", ""));
        assertEquals("", Strings.c_case("", "", ""));

        assertEquals("get_my_thing_now", Strings.c_case("get_", "MyThing", "_now"));
        assertEquals("get_abbr_now", Strings.c_case("get_", "ABBR", "_now"));
        assertEquals("get_pascal_is_not_nice_now", Strings.c_case("get_", "Pascal_Is_Not_Nice", "_now"));

        // TODO: This case should be possible to split to: "get_abbr_and_more_now".
        assertEquals("get_abbrand_more_now", Strings.c_case("get_", "ABBRAndMore", "_now"));
    }

    @Test
    public void testAsString_double() {
        assertEquals("1234.5678", Strings.asString(1234.5678d));
        assertEquals("1234", Strings.asString(1234.0d));
        assertEquals("1.23456789E14", Strings.asString(123456789000000.0d));
    }

    @Test
    public void testAsString_binary() {
        assertEquals("null", Strings.asString((Binary) null));
        assertEquals("[AAECAwQ]", Strings.asString(Binary.wrap(new byte[]{0, 1, 2, 3, 4})));
    }

    @Test
    public void testAsString_collection() {
        assertEquals("null", Strings.asString((Collection) null));
        assertEquals("[]", Strings.asString(Collections.EMPTY_LIST));
        assertEquals("[]", Strings.asString(Collections.EMPTY_SET));
        assertEquals("[12.4,22,1.23456789E16]",
                     Strings.asString(ImmutableSet.of(12.4, 22, 12345678900000000d)));
    }

    @Test
    public void testAsString_map() {
        assertEquals("null", Strings.asString((Map<?, ?>) null));
        assertEquals("{}", Strings.asString(Collections.EMPTY_MAP));
        assertEquals("{12.4:\"a\",22:\"b a b\",1.23456789E16:\"c\"}",
                     Strings.asString(ImmutableMap.of(12.4, "a",
                                                      22, "b a b",
                                                      12345678900000000d, "c")));
    }

    @Test
    public void testAsString_object() {
        assertAsString("null", null);
        assertAsString("[]", Collections.EMPTY_LIST);
        assertAsString("[]", Collections.EMPTY_SET);
        assertAsString("{}", Collections.EMPTY_MAP);
        assertAsString("[Bw]", Binary.wrap(new byte[]{7}));
        assertAsString("[Bw]", Binary.wrap(new byte[]{7}));
        assertAsString("5", new IsNumeric());
        assertAsString("tmp", new IsStringable());
    }

    private static class IsStringable implements Stringable {
        @Override
        public String asString() {
            return "tmp";
        }
    }

    private static class IsNumeric implements Numeric {
        @Override
        public int asInteger() {
            return 5;
        }
    }

    private void assertAsString(String expected, Object value) {
        assertEquals(expected, Strings.asString(value));
    }

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Strings> c = Strings.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());

        c.setAccessible(true);
        c.newInstance();  // to make code coverage 100%.
        c.setAccessible(false);
    }
}
