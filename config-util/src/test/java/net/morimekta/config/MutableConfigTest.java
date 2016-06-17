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
package net.morimekta.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MutableConfigTest {
    @Test
    public void testConfig() {
        Config config = new MutableConfig();

        assertTrue(config.isEmpty());
        assertEquals(0, config.size());
    }

    @Test
    public void testStringValue() throws ConfigException {
        MutableConfig builder = new MutableConfig();

        assertSame(builder, builder.putString("a", "b"));
        assertEquals("b", builder.getString("a"));

        assertSame(builder, builder.putString("b.c", "d"));
        assertEquals("d", builder.getString("b.c"));
    }

    @Test
    public void testLongValue() throws ConfigException {
        MutableConfig builder = new MutableConfig();

        assertSame(builder, builder.putLong("a", 1234567890L));
        assertEquals(1234567890L, builder.getLong("a"));

        assertSame(builder, builder.putLong("b.c", 9876543210L));
        assertEquals(9876543210L, builder.getLong("b.c"));
    }
}
