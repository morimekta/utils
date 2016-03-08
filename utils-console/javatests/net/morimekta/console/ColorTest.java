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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Tests for Color utility class.
 */
public class ColorTest {
    @Test
    public void testColorConstants() {
        assertEquals("\033[00m", Color.CLEAR.toString());

        assertEquals("\033[39m", Color.DEFAULT.toString());
        assertEquals("\033[30m", Color.BLACK.toString());
        assertEquals("\033[31m", Color.RED.toString());
        assertEquals("\033[32m", Color.GREEN.toString());
        assertEquals("\033[33m", Color.YELLOW.toString());
        assertEquals("\033[34m", Color.BLUE.toString());
        assertEquals("\033[35m", Color.MAGENTA.toString());
        assertEquals("\033[36m", Color.CYAN.toString());
        assertEquals("\033[37m", Color.WHITE.toString());

        assertEquals("\033[49m", Color.BG_DEFAULT.toString());
        assertEquals("\033[40m", Color.BG_BLACK.toString());
        assertEquals("\033[41m", Color.BG_RED.toString());
        assertEquals("\033[42m", Color.BG_GREEN.toString());
        assertEquals("\033[43m", Color.BG_YELLOW.toString());
        assertEquals("\033[44m", Color.BG_BLUE.toString());
        assertEquals("\033[45m", Color.BG_MAGENTA.toString());
        assertEquals("\033[46m", Color.BG_CYAN.toString());
        assertEquals("\033[47m", Color.BG_WHITE.toString());

        assertEquals("\033[01m", Color.BOLD.toString());
        assertEquals("\033[02m", Color.DIM.toString());
        assertEquals("\033[04m", Color.UNDERLINE.toString());
        assertEquals("\033[09m", Color.STROKE.toString());
        assertEquals("\033[07m", Color.INVERT.toString());
        assertEquals("\033[08m", Color.HIDDEN.toString());

        assertEquals("\033[21m", Color.UNSET_BOLD.toString());
        assertEquals("\033[22m", Color.UNSET_DIM.toString());
        assertEquals("\033[24m", Color.UNSET_UNDERLINE.toString());
        assertEquals("\033[29m", Color.UNSET_STROKE.toString());
        assertEquals("\033[27m", Color.UNSET_INVERT.toString());
        assertEquals("\033[28m", Color.UNSET_HIDDEN.toString());
    }

    @Test
    public void testConstructor() {
        Color br = new Color(31, 1);  // bold red.

        assertEquals("\033[01;31m", br.toString());

        Color bg = new Color(32, 1);  // bold green

        assertEquals("\033[01;32m", bg.toString());

        assertEquals(new Color(Color.BOLD, Color.RED), br);
        assertNotEquals(new Color(Color.BOLD, Color.RED), bg);

        assertEquals(Color.CLEAR, new Color(Color.BOLD, Color.RED, Color.CLEAR));
        assertEquals(new Color(Color.RED, Color.UNSET_BOLD), new Color(new Color(Color.BOLD, Color.RED), Color.UNSET_BOLD));
        assertEquals("\033[01;31;46m", new Color(Color.BOLD, Color.RED, Color.BG_CYAN).toString());
    }

    @Test
    public void testConstructor_parser() {
        Color br = new Color(Color.BOLD, Color.RED);  // bold red.

        assertEquals(new Color(Color.BOLD, Color.RED), new Color("\033[01;31m"));
        try {
            new Color("\033[01;m");
            fail("No exception on invalid color sequence.");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid color control sequence: \"\\033[01;m\"", e.getMessage());
        }
    }

    @Test
    public void testEquals() {
        assertEquals(Color.RED, Color.RED);
        assertNotEquals(Color.RED, Color.BG_RED);
        assertNotEquals(Color.RED, null);
        assertNotEquals(Color.RED, new Object());
    }
}
