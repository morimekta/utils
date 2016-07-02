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

import static net.morimekta.console.util.Parser.i32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the sub-command handling.
 */
public class SubCommandSetTest {
    @Test
    public void testConstructor() {
        @SuppressWarnings("unchecked")
        SubCommandSet<Sub> scs = new SubCommandSet<>("sub", "Subbety sub", this::setSub).addAll(
                new SubCommand<>("cmd_1", "Test command 1", false, Command1::new, Sub::getParser),
                new SubCommand<>("cmd_2", "Test command 1", false, Command2::new, Sub::getParser)
        );

        assertEquals("sub", scs.getName());
        assertEquals("[cmd_1 | cmd_2] [...]", scs.getSingleLineUsage());
        assertEquals("sub", scs.getPrefix());
        assertNull(scs.getDefaultValue());
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
            return new ArgumentParser("test cmd1", "v0.1.1", "Test command 1")
                    .add(new Option("--param_1", null, "str", "Param 1", this::setParam1))
                    .add(new Option("--param_2", null, "int", "Param 2", i32().andThen(this::setParam2)));
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
            return new ArgumentParser("test cmd2", "v0.1.1", "Test command 2")
                    .add(new Option("--param_3", null, "str", "Param 3", this::setParam3))
                    .add(new Option("--param_4", null, "int", "Param 4", i32().andThen(this::setParam4)));
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
