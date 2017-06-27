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

import com.google.common.base.MoreObjects;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static net.morimekta.console.util.Parser.i32;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for the sub-command handling.
 */
public class SubCommandSetTest {
    @Test
    public void testConstructor() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", Command2::new, Sub::getParser)
        );

        assertEquals("sub", scs.getName());
        assertEquals("[cmd_1 | cmd_2] [...]", scs.getSingleLineUsage());
        assertEquals("sub", scs.getPrefix());
        assertNull(scs.getDefaultValue());
    }

    @Test
    public void testAddBad() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", Command2::new, Sub::getParser)
        );

        try {
            scs.add(new SubCommand<>("cmd_1", "Another cmd1", Command1::new, Sub::getParser));
            fail("No exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("SubCommand with name cmd_1 already exists"));
        }

        try {
            scs.add(new SubCommand<>("cmd_3", "Another cmd1", Command1::new, Sub::getParser, "cmd_1"));
            fail("No exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("SubCommand (cmd_3) alias cmd_1 already exists"));
        }
    }

    @Test
    public void testPrintUsage() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser)
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scs.printUsage(baos);

        assertEquals(" cmd_1 : Test command 1\n" +
                     " cmd_2 : Test command 1\n", baos.toString());

        baos.reset();
        scs.printUsage(baos, "cmd_1");

        assertEquals(" --param_1 str : Param 1\n" +
                     " --param_2 int : Param 2\n", baos.toString());
    }

    @Test
    public void testPrintUsage_hidden() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", true, Command2::new, Sub::getParser)
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scs.printUsage(new PrintWriter(baos));

        assertEquals(" cmd_1 : Test command 1\n", baos.toString());

        baos.reset();
        scs.printUsage(new PrintWriter(baos), "cmd_2");

        assertEquals(" --param_3 str : Param 3\n" +
                     " --param_4 int : Param 4\n", baos.toString());
    }

    @Test
    public void testGetSingleLineUsage() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser));

        assertEquals("test [...] cmd_1 [--param_1 str] [--param_2 int]", scs.getSingleLineUsage("cmd_1"));
        assertEquals("test [...] cmd_2 [--param_3 str] [--param_4 int]", scs.getSingleLineUsage("cmd_2"));
    }

    @Test
    public void testGetSingleLineUsage_long() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 2", false, Command2::new, Sub::getParser),
                new SubCommand<>("cmd_3", "Test command 3", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_4", "Test command 4", false, Command2::new, Sub::getParser),
                new SubCommand<>("cmd_5", "Test command 5", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_6", "Test command 6", false, Command2::new, Sub::getParser));

        assertEquals("sub [...]", scs.getSingleLineUsage());
    }

    @Test
    public void testGetSingleLineUsage_short() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub, "sub_1").addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser));

        assertEquals("[[cmd_1 | cmd_2] [...]]", scs.getSingleLineUsage());
    }

    @Test
    public void testGetSingleLineUsage_bad() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser));

        try {
            scs.getSingleLineUsage("cmd_3");
            fail("No exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No such sub cmd_3"));
        }
    }

    @Test
    public void testPrintUsage_bad() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser));

        try {
            scs.printUsage(new ByteArrayOutputStream(), "cmd_3");
            fail("No exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No such sub cmd_3"));
        }
    }

    @Test
    public void testPipeline_fail() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser));

        try {
            scs.apply(new ArgumentList("boo"));
            fail("No exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("No such sub: boo"));
        }

        scs.apply(new ArgumentList("cmd_1"));
        try {
            scs.apply(new ArgumentList("cms_2"));
            fail("No exception");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), is("sub already selected"));
        }
    }

    @Test
    public void testPipeline() {
        ArgumentParser parser = getParser();
        try {
            parser.validate();
        } catch (ArgumentException e) {
            assertEquals("sub not chosen", e.getMessage());
        }

        parser.parse("--arg", "no-ooo", "cmd_1", "--param_2", "44");
        parser.validate();

        assertEquals("no-ooo", arg);
        assertNotNull(sub);
        assertEquals("Command1{param_2=44}", sub.apply());
    }

    private String arg;
    private Sub sub;

    private void setSub(Sub sub) {
        this.sub = sub;
    }

    private void setArg(String arg) {
        this.arg = arg;
    }

    @SuppressWarnings("unchecked")
    private ArgumentParser getParser() {
        return new ArgumentParser("test", "v0.1.1", "Test")
                .add(new Option("--arg", null, "a", "Argh!", this::setArg))
                .add(new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                        new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                        new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser)
                ));
    }

    private interface Sub {
        String apply();

        ArgumentParser getParser();
    }

    private class Command1 implements Sub {
        public void setParam1(String param_1) {
            this.param_1 = param_1;
        }

        public void setParam2(int param_2) {
            this.param_2 = param_2;
        }

        @Override
        public ArgumentParser getParser() {
            return new ArgumentParser("test [...] cmd_1", "v0.1.1", "Test command 1")
                    .add(new Option("--param_1", null, "str", "Param 1", this::setParam1))
                    .add(new Option("--param_2", null, "int", "Param 2", i32().andApply(this::setParam2)));
        }

        @Override
        public String apply() {
            return MoreObjects.toStringHelper(this)
                              .omitNullValues()
                              .add("param_1", param_1)
                              .add("param_2", param_2)
                              .toString();
        }

        private String param_1;
        private int    param_2;
    }

    private class Command2 implements Sub {
        public void setParam3(String param_1) {
            this.param_3 = param_1;
        }

        public void setParam4(int param_2) {
            this.param_4 = param_2;
        }

        @Override
        public ArgumentParser getParser() {
            return new ArgumentParser("test [...] cmd_2", "v0.1.1", "Test command 2")
                    .add(new Option("--param_3", null, "str", "Param 3", this::setParam3))
                    .add(new Option("--param_4", null, "int", "Param 4", i32().andApply(this::setParam4)));
        }

        @Override
        public String apply() {
            return MoreObjects.toStringHelper(this)
                              .omitNullValues()
                              .add("param_3", param_3)
                              .add("param_4", param_4)
                              .toString();
        }

        private String param_3;
        private int    param_4;
    }
}
