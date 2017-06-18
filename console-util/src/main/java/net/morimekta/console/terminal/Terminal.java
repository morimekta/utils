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
package net.morimekta.console.terminal;

import com.google.common.annotations.VisibleForTesting;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharReader;
import net.morimekta.console.chr.Control;
import net.morimekta.console.util.STTY;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Terminal interface. It sets proper TTY mode and reads complex characters
 * from the input, and writes lines dependent on terminal mode.
 */
public class Terminal extends CharReader implements Closeable, LinePrinter {
    private final STTY tty;
    /**
     * Construct a default RAW terminal.
     *
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal() {
        this(new STTY());
    }

    /**
     * Construct a default RAW terminal.
     *
     * @param tty The terminal device.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTY tty) {
        this(tty, STTYMode.RAW, null);
    }

    /**
     * Construct a terminal with given mode.
     *
     * @param tty The terminal device.
     * @param mode The terminal mode.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTY tty, STTYMode mode) {
        this(tty, mode, null);
    }

    /**
     * Construct a terminal with a custom line printer.
     *
     * @param tty The terminal device.
     * @param lp The line printer.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTY tty, LinePrinter lp) {
        this(tty, STTYMode.RAW, lp);
    }

    /**
     * Construct a terminal with a terminal mode and custom line printer.
     *
     * @param tty The terminal device.
     * @param mode The terminal mode.
     * @param lp The line printer.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTY tty, STTYMode mode, LinePrinter lp) {
        this(tty, System.in, System.out, lp, tty.setSTTYMode(mode));
    }

    /**
     * Constructor visible for testing.
     *
     * @param tty The terminal device.
     * @param in The input stream.
     * @param out The output stream.
     * @param lp The line printer or null.
     * @param switcher TTY mode switcher.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    @VisibleForTesting
    public Terminal(STTY tty, InputStream in, OutputStream out, LinePrinter lp, STTYModeSwitcher switcher) {
        super(in);
        this.tty = tty;
        this.lp = lp == null ? this::printlnInternal : lp;
        this.out = out;
        this.switcher = switcher;
        this.lineCount = 0;
        try {
            if (switcher.didChangeMode() && switcher.getBefore() == STTYMode.RAW) {
                this.out.write('\n');
                this.out.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return Get the terminal device.
     */
    public STTY getTTY() {
        return tty;
    }

    /**
     * Make a user confirmation. E.g.:
     *
     * <code>boolean really = term.confirm("Do you o'Really?");</code>
     *
     * Will print out "<code>Do you o'Really? [Y/n]: </code>". If the user press
     * 'y' and 'enter' will pass (return true), if 'n', and 'backspace' will return
     * false. Invalid characters will print a short error message.
     *
     * @param what What to confirm. Basically the message before '[Y/n]'.
     * @return Confirmation result.
     */
    public boolean confirm(String what) {
        return confirm(what, true);
    }

