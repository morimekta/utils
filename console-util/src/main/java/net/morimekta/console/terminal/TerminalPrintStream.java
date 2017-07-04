/*
 * Copyright (c) 2017, Stein Eldar Johnsen
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

import net.morimekta.console.chr.Char;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * Terminal interface. It sets proper TTY mode and reads complex characters
 * from the input, and writes lines dependent on terminal mode.
 */
class TerminalPrintStream extends PrintStream {
    private final Terminal terminal;

    /**
     * Construct a default RAW terminal.
     *
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    TerminalPrintStream(Terminal terminal) {
        super(terminal.getOutputStream(), true);
        this.terminal = terminal;
    }

    @Override
    public void write(int i) {
        if (i == Char.LF) {
            super.flush();
            terminal.println();
        } else {
            super.write(i);
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes, int off, int len) {
        for (int i = off; i < off + len; ++i) {
            int ch = bytes[i] < 0 ? 0x100 + bytes[i] : bytes[i];
            this.write(ch);
        }
        super.flush();
    }
}
