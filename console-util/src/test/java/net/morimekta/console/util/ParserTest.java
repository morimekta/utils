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
package net.morimekta.console.util;

import net.morimekta.config.impl.SimpleConfig;
import net.morimekta.console.args.ArgumentException;

import com.google.common.util.concurrent.AtomicDouble;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static net.morimekta.console.util.Parser.dbl;
import static net.morimekta.console.util.Parser.dir;
import static net.morimekta.console.util.Parser.file;
import static net.morimekta.console.util.Parser.i32;
import static net.morimekta.console.util.Parser.i64;
import static net.morimekta.console.util.Parser.oneOf;
import static net.morimekta.console.util.Parser.outputDir;
import static net.morimekta.console.util.Parser.outputFile;
import static net.morimekta.console.util.Parser.path;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * TODO(morimekta): Make a real class description.
 */
public class ParserTest {
    @Rule
    public TemporaryFolder tmp;

    private File tempFile;
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tmp = new TemporaryFolder();
        tmp.create();

        tempFile = tmp.newFile("file");
        tempDir = tmp.newFolder("dir");

        tempFile.createNewFile();
        tempDir.mkdirs();
    }

    @Test
    public void testAndThen() {
        AtomicInteger integer = new AtomicInteger();
        SimpleConfig config = new SimpleConfig();

        Parser.IntegerParser parser = new Parser.IntegerParser();

        parser.andApply(integer::set).accept("4");
        assertEquals(4, integer.get());

        parser.andPut(config::putInteger).put("first", "4");
        assertEquals(4, config.getInteger("first"));

        parser.andPutAs(config::putInteger, "second").accept("6");
        assertEquals(6, config.getInteger("second"));
    }

    @Test
    public void testPutInto() {
        SimpleConfig config = new SimpleConfig();

        Parser.putAs(config::putString, "test").accept("value");
        assertEquals("value", config.getString("test"));
    }

    @Test
    public void testI32() {
        AtomicInteger integer = new AtomicInteger();

        i32(integer::set).accept("4");
        assertEquals(4, integer.get());

        try {
            i32(integer::set).accept("b");
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertEquals("Invalid integer value b", e.getMessage());
        }
    }

    @Test
    public void testI64() {
        AtomicLong integer = new AtomicLong();

        i64(integer::set).accept("4");
        assertEquals(4L, integer.get());

        try {
            i64(integer::set).accept("b");
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertEquals("Invalid long value b", e.getMessage());
        }
    }

    @Test
    public void testDBL() {
        AtomicDouble integer = new AtomicDouble();

        dbl(integer::set).accept("4.7");
        assertEquals(4.7, integer.get(), 0.001);

        try {
            dbl(integer::set).accept("b");
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertEquals("Invalid double value b", e.getMessage());
        }
    }

    private enum E {
        A,
        B,
    }

    @Test
    public void testOneOf() {
        AtomicReference<E> value = new AtomicReference<>(E.A);

        oneOf(E.class, value::set).accept("B");

        assertEquals(E.B, value.get());

        try {
            oneOf(E.class).andApply(value::set).accept("not");
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertEquals("Invalid E value not", e.getMessage());
        }
    }

    @Test
    public void testFile() throws IOException {
        AtomicReference<File> f = new AtomicReference<>();

        file(f::set).accept(tempFile.getAbsolutePath());

        assertEquals(tempFile, f.get());

        try {
            File not = tmp.newFile("exists.not");
            not.delete();

            file(f::set).accept(not.getAbsolutePath());
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), startsWith("No such file "));
        }
    }

    @Test
    public void testDir() throws IOException {
        AtomicReference<File> f = new AtomicReference<>();

        dir(f::set).accept(tempDir.getAbsolutePath());

        assertEquals(tempDir, f.get());

        try {
            File not = tmp.newFile("exists.not");
            not.delete();

            dir(f::set).accept(not.getAbsolutePath());
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), startsWith("No such directory "));
        }
    }

    @Test
    public void testOutputFile() throws IOException {
        AtomicReference<File> f = new AtomicReference<>();

        outputFile(f::set).accept(tempFile.getAbsolutePath());
        assertEquals(tempFile, f.get());

        tempFile.delete();
        f.set(null);

        outputFile(f::set).accept(tempFile.getAbsolutePath());
        assertEquals(tempFile, f.get());

        try {
            outputFile(f::set).accept(tempDir.getAbsolutePath());
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), containsString(" exists and is not a file"));
        }
    }

    @Test
    public void testOutputDir() throws IOException {
        AtomicReference<File> f = new AtomicReference<>();

        outputDir(f::set).accept(tempDir.getAbsolutePath());
        assertEquals(tempDir, f.get());

        tempDir.delete();
        f.set(null);

        outputDir(f::set).accept(tempDir.getAbsolutePath());
        assertEquals(tempDir, f.get());

        try {
            outputDir(f::set).accept(tempFile.getAbsolutePath());
            fail("No exception on invalid value.");
        } catch (ArgumentException e) {
            assertThat(e.getMessage(), containsString(" exists and is not a directory"));
        }
    }

    @Test
    public void testPath() {
        AtomicReference<Path> p = new AtomicReference<>();

        path(p::set).accept(tmp.getRoot().getAbsolutePath());
        assertEquals(Paths.get(tmp.getRoot().getAbsolutePath()), p.get());
    }
}
