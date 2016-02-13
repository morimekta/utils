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
package net.morimekta.console;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests for FormatString.
 */
public class FormatStringTest {
    @Test
    public void testFormat() {
        FormatString fmt = new FormatString("abba");

        assertEquals("abba", fmt.format());
        assertEquals("abba", fmt.format(new String[0]));

        fmt = new FormatString("abba %5.2f");

        assertEquals("abba  2.50", fmt.format(2.5));
        assertEquals("abba  2,50", fmt.formatWithLocale(Locale.GERMANY, 2.5));
    }

    @Test
    public void testExcept() {
        CmdLineParser parser = new CmdLineParser(new TestOpts());

        CmdLineException ex = FormatString.except(parser, "test");
        assertEquals("test", ex.getMessage());
        assertEquals("test", ex.getLocalizedMessage());

        ex = FormatString.except(parser, "test %s", "abba");
        assertEquals("test abba", ex.getMessage());
        assertEquals("test abba", ex.getLocalizedMessage());
    }

    private static class TestOpts {}
}
