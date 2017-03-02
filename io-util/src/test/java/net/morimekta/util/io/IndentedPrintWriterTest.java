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
 */package net.morimekta.util.io;

import net.morimekta.util.Binary;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * Testing for IndentedPrintWriter
 */
public class IndentedPrintWriterTest {
    @Test
    public void testIndent() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IndentedPrintWriter writer = new IndentedPrintWriter(baos);

        writer.append('{');

        writer.begin();

        writer.appendln("next with more indents:");

        writer.begin("        ");

        writer.formatln("%d", 12345);

        writer.end();
        writer.end();

        writer.appendln('}');

        writer.flush();

        assertEquals("{\n" +
                     "    next with more indents:\n" +
                     "            12345\n" +
                     "}", new String(baos.toByteArray(), UTF_8));
    }

    @Test
    public void testBadEnds() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IndentedPrintWriter writer = new IndentedPrintWriter(baos);

        try {
            writer.end();
        } catch (IllegalStateException e) {
            assertEquals("No indent to end", e.getMessage());
        }
    }

    @Test
    public void testExtraMethods() {
        Locale.setDefault(Locale.ENGLISH);

        // testing methods that just wraps PrintWriter methods.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IndentedPrintWriter writer = new IndentedPrintWriter(new PrintWriter(baos));

        IndentedPrintWriter tmp = writer.printf("en %f\n", 1.0)
                                        .printf(Locale.FRANCE, "fr %f\n", 1.0)
                                        .format("en %f\n", 1.0)
                                        .format(Locale.FRANCE, "fr %f\n", 1.0)
                                        .append("|ԥ\n")
                                        .append("B㡧-", 1, 2)
                                        .append('㚉');
        tmp.flush();
        assertEquals("en 1.000000\n" +
                     "fr 1,000000\n" +
                     "en 1.000000\n" +
                     "fr 1,000000\n" +
                     "|ԥ\n" +
                     "㡧㚉", new String(baos.toByteArray(), UTF_8).replaceAll("\\r\\n", "\n"));
    }

    @Test
    public void testPrintln() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IndentedPrintWriter writer = new IndentedPrintWriter(new PrintWriter(baos));

        writer.append('{');
        writer.begin("  ");

        writer.println(false);
        writer.println((byte) 0x7f);
        writer.println((short) 0x2000);

        writer.println();

        writer.println(1234567890);
        writer.println(0xcafebabedeadbeefL);
        writer.println(1234.5677f);  // 1234.5678 gets 'rounded' to 1234.5677 for floats.
        writer.println(6.62607004e-34);

        writer.println();

        writer.println(new char[]{'㡧', '㚉'});
        writer.println("dead\tbeef");
        writer.println(Binary.wrap(new byte[]{4, 3, 2, 1}));

        writer.end();
        writer.println('}');

        writer.flush();
        assertEquals("{\n" +
                     "  false\n" +
                     "  127\n" +
                     "  8192\n" +
                     "\n" +
                     "  1234567890\n" +
                     "  -3819410105021120785\n" +
                     "  1234.5677\n" +
                     "  6.62607004E-34\n" +
                     "\n" +
                     "  㡧㚉\n" +
                     "  dead\tbeef\n" +
                     "  binary(04030201)\n" +
                     "}", new String(baos.toByteArray(), UTF_8));
    }
}
