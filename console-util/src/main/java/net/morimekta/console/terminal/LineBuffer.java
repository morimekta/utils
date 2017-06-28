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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.morimekta.console.chr.CharUtil.printableWidth;
import static net.morimekta.console.chr.Control.CURSOR_ERASE;
import static net.morimekta.console.chr.Control.UP;
import static net.morimekta.console.chr.Control.cursorDown;
import static net.morimekta.console.chr.Control.cursorRight;
import static net.morimekta.console.chr.Control.cursorUp;

/**
 * Class that holds a set of lines, that are printed to the terminal, and
 * methods to dynamically update those buffer. It will keep the cursor at
 * the bottom line (end of printed line) for easy continuation.
 * <p>
 * The class acts as a wrapper around a {@link Terminal} instance, and
 * makes sure that a list of lines can be updated and printed properly to
 * the terminal in the most efficient order.
 * <p>
 * Example uses are for showing a list of {@link Progress}'es, or
 * handling the internals of a {@link InputSelection}.
 */
public class LineBuffer {
    private final Terminal          terminal;
    private final ArrayList<String> buffer;

    /**
     * Create a LineBuffer instance.
     *
     * @param terminal The terminal to wrap.
     */
    public LineBuffer(Terminal terminal) {
        this.terminal = terminal;
        this.buffer = new ArrayList<>();
    }

    /**
     * @return Number of lines in the buffer.
     */
    public int count() {
        return buffer.size();
    }

    /**
     * Add new lines to the end of the buffer, and print them out.
     *
     * @param lines The lines to add.
     */
    public void add(String ... lines) {
        add(ImmutableList.copyOf(lines));
    }

    /**
     * Add new lines to the end of the buffer, and print them out.
     *
     * @param lines The lines to add.
     */
    public void add(Collection<String> lines) {
        for (String line : lines) {
            buffer.add(line);
            terminal.println(line);
        }
    }

    /**
     * Update a number of lines starting at a specific offset.
     *
     * @param offset The line offset (0-indexed to count).
     * @param lines The new line content.
     */
    public void update(int offset, String... lines) {
        update(offset, ImmutableList.copyOf(lines));
    }

    /**
     * Update a number of lines starting at a specific offset.
     *
     * @param offset The line offset (0-indexed to count).
     * @param lines The new line content.
     */
    public void update(int offset, List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Empty line set");
        }
        if (offset >= count() || offset < 0) {
            throw new IndexOutOfBoundsException("Index: " + offset + ", Size: " + count());
        }

        int up = count() - offset - 1;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (i == 0) {
                terminal.print("\r");
                if (up > 0) {
                    terminal.print(cursorUp(up));
                }
            } else {
                terminal.println();
                --up;
            }

            String old = offset + i < buffer.size()
                         ? buffer.get(offset + i)
                         : null;
            buffer.set(offset + i, line);
            if (line.equals(old) && !line.isEmpty()) {
                // No change.
                continue;
            }
            terminal.print(CURSOR_ERASE);
            terminal.print(line);
        }

        // Move the cursor back to the end of the last line.
        if (up > 0) {
            terminal.format("\r%s%s",
                            cursorDown(up),
                            cursorRight(printableWidth(lastLine())));
        }

    }

    /**
     * Clear the entire buffer, and the terminal area it represents.
     */
    public void clear() {
        if (buffer.size() > 0) {
            terminal.format("\r%s", CURSOR_ERASE);
            for (int i = 1; i < buffer.size(); ++i) {
                terminal.format("%s%s", UP, CURSOR_ERASE);
            }
            buffer.clear();
        }
    }

    /**
     * Clear the last N lines, and move the cursor to the end of the last
     * remaining line.
     *
     * @param N Number of lines to clear.
     */
    public void clearLast(int N) {
        if (N < 1) {
            throw new IllegalArgumentException("Unable to clear " + N + " lines");
        }
        if (N > count()) {
            throw new IllegalArgumentException("Count: " + N + ", Size: " + count());
        }
        if (N == count()) {
            clear();
            return;
        }

        terminal.format("\r%s", CURSOR_ERASE);
        buffer.remove(buffer.size() - 1);
        for (int i = 1; i < N; ++i) {
            terminal.format("%s%s", UP, CURSOR_ERASE);
            buffer.remove(buffer.size() - 1);
        }

        terminal.format("%s\r%s",
                        UP,
                        cursorRight(printableWidth(lastLine())));
    }

    /**
     * @return The content of the last line in the buffer.
     */
    private String lastLine() {
        return buffer.get(buffer.size() - 1);
    }
}
