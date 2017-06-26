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

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.morimekta.console.util.Parser.i32;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the Argument Parser.
 */
public class ArgumentParserTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testConstructor() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        assertEquals("Git Tools - 0.2.5", parser.getProgramDescription());
        assertEquals("gt", parser.getProgram());
        assertEquals("0.2.5", parser.getVersion());
        assertEquals("Git Tools", parser.getDescription());
    }

    @Test
    public void testParentConstructor() {
        AtomicBoolean help = new AtomicBoolean();
        AtomicBoolean other = new AtomicBoolean();

        ArgumentParser parent = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parent.add(new Flag("--help", "?h", "Help", help::set));

        ArgumentParser parser = new ArgumentParser(parent, "tool", "A Tool");
        parser.add(new Flag("--other", "o", "Other", other::set));

        assertThat(parser.getProgramDescription(), is("A Tool - 0.2.5"));

        parser.parse("--help", "-o");
        assertThat(help.get(), is(true));
        assertThat(other.get(), is(true));
    }

    @Test
    public void testParentConstructor_2() {
        AtomicBoolean help = new AtomicBoolean();
        AtomicBoolean other = new AtomicBoolean();

        ArgumentParser parent = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parent.add(new Flag("--help", "?h", "Help", help::set));

        ArgumentParser parser = new ArgumentParser(parent, "tool", "A Tool");
        parser.add(new Flag("--other", "o", "Other", other::set));

        assertThat(parser.getProgramDescription(), is("A Tool - 0.2.5"));

        parser.parse("-?", "--other");
        assertThat(help.get(), is(true));
        assertThat(other.get(), is(true));
    }

    @Test
    public void testParse_simple() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        LinkedList<String> args = new LinkedList<>();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        Properties props = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", props::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", type::set, "no-type", s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", args::add, null, null, true, true, false));

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

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set)));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Boolean Usage", ab::set));

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

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set)));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", "b", "Boolean Usage", ab::set));

        parser.parse("-ba4");

        assertEquals(4, ai.get());
        assertEquals(null, properties.getProperty("some.key"));
        assertTrue(ab.get());
    }

    @Test
    public void testParse_shortFlag() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicBoolean b1 = new AtomicBoolean(false);
        AtomicBoolean b2 = new AtomicBoolean(false);
        AtomicBoolean b3 = new AtomicBoolean(false);

        parser.add(new Flag("--arg1", "a", "Boolean Usage", b1::set));
        parser.add(new Flag("--arg2", "b", "Boolean Usage", b2::set));
        parser.add(new Flag("--arg3", "c", "Boolean Usage", b3::set));

        parser.parse("-ac");

        assertTrue(b1.get());
        assertFalse(b2.get());
        assertTrue(b3.get());
    }

    @Test
    public void testParse_args() {
        AtomicBoolean      b      = new AtomicBoolean(false);
        LinkedList<String> arg    = new LinkedList<>();
        ArgumentParser     parser = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parser.add(new Flag("--bool", "b", "Bool", b::set));
        parser.add(new Argument("arg", "A", arg::add, "", null, true, true, false));
        parser.parse("first", "--", "--bool");

        assertThat(arg, is(ImmutableList.of("first", "--bool")));
        assertThat(b.get(), is(false));
    }

    @Test
    public void testParse_fails() {
        AtomicBoolean      b      = new AtomicBoolean(false);
        LinkedList<String> arg    = new LinkedList<>();

        ArgumentParser     parser = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parser.add(new Flag("--bool", "b", "Bool", b::set));
        parser.add(new Argument("arg", "A", arg::add, "",
                                s -> !s.equals("bcd"), true, true, false));

        try {
            parser.parse("--boo");
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No option for --boo"));
        }

        try {
            parser.parse("-bcd");
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No short opt for -c"));
        }

        try {
            parser.parse("bcd");
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No option found for bcd"));
        }

        try {
            parser.parse("--", "bcd");
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No argument found for bcd"));
        }
    }

    @Test
    public void testFlagsFile() throws IOException {
        File flags = tmp.newFile("flags");
        try (FileOutputStream fos = new FileOutputStream(flags)) {
            fos.write(("--bool\n" +
                       "--opt the-opt\n" +
                       "\n" +
                       "# comment\n" +
                       "\"foo\\tbar\"\n" +
                       "      baz\n").getBytes(UTF_8));
        }

        AtomicBoolean      b      = new AtomicBoolean(false);
        LinkedList<String> arg    = new LinkedList<>();
        AtomicReference<String> opt = new AtomicReference<>();

        ArgumentParser     parser = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parser.add(new Flag("--bool", "b", "Bool", b::set));
        parser.add(new Option("--opt", "o", "OPT", "Usage", opt::set));
        parser.add(new Argument("arg", "A", arg::add, "", null, true, true, false));

        parser.parse("@" + flags.getCanonicalPath());

        assertThat(b.get(), is(true));
        assertThat(opt.get(), is("the-opt"));
        assertThat(arg, is(ImmutableList.of("foo\tbar", "baz")));

        try (FileOutputStream fos = new FileOutputStream(flags)) {
            fos.write(("--boo\n").getBytes(UTF_8));
        }

        try {
            parser.parse("@" + flags.getCanonicalPath());
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("Argument file flags: No option for --boo"));
        }
    }

    @Test
    public void testValidate() {
        AtomicBoolean      b      = new AtomicBoolean(false);
        LinkedList<String> arg    = new LinkedList<>();
        AtomicReference<String> opt = new AtomicReference<>();

        ArgumentParser     parser = new ArgumentParser("gt", "0.2.5", "Git Tools");
        parser.add(new Option("--opt", "o", "OPT", "Usage", opt::set, null, false, true, false));
        parser.add(new Argument("arg", "A", arg::add, "", null, false, true, false));

        try {
            parser.validate();
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("Option --opt is required"));
        }

        parser.parse("--opt", "O");

        try {
            parser.validate();
            fail("no exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("Argument arg is required"));
        }

        parser.parse("arg");

        parser.validate();
    }

    @Test
    public void testHelp_long() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools");

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Option(null, "A", "I", "Integer value (#2), " +
                                              "This one has a really long description, that should be " +
                                              "wrapped at some point. This extra text is just to make " +
                                              "sure it actually wraps...", i32().andApply(ai::set), "55"));
        parser.add(new Option("--this-is-somewhat-long-long-option", null, "V",
                              "ANOTHER Integer value (#3), " +
                              "This one has a really long description, that should be " +
                              "wrapped at some point. This extra text is just to make " +
                              "sure it actually wraps... This one should also be side-" +
                              "shifted on the first line.", i32().andApply(ai::set), "55"));
        parser.add(new Option("--this-is-a-really-long-long-option", "cdefgh", "true-value",
                              "ANOTHER Integer value (#2), " +
                              "This one has a really long description, that should be " +
                              "wrapped at some point. This extra text is just to make " +
                              "sure it actually wraps... Though this one should be shifted " +
                              "entirely to the next line.", i32().andApply(ai::set), "55"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertEquals(" --arg1 (-a) I          : Integer value (default: 55)\n" +
                     " -Dkey=val              : System property\n" +
                     " --arg2                 : Another boolean\n" +
                     " -A I                   : Integer value (#2), This one has a really long\n" +
                     "                          description, that should be wrapped at some point.\n" +
                     "                          This extra text is just to make sure it actually\n" +
                     "                          wraps... (default: 55)\n" +
                     " --this-is-somewhat-long-long-option V : ANOTHER Integer value (#3), This one\n" +
                     "                          has a really long description, that should be wrapped\n" +
                     "                          at some point. This extra text is just to make sure it\n" +
                     "                          actually wraps... This one should also be side-shifted\n" +
                     "                          on the first line. (default: 55)\n" +
                     " --this-is-a-really-long-long-option (-c, -d, -e, -f, -g, -h) true-value\n" +
                     "                          ANOTHER Integer value (#2), This one has a really long\n" +
                     "                          description, that should be wrapped at some point.\n" +
                     "                          This extra text is just to make sure it actually\n" +
                     "                          wraps... Though this one should be shifted entirely to\n" +
                     "                          the next line. (default: 55)\n", baos.toString());
    }

    @Test
    public void testHelp_noDefaults() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Option(null, "A", "I", "Integer value (#2), " +
                                              "This one has a really long description, that should be " +
                                              "wrapped at some point. This extra text is just to make " +
                                              "sure it actually wraps...", i32().andApply(ai::set), "55"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertThat(baos.toString(),
                   is(equalTo(" --arg1 (-a) I : Integer value\n" +
                   " -Dkey=val     : System property\n" +
                   " --arg2        : Another boolean\n" +
                   " -A I          : Integer value (#2), This one has a really long description,\n" +
                   "                 that should be wrapped at some point. This extra text is just\n" +
                   "                 to make sure it actually wraps...\n")));
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

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", type::set, "no-type", s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", args::add, null, null, true, true, false));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.printUsage(baos);

        assertEquals(" --arg1 (-a) I : Integer value\n" +
                     " -Dkey=val     : System property\n" +
                     " --arg2        : Another boolean\n" +
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

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", type::set, "no-type", s -> !s.startsWith("/"), false, false, false));
        parser.add(new Argument("file", "Extra files", args::add, null, null, true, true, false));

        assertEquals("gt [-a I] [-Dkey=val ...] [--arg2] [type] file...",
                     parser.getSingleLineUsage());
    }

    @Test
    public void testSingleLineUsage_flags() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false));

        parser.add(new Flag("--arg", "a", "Argument flag", s -> {}));
        parser.add(new Flag("--help", "h?", "Show help", s -> {}));
        parser.add(new Argument("file", "File", s -> {}));

        assertEquals("gt [-ah] file",
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
                                                                  .withDefaultsShown(false)
                                                                  .withSubCommandsShown(true));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        AtomicReference<Sub> sub = new AtomicReference<>();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", type::set, "no-type", s -> !s.startsWith("/"), false, false, false));

        SubCommandSet<Sub> subs = new SubCommandSet<>("file", "Extra files", sub::set);
        subs.add(new SubCommand<>("sub1", "Sub sub", false, Sub::new,
                                  i -> new ArgumentParser("gt sub1", null, null).add(new Option("--int",
                                                                                                "i",
                                                                                                "I",
                                                                                                "Integer",
                                                                                                i32().andApply(i::setI),
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
                                                                                                i32().andApply(i::setI)))));

        parser.add(subs);

        assertEquals("gt [-a I] [-Dkey=val ...] [--arg2] [type] [sub1 | sub2] [...]", parser.getSingleLineUsage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPrintUsage_withSubCommands() {
        ArgumentParser parser = new ArgumentParser("gt", "0.2.5", "Git Tools",
                                                   ArgumentOptions.defaults()
                                                                  .withDefaultsShown(false)
                                                                  .withSubCommandsString("Available Subs:")
                                                                  .withSubCommandsShown(true));

        AtomicInteger ai = new AtomicInteger(55);
        AtomicBoolean ab = new AtomicBoolean();
        AtomicReference<String> type = new AtomicReference<>("no-type");
        AtomicReference<Sub> sub = new AtomicReference<>();
        Properties properties = new Properties();

        parser.add(new Option("--arg1", "a", "I", "Integer value", i32().andApply(ai::set), "55"));
        parser.add(new Property('D', "key", "val", "System property", properties::setProperty));
        parser.add(new Flag("--arg2", null, "Another boolean", ab::set));
        parser.add(new Argument("type", "Some type", type::set, "no-type", s -> !s.startsWith("/"), false, false, false));

        SubCommandSet<Sub> subs = new SubCommandSet<>("file", "Extra files", sub::set);
        subs.add(new SubCommand<>("sub1", "Sub sub", false, Sub::new,
                                  i -> new ArgumentParser("gt sub1", null, null).add(new Option("--int",
                                                                                                "i",
                                                                                                "I",
                                                                                                "Integer",
                                                                                                i32().andApply(i::setI),
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
                                                                                                i32().andApply(i::setI)))));

        parser.add(subs);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        parser.printUsage(out);

        assertThat(out.toString(),
                   is(" --arg1 (-a) I : Integer value\n" +
                      " -Dkey=val     : System property\n" +
                      " --arg2        : Another boolean\n" +
                      " type          : Some type\n" +
                      " file          : Extra files\n" +
                      "\n" +
                      "Available Subs:\n" +
                      "\n" +
                      " sub1 : Sub sub\n" +
                      " sub2 : Sub sub\n"));
    }

    @Test
    public void testAdd_fails() {
        AtomicBoolean b = new AtomicBoolean();

        ArgumentParser parent = new ArgumentParser("gt", "v0.1", "GitTool");
        parent.add(new Flag("--parent", "p", "", b::set));
        ArgumentParser parser = new ArgumentParser(parent, "tool", "GitTool");
        parser.add(new Flag("--exists", "e", "", b::set));

        try {
            parser.add(new Flag("--exists", "n", "", b::set));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Option --exists already exists"));
        }

        try {
            parser.add(new Flag("--parent", "n", "", b::set));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Option --parent already exists in parent"));
        }

        try {
            parser.add(new Flag("--new", "e", "", b::set));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Short option -e already exists"));
        }

       try {
            parser.add(new Flag("--new3", "p", "", b::set));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Short option -p already exists in parent"));
        }

        try {
            parser.add((BaseArgument) new Flag("--new2", "-2", "", b::set, null, "--exists"));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Flag --exists already exists"));
        }

        AtomicReference<String> fish = new AtomicReference<>();

        parser.add(new SubCommandSet<>("fish", "Fish", fish::set));
        try {
            parser.add(new Argument("fish", "Fish", fish::set));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("No arguments can be added after a sub-command set"));
        }
    }
}
