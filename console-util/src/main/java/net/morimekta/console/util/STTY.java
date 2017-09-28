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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A terminal controller helper.
 */
public class STTY {
    private final Runtime                       runtime;
    private final AtomicLong                    termTime;
    private final AtomicReference<TerminalSize> termSize;
    private final AtomicReference<IOException>  ex;
    private final Clock clock;

    public STTY() {
        this(Runtime.getRuntime(), Clock.systemUTC());
    }

    @VisibleForTesting
    public STTY(Runtime runtime, Clock clock) {
        this.runtime = runtime;
        this.ex = new AtomicReference<>();
        this.termSize = new AtomicReference<>();
        this.termTime = new AtomicLong(0L);
        this.clock = clock;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(STTY.class)
                          .omitNullValues()
                          .add("interactive", isInteractive())
                          .add("tty", isInteractive() ? getTerminalSize() : null)
                          .add("mode", STTYModeSwitcher.currentMode())
                          .toString();
    }

    /**
     * Set the current STTY mode, and return the closable switcher to turn back.
     *
     * @param mode The STTY mode to set.
     * @return The mode switcher.
     */
    public STTYModeSwitcher setSTTYMode(STTYMode mode) {
        try {
            return new STTYModeSwitcher(mode, runtime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get the terminal size.
     *
     * @return the terminal size.
     * @throws UncheckedIOException If getting the terminal size failed.
     */
    public TerminalSize getTerminalSize() {
        return Optional.ofNullable(getTerminalSizeInternal())
                       .orElseThrow(() -> new UncheckedIOException(ex.get()));
    }

    /**
     * Clear the cached terminal size regardless of when it was last checked.
     */
    public void clearCachedTerminalSize() {
        termTime.set(0L);
    }

    /**
     * @return True if this is an interactive TTY terminal.
     */
    public boolean isInteractive() {
        return getTerminalSizeInternal() != null;
    }

    private TerminalSize getTerminalSizeInternal() {
        return termSize.updateAndGet(ts -> {
            if (termTime.get() < clock.millis()) {
                try {
                    return getTerminalSize(runtime);
                } catch (IOException e) {
                    ex.set(e);
                    return null;
                } finally {
                    termTime.set(clock.millis() + 499);
                }
            }
            return ts;
        });
    }

    private static TerminalSize getTerminalSize(Runtime runtime) throws IOException {
        String[] cmd = new String[]{"/bin/sh", "-c", "stty size </dev/tty"};
        Process p = runtime.exec(cmd);
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            throw new IOException(ie.getMessage(), ie);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream(), UTF_8))) {
            String err = reader.readLine();
            if (err != null) {
                throw new IOException(err);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), UTF_8))) {
            String out = reader.readLine();
            if (out != null) {
                String[] parts = out.trim()
                                    .split("[ ]");
                if (parts.length == 2) {
                    int rows = Integer.parseInt(parts[0]);
                    int cols = Integer.parseInt(parts[1]);
                    return new TerminalSize(rows, cols);
                }
                throw new IOException("Unknown 'stty size' output: " + out);
            }
            throw new IOException("No 'stty size' output.");
        }
    }
}
