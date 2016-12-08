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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Switch terminal mode and make it return on close. Basic usage is:
 *
 * <code>
 * try (STTYModeSwitcher tty = new STTYModeSwitcher(STTYMode.RAW)) {
 *     // do stuff in raw mode.
 * }
 * </code>
 */
public class STTYModeSwitcher implements Closeable {
    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @throws IOException If unable to switch.
     * @deprecated Use {@link STTY#setSTTYMode(STTYMode)}.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public STTYModeSwitcher(STTYMode mode) throws IOException {
        this(mode, Runtime.getRuntime());
    }

    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @param runtime The runtime to execute stty commands on.
     * @throws IOException If unable to switch.

     */
    public STTYModeSwitcher(STTYMode mode, Runtime runtime) throws IOException {
        this.runtime = runtime;
        this.mode = mode;
        this.before = setSttyMode(mode);
    }

    /**
     * Close the terminal mode switcher and turn back the the mode before it
     * was opened.
     *
     * @throws IOException If unable to switch back.
     */
    public void close() throws IOException {
        setSttyMode(before);
    }

    /**
     * Get the mode set by the seitcher.
     * @return The tty mode.
     */
    public STTYMode getMode() {
        return mode;
    }

    /**
     * Get the mode that was replaced by the switcher.
     *
     * @return the tty mode.
     */
    public STTYMode getBefore() {
        return before;
    }

    /**
     * Get the current TTY mode.
     *
     * @return The tty mode.
     */
    public STTYMode getCurrentMode() {
        synchronized (STTYModeSwitcher.class) {
            return current_mode;
        }
    }

    /**
     * @return True if the mode switcher changed the tty mode.
     */
    public boolean didChangeMode() {
        return getMode() != getBefore();
    }

    /**
     * Get the current TTY mode.
     *
     * @return The tty mode.
     */
    public static STTYMode currentMode() {
        synchronized (STTYModeSwitcher.class) {
            return current_mode;
        }
    }

    // Default input mode is COOKED.
    private static STTYMode current_mode = STTYMode.COOKED;

    private final Runtime  runtime;
    private final STTYMode before;
    private final STTYMode mode;

    /**
     * Set terminal mode.
     *
     * @param mode The mode to set.
     * @return The mode before the call.
     */
    private STTYMode setSttyMode(STTYMode mode) throws IOException {
        synchronized (STTYModeSwitcher.class) {
            STTYMode old = current_mode;
            if (mode != current_mode) {
                String[] cmd;
                if (mode == STTYMode.COOKED) {
                    cmd = new String[]{"/bin/sh", "-c", "stty -raw echo </dev/tty"};
                } else {
                    cmd = new String[]{"/bin/sh", "-c", "stty raw -echo </dev/tty"};
                }

                Process p = runtime.exec(cmd);

                try {
                    p.waitFor();
                } catch (InterruptedException ie) {
                    throw new IOException(ie.getMessage(), ie);
                }

                try (InputStreamReader in = new InputStreamReader(p.getErrorStream(), UTF_8);
                     BufferedReader reader = new BufferedReader(in)) {
                    String err = reader.readLine();
                    if (err != null) {
                        throw new IOException(err);
                    }
                }

                current_mode = mode;
            }
            return old;
        }
    }
}
