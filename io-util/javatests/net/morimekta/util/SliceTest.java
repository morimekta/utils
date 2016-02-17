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

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Stein Eldar Johnsen
 * @since 16.01.16.
 */
public class SliceTest {
    private byte[] data;

    @Before
    public void setUp() {
        data = "This is a -123.45 long \"test ł→Þħ úñí©øð€.\"".getBytes(UTF_8);
    }

    @Test
    public void testSlice() {
        Slice slice = new Slice(data, 0, 7);

        assertEquals(7, slice.length());
        assertEquals("This is", slice.asString());
        assertEquals("slice([0..7>/56)", slice.toString());
        assertEquals('s', slice.charAt(6));

        Slice other = new Slice(data, 0, 22);
        assertEquals(22, other.length());
        assertEquals("This is a -123.45 long", other.asString());
        assertEquals("slice([0..22>/56)", other.toString());
        assertEquals('s', other.charAt(6));

        assertNotEquals(slice, other);

        other = new Slice(data, 0, 7);

        assertEquals(slice, other);
    }

    @Test
    public void testParseInteger() {
        Slice slice = new Slice(data, 11, 3);

        assertEquals(3, slice.length());
        assertEquals("123", slice.asString());
        assertEquals("slice([11..14>/56)", slice.toString());
        assertEquals('1', slice.charAt(0));

        assertEquals(123L, slice.parseInteger());
        assertEquals(123.0, slice.parseDouble(), 0.0001);
    }

    @Test
    public void testParseDouble() {
        Slice slice = new Slice(data, 11, 6);

        assertEquals(6, slice.length());
        assertEquals("123.45", slice.asString());
        assertEquals("slice([11..17>/56)", slice.toString());
        assertEquals('1', slice.charAt(0));

        assertEquals(123.45, slice.parseDouble(), 0.0001);
    }
}
