package net.morimekta.util.concurrent;

import net.morimekta.util.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for the integration executor.
 */
public class ProcessExecutorTest {
    @Rule
    public  TemporaryFolder tmp = new TemporaryFolder();

    private File integration;

    @Before
    public void setUp() throws IOException {
        integration = tmp.newFile("integration.jar");
        try (OutputStream out = new FileOutputStream(integration)) {
            IOUtils.copy(getClass().getResourceAsStream("/integration.zip"),
                         out);
        }
    }

    @Test
    public void testRun() throws IOException {
        File sh = tmp.newFile("tmp.sh");
        try (OutputStream out = new FileOutputStream(sh)) {
            out.write(("#!/bin/sh\n" +
                       "cd $(dirname $0)\n" +
                       "ls -1\n").getBytes(UTF_8));
        }
        ProcessExecutor sut = new ProcessExecutor("sh", sh.getAbsolutePath());

        assertThat(sut.call(), is(0));
        assertThat(sut.getOutput(), is("integration.jar\n" +
                                       "tmp.sh\n"));
        assertThat(sut.getError(), is(""));
    }

    @Test
    public void testTimesOut() throws IOException {
        File sh = tmp.newFile("tmp.sh");
        try (OutputStream out = new FileOutputStream(sh)) {
            out.write(("#!/usr/bin/env bash\n" +
                       "sleep 10\n").getBytes(UTF_8));
        }
        ProcessExecutor sut = new ProcessExecutor(
                "sh", sh.getAbsolutePath(),
                "a'boo'b",
                "must be quoted");
        sut.setDeadlineMs(100);

        try {
            sut.call();
            fail("No exception on deadline exceeded.");
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(), startsWith("java.io.IOException: Process took too long: sh "));
            assertThat(e.getMessage(), endsWith(" \"a\\'boo\\'b\" \"must be quoted\""));
        }
    }

    @Test
    public void testRun_noDeadline() {

    }

    @Test
    public void testRun_withInput() throws IOException {
        ProcessExecutor sut = new ProcessExecutor(
                "java",
                "-jar", integration.getAbsolutePath(),
                "cat", "chemistry");

        ByteArrayInputStream in = new ByteArrayInputStream("one step ahead ;)".getBytes(UTF_8));
        sut.setInput(in);

        assertThat(sut.call(), is(0));
        assertThat(sut.getOutput(),
                   is("len: 2\n" +
                     "one step ahead ;)\n" +
                     "+ cat\n"));
        assertThat(sut.getError(), is("- chemistry\n"));
    }

    @Test
    public void testRun_partialOutput() throws IOException, InterruptedException, ExecutionException {
        File sh = tmp.newFile("tmp.sh");
        try (OutputStream out = new FileOutputStream(sh)) {
            out.write(("#!/usr/bin/env bash\n" +
                       "echo first line\n" +
                       "sleep 1\n" +
                       "echo second line\n").getBytes(UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProcessExecutor sut = new ProcessExecutor(
                "sh", sh.getAbsolutePath(),
                "a'boo'b",
                "must be quoted");
        sut.setDeadlineMs(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> i = executor.submit(sut);
        Thread.sleep(100);

        assertThat(sut.getOutput(), is("first line\n"));
        assertThat(i.get(), is(0));
        assertThat(sut.getOutput(), is("first line\n" +
                                       "second line\n"));
    }
}
