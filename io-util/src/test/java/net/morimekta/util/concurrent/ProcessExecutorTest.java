package net.morimekta.util.concurrent;

import net.morimekta.util.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the integration executor.
 */
public class ProcessExecutorTest {
    @Rule
    public  TemporaryFolder tmp = new TemporaryFolder();

    private File integration;
    private ExecutorService executor;

    @Before
    public void setUp() throws IOException {
        executor = Executors.newFixedThreadPool(3);

        integration = tmp.newFile("integration.jar");
        try (OutputStream out = new FileOutputStream(integration)) {
            IOUtils.copy(getClass().getResourceAsStream("/integration.zip"),
                         out);
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        executor.shutdownNow();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            fail("Oops");
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
        } catch (IOException e) {
            assertThat(e.getMessage(), startsWith("Process took too long: sh "));
            assertThat(e.getMessage(), endsWith(" \"a\\'boo\\'b\" \"must be quoted\""));
        }
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

        Future<Integer> i = executor.submit(sut);
        Thread.sleep(100);

        assertThat(sut.getOutput(), is("first line\n"));
        assertThat(i.get(), is(0));
        assertThat(sut.getOutput(), is("first line\n" +
                                       "second line\n"));
    }

    @Test
    public void testOutputError() throws IOException, InterruptedException {

        InputStream err = mock(InputStream.class);
        when(err.read()).thenThrow(new IOException("stdout"));
        when(err.read(any(byte[].class))).thenThrow(new IOException("stderr"));

        ByteArrayOutputStream in = new ByteArrayOutputStream();
        ByteArrayInputStream out = new ByteArrayInputStream(new byte[]{});

        Process process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(out);
        when(process.getOutputStream()).thenReturn(in);
        when(process.waitFor(anyLong(), eq(TimeUnit.MILLISECONDS))).then(i -> {
            // Trigger the other threads.
            Thread.sleep(10);
            return true;
        });

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(any(String[].class))).thenReturn(process);

        ProcessExecutor sut = new ProcessExecutor(new String[]{
                "ls", "-1"}, runtime, executor);
        try {
            sut.call();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getCause().getMessage(), is("stderr"));
        }
    }

    @Test
    public void testErrorError() throws IOException, InterruptedException {

        InputStream out = mock(InputStream.class);
        when(out.read(any(byte[].class))).thenThrow(new IOException("stdout"));

        ByteArrayOutputStream in = new ByteArrayOutputStream();
        ByteArrayInputStream err = new ByteArrayInputStream(new byte[]{});

        Process process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(out);
        when(process.getOutputStream()).thenReturn(in);
        when(process.waitFor(anyLong(), eq(TimeUnit.MILLISECONDS))).then(i -> {
            // Trigger the other threads.
            Thread.sleep(10);
            return true;
        });

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(any(String[].class))).thenReturn(process);

        ProcessExecutor sut = new ProcessExecutor(new String[]{
                "ls", "-1"}, runtime, executor);
        try {
            sut.call();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getCause().getMessage(), is("stdout"));
        }
    }

    @Test
    public void testInputError() throws IOException, InterruptedException {
        OutputStream in = mock(OutputStream.class);
        doThrow(new IOException("stdin"))
                .when(in).write(any(byte[].class), anyInt(), anyInt());
        doThrow(new IOException("close"))
                .when(in).close();

        ByteArrayInputStream err = new ByteArrayInputStream(new byte[]{});
        ByteArrayInputStream out = new ByteArrayInputStream(new byte[]{});

        Process process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(out);
        when(process.getOutputStream()).thenReturn(in);
        when(process.waitFor(anyLong(), eq(TimeUnit.MILLISECONDS))).then(i -> {
            // Trigger the other threads.
            Thread.sleep(10);
            return true;
        });

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(any(String[].class))).thenReturn(process);

        ProcessExecutor sut = new ProcessExecutor(new String[]{
                "ls", "-1"}, runtime, executor);
        sut.setInput(new ByteArrayInputStream(new byte[]{0, 1, 2}));
        try {
            sut.call();
            fail("no exception");
        } catch (IOException e) {
            // IOException / handleInputInternal
            // IOException / handleInput / read
            assertThat(e.getCause().getMessage(), is("stdin"));
            assertThat(e.getCause().getSuppressed().length, is(1));
            assertThat(e.getCause().getSuppressed()[0].getMessage(), is("close"));
        }
    }

    @Test
    public void testInputError_close() throws IOException, InterruptedException {
        OutputStream in = mock(OutputStream.class);
        doThrow(new IOException("close"))
                .when(in).close();

        ByteArrayInputStream err = new ByteArrayInputStream(new byte[]{});
        ByteArrayInputStream out = new ByteArrayInputStream(new byte[]{});

        Process process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(out);
        when(process.getOutputStream()).thenReturn(in);
        when(process.waitFor(anyLong(), eq(TimeUnit.MILLISECONDS))).then(i -> {
            // Trigger the other threads.
            Thread.sleep(10);
            return true;
        });

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(any(String[].class))).thenReturn(process);

        ProcessExecutor sut = new ProcessExecutor(new String[]{
                "ls", "-1"}, runtime, executor);
        try {
            sut.call();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("close"));
        }
    }

    @Test
    public void testError_interrupted() throws IOException, InterruptedException {
        ByteArrayOutputStream in = new ByteArrayOutputStream();
        ByteArrayInputStream err = new ByteArrayInputStream(new byte[]{});
        ByteArrayInputStream out = new ByteArrayInputStream(new byte[]{});

        Process process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(out);
        when(process.getOutputStream()).thenReturn(in);
        when(process.waitFor(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenThrow(new InterruptedException("waitFor"));

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(any(String[].class))).thenReturn(process);

        ProcessExecutor sut = new ProcessExecutor(new String[]{
                "ls", "-1"}, runtime, executor);
        try {
            sut.call();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getCause().getMessage(), is("waitFor"));
        }
    }
}
