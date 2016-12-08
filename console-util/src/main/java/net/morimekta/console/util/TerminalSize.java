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

import java.io.UncheckedIOException;

/**
 * Column and row count for the current terminal.
 */
public class TerminalSize {
    public final int rows;
    public final int cols;

    @VisibleForTesting
    public TerminalSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public String toString() {
        return String.format("tty(rows:%d, cols:%d)", rows, cols);
    }

    /**
     * Get the terminal size.
     *
     * @return the terminal size.
     * @throws UncheckedIOException If getting the terminal size failed.
     * @deprecated Use {@link STTY#getTerminalSize()}.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static TerminalSize get() {
        return new STTY().getTerminalSize();
    }

    /**
     * @return True if this is an interactive TTY terminal.
     * @deprecated Use {@link STTY#isInteractive()}.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static boolean isInteractive() {
        return new STTY().isInteractive();
    }
}