    /**
     * Make a user confirmation. E.g.:
     *
     * <code>boolean really = term.confirm("Do you o'Really?", false);</code>
     *
     * Will print out "<code>Do you o'Really? [y/N]: </code>". If the user press
     * 'y' will pass (return true), if 'n', 'enter' and 'backspace' will return
     * false. Invalid characters will print a short error message.
     *
     * @param what What to confirm. Basically the message before '[Y/n]'.
     * @param def the default response on 'enter'.
     * @return Confirmation result.
     */
    public boolean confirm(String what, boolean def) {
        String yn = def ? "Y/n" : "y/N";
        formatln("%s [%s]:", what, yn);

        try {
            for (;;) {
                Char c = read();
                if (c == null) {
                    throw new IOException("End of stream.");
                }
                int cp = c.asInteger();
                if (cp == Char.CR || cp == Char.LF) {
                    print(def ? " Yes." : " No.");
                    return def;
                }
                if (cp == 'y' || cp == 'Y') {
                    print(" Yes.");
                    return true;
                }
                if (cp == 'n' || cp == 'N' || cp == Char.DEL) {
                    print(" No.");
                    return false;
                }
                if (cp == Char.ESC || cp == Char.ABR || cp == Char.EOF) {
                    throw new IOException("User interrupted: " + c.asString());
                }
                if (cp == ' ' || cp == '\t') {
                    continue;
                }
                format("\r%s%s [%s]: '%s' is not valid input.",
                       Control.CURSOR_ERASE,
                       what, yn, c.asString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Execute callable, which may not be interruptable by itself, but listen to
     * terminal input and abort the task if CTRL-C is pressed.
     *
     * @param exec The executor to run task on.
     * @param callable The callable function.
     * @param <T> The return type of the callable.
     * @return The result of the callable.
     * @throws IOException If aborted or read failure.
     * @throws InterruptedException If interrupted while waiting.
     * @throws ExecutionException If execution failed.
     */
    public <T> T executeAbortable(ExecutorService exec, Callable<T> callable)
            throws IOException, InterruptedException, ExecutionException {
        Future<T> task = exec.submit(callable);
        waitAbortable(task);
        return task.get();
    }

    /**
     * Execute runnable, which may not be interruptable by itself, but listen to
     * terminal input and abort the task if CTRL-C is pressed.
     *
     * @param exec The executor to run task on.
     * @param callable The runnable function.
     * @throws IOException If aborted or read failure.
     * @throws InterruptedException If interrupted while waiting.
     * @throws ExecutionException If execution failed.
     */
    public void executeAbortable(ExecutorService exec, Runnable callable)
            throws IOException, InterruptedException, ExecutionException {
        Future<?> task = exec.submit(callable);
        waitAbortable(task);
        task.get();
    }

    /**
     * Wait for future task to be done or canceled. React to terminal
     * induced abort (ctrl-C) and cancel the task if so.
     *
     * @param task The task to wait for.
     * @param <T> The task generic type.
     * @throws IOException On aborted or read failure.
     * @throws InterruptedException On thread interrupted.
     */
    public <T> void waitAbortable(Future<T> task) throws IOException, InterruptedException {
        while (!task.isDone() && !task.isCancelled()) {
            Char c = readIfAvailable();
            if (c != null && c.asInteger() == Char.ABR) {
                task.cancel(true);
                throw new IOException("Aborted with '" + c.asString() + "'");
            }
            Thread.sleep(79L);
        }
    }

    public void format(String format, Object... args) {
        print(String.format(format, args));
    }

    public void print(Char ch) {
        print(ch.toString());
    }

    public void print(String message) {
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void println(String message) {
        lp.println(message);
    }

    public void println() {
        lp.println(null);
    }

    /**
     * Finish the current set of lines and continue below.
     */
    public void finish() {
        try {
            if (switcher.getCurrentMode() == STTYMode.RAW && lineCount > 0) {
                out.write('\r');
                out.write('\n');
                out.flush();
            }
            lineCount = 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (switcher.didChangeMode() && switcher.getBefore() == STTYMode.COOKED) {
                finish();
            }
        } catch (UncheckedIOException e) {
            // Ignore.
        }
        switcher.close();
    }

    private void printlnInternal(String message) {
        try {
            if (switcher.getCurrentMode() == STTYMode.RAW) {
                lnBefore(message);
            } else {
                lnAfter(message);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void lnAfter(String message) throws IOException {
        if (message != null) {
            out.write('\r');
            out.write(message.getBytes(StandardCharsets.UTF_8));
        }
        out.write('\r');
        out.write('\n');
        out.flush();

        ++lineCount;
    }

    private void lnBefore(String message) throws IOException {
        if (lineCount > 0) {
            out.write('\r');
            out.write('\n');
        }
        if (message != null) {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();

        ++lineCount;
    }

    private final STTYModeSwitcher switcher;
    private final OutputStream out;
    private final LinePrinter  lp;

    private int lineCount;
}
