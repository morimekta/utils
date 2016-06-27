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

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.morimekta.console.util.Parser.i32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Argument Parser.
 */
public class ArgumentParserTest {
    @Test
    public void testParse() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        LinkedList<String> args = new LinkedList<>();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        Properties props = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", props::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", "no-type", type::set, s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", null, args::add, null, true, true, false));

        parser.parse("--arg1", "4", "-Dsome.key=not.default", "--arg2", "/iggy", "some-type", "pop");

        assertEquals(4, ai.get());
        assertEquals("not.default", props.getProperty("some.key"));
        assertTrue(ab.get());
        assertEquals("some-type", type.get());
        assertEquals(2, args.size());
        assertEquals("/iggy", args.get(0));
        assertEquals("pop", args.get(1));
    }

    @Test
    public void testParse_alt() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean(true);

        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set)));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Boolean Usage", ab::set));

        parser.parse("--arg1=4", "--no_arg2");

        assertEquals(4, ai.get());
        assertEquals(null, properties.getProperty("some.key"));
        assertFalse(ab.get());
    }

    @Test
    public void testParse_short() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean(false);

        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set)));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", "b", "Boolean Usage", ab::set));

        parser.parse("-ba4");

        assertEquals(4, ai.get());
        assertEquals(null, properties.getProperty("some.key"));
        assertTrue(ab.get());
    }


    @Test
    public void testHelp_long() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Option(null, "A", "I", "Integer value (#2), " +
                                              "This one has a really long description, that should be " +
                                              "wrapped at some point. This extra text is just to make " +
                                              "sure it actually wraps...", i32(ai::set), "55"));
        parser.add(new Option("--this-is-somewhat-long-long-option", null, "V",
                              "ANOTHER Integer value (#3), " +
                              "This one has a really long description, that should be " +
                              "wrapped at some point. This extra text is just to make " +
                              "sure it actually wraps... This one should also be side-" +
                              "shifted on the first line.", i32(ai::set), "55"));
        parser.add(new Option("--this-is-a-really-long-long-option", "cdefgh", "true-value",
                              "ANOTHER Integer value (#2), " +
                              "This one has a really long description, that should be " +
                              "wrapped at some point. This extra text is just to make " +
                              "sure it actually wraps... Though this one should be shifted " +
                              "entirely to the next line.", i32(ai::set), "55"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertEquals(" --arg1 (-a) I          : Integer value [default: 55]\n" +
                     " -Dkey=val              : System property\n" +
                     " --arg2                 : Another boolean\n" +
                     " -A I                   : Integer value (#2), This one has a really long\n" +
                     "                          description, that should be wrapped at some point.\n" +
                     "                          This extra text is just to make sure it actually\n" +
                     "                          wraps... [default: 55]\n" +
                     " --this-is-somewhat-long-long-option V : ANOTHER Integer value (#3), This one\n" +
                     "                          has a really long description, that should be wrapped\n" +
                     "                          at some point. This extra text is just to make sure it\n" +
                     "                          actually wraps... This one should also be side-shifted\n" +
                     "                          on the first line. [default: 55]\n" +
                     " --this-is-a-really-long-long-option (-c, -d, -e, -f, -g, -h) true-value\n" +
                     "                          ANOTHER Integer value (#2), This one has a really long\n" +
                     "                          description, that should be wrapped at some point.\n" +
                     "                          This extra text is just to make sure it actually\n" +
                     "                          wraps... Though this one should be shifted entirely to\n" +
                     "                          the next line. [default: 55]\n", baos.toString());
    }

    @Test
    public void testHelp_noDefaults() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Option(null, "A", "I", "Integer value (#2), " +
                                              "This one has a really long description, that should be " +
                                              "wrapped at some point. This extra text is just to make " +
                                              "sure it actually wraps...", i32(ai::set), "55"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertEquals(" --arg1 (-a) I : Integer value\n" +
                     " -Dkey=val     : System property\n" +
                     " --arg2        : Another boolean\n" +
                     " -A I          : Integer value (#2), This one has a really long description,\n" +
                     "                 that should be wrapped at some point. This extra text is just\n" +
                     "                 to make sure it actually wraps...\n",
                     baos.toString());
    }

    @Test
    public void testHelp_withArgs() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        LinkedList<String> args = new LinkedList<>();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", "no-type", type::set, s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", null, args::add, null, true, true, false));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertEquals(" --arg1 (-a) I : Integer value\n" +
                     " -Dkey=val     : System property\n" +
                     " --arg2        : Another boolean\n" +
                     "\n" +
                     "Available arguments:\n" +
                     " type          : Some type\n" +
                     " file          : Extra files\n",
                     baos.toString());

    }

    @Test
    public void testSingleLineUsage() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        LinkedList<String> args = new LinkedList<>();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", "no-type", type::set, s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", null, args::add, null, true, true, false));

        assertEquals("gt [-a I] [-Dkey=val ...] [--arg2] [type] file...",
                     parser.getSingleLineUsage());
    }

    private static class Sub {
        int i = 12;

        public void setI(int i) {
            this.i = i;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleLineUsage_withSubCommands() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        AtomicReference<Sub> sub = new AtomicReference<>();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new UnaryOption("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", "no-type", type::set, s -> !s.startsWith("/"), false, false, false));

        SubCommandSet<Sub> subs = new SubCommandSet<>("file", "Extra files", sub::set);
        subs.add(new SubCommand<>("sub1", "Sub sub", false, Sub::new,
                                  i -> new ArgumentParser("gt sub1", null, null).add(new Option("--int",
                                                                                                "i",
                                                                                                "I",
                                                                                                "Integer",
                                                                                                i32(i::setI),
                                                                                                "12")),
                                  "s"));
        subs.add(new SubCommand<>("sub2",
                                  "Sub sub",
                                  false,
                                  Sub::new,
                                  i -> new ArgumentParser("gt sub2", null, null).add(new Option("--int",
                                                                                                "i",
                                                                                                "I",
                                                                                                "Integer",
                                                                                                i32(i::setI)))));

        parser.add(subs);

        assertEquals("gt [-a I] [-Dkey=val ...] [--arg2] [type] [sub1 | sub2] [...]", parser.getSingleLineUsage());
    }
}
