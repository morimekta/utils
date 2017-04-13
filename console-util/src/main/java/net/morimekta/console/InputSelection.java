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
package net.morimekta.console;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.chr.Color;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.console.util.TerminalBuffer;
import net.morimekta.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Tabular selection with simple navigation.
 *
 * <code>
 * {message}: [c=command, o=overview]
 *  1 {line 1}
 *  2 {line 2}
 *  3 {line 3}
 * Your choice (1..N or c,o):
 * </code>
 *
 * If extra messages are needed, they will be printed *below* the 'Your choice'
 * line with a LinePrinter connected back to this.
 */
public class InputSelection<E> {
    /**
     * Interface for the entry printer.
     *
     * @param <E> The entry type.
     */
    @FunctionalInterface
    public interface EntryPrinter<E> {
        /**
         * Print the entry line.
         *
         * @param entry The entry to be printed.
         * @param bgColor The background color.
         * @return The entry line.
         */
        String print(E entry, Color bgColor);

        /**
         * Print the entry line with default background.
         *
         * @param entry The entry to be printed.
         * @return The entry line.
         */
        default String print(E entry) {
            return print(entry, Color.BG_DEFAULT);
        }
    }

    /**
     * Command reaction enum.
     */
    public enum Reaction {
        /**
         * Select the entry.
         */
        SELECT,

        /**
         * Exit selection with no value (null).
         */
        EXIT,

        /**
         * Stay in the selection.
         */
        STAY,

        /**
         * Stay in the selection and update entries (clear draw cache and
         * redraw all visible entries). Keeps the same selected item regardless
         * of position.
         */
        UPDATE_KEEP_ITEM,

        /**
         * Stay in the selection and update entries (clear draw cache and
         * redraw all visible entries). Keeps the same selected position
         * regardless of the underlying item.
         */
        UPDATE_KEEP_POSITION,
    }

    /**
     * The command action interface.
     *
     * @param <E> The entry value type.
     */
    @FunctionalInterface
    public interface Action<E> {
        /**
         * Call the command with the given entry.
         * @param entry The entry to work on.
         * @param printer Line printer to show extra messages.
         * @return The reaction.
         */
        Reaction call(E entry, LinePrinter printer);
    }

    /**
     * Command. The command works on an entry.
     *
     * @param <E> Value type.
     */
    public static class Command<E> {
        public Command(char key, String name, Action<E> action) {
            this(key, name, action, false);
        }

        public Command(char key, String name, Action<E> action, boolean hidden) {
            this(new Unicode(key), name, action, hidden);
        }

        public Command(Char key, String name, Action<E> action) {
            this(key, name, action, false);
        }

        public Command(Char key, String name, Action<E> action, boolean hidden) {
            this.key = key;
            this.name = name;
            this.action = action;
            this.hidden = hidden;
        }

        private final Char      key;
        private final String    name;
        private final Action<E> action;
        private final boolean   hidden;
    }

    /**
     * Create a selection instance.
     *
     * @param terminal The terminal to print to.
     * @param prompt The prompt to introduce the selection with.
     * @param entries The list of entries.
     * @param commands The list of commands.
     * @param printer The entry printer.
     */
    public InputSelection(Terminal terminal,
                          String prompt,
                          List<E> entries,
                          List<Command<E>> commands,
                          EntryPrinter<E> printer) {
        this(terminal, prompt, entries, commands, printer, defaultPageSize(terminal), 5, 0);
    }

    /**
     * Create a selection instance.
     *
     * @param terminal The terminal to print to.
     * @param prompt The prompt to introduce the selection with.
     * @param entries The list of entries.
     * @param commands The list of commands.
     * @param printer The entry printer.
     * @param pageSize The max number of entries per page.
     * @param pageMargin The number of entries above page size needed to trigger paging.
     * @param lineWidth The number of columns to print on.
     */
    public InputSelection(Terminal terminal,
                          String prompt,
                          List<E> entries,
                          List<Command<E>> commands,
                          EntryPrinter<E> printer,
                          int pageSize,
                          int pageMargin,
                          int lineWidth) {
        this(terminal, prompt, entries, commands, printer, Clock.systemUTC(), pageSize, pageMargin, lineWidth);
    }

    /**
     * Create a selection instance.
     *
     * @param terminal The terminal to print to.
     * @param prompt The prompt to introduce the selection with.
     * @param entries The list of entries.
     * @param commands The list of commands.
     * @param printer The entry printer.
     * @param clock The system clock.
     * @param pageSize The max number of entries per page.
     * @param pageMargin The number of entries above page size needed to trigger paging.
     * @param lineWidth The number of columns to print on.
     */
    public InputSelection(Terminal terminal,
                          String prompt,
                          List<E> entries,
                          List<Command<E>> commands,
                          EntryPrinter<E> printer,
                          Clock clock,
                          int pageSize,
                          int pageMargin,
                          int lineWidth) {
        this.terminal = terminal;
        this.terminalBuffer = new TerminalBuffer(terminal);
        this.prompt = prompt;
        this.entries = entries;
        this.commands = commands;
        this.clock = clock;

        this.commandMap = new HashMap<>();
        for (Command<E> cmd : commands) {
            this.commandMap.put(cmd.key, cmd);
        }

        this.printer = printer;
        this.pageSize = pageSize;
        if (lineWidth == 0) {
            if (terminal.getTTY().isInteractive()) {
                this.lineWidth = terminal.getTTY().getTerminalSize().cols;
            } else {
                // Fallback for non-interactive terminals.
                this.lineWidth = 100;
            }
        } else {
            this.lineWidth = lineWidth;
        }
        this.digits = "";

        if (entries.size() > (pageSize + pageMargin)) {
            this.paged = true;
            this.shownEntries = pageSize;
        } else {
            this.paged = false;
            this.shownEntries = entries.size();
        }
    }

    public E select() {
        return select(null);
    }

    public E select(E initial) {
        updateSelectionIndex(initial);

        // Initialize lines:
        // - prompt line
        terminalBuffer.add(makePromptLine());
        // - if (paged): hidden entries line
        if (paged) {
            terminalBuffer.add(makeMoreEntriesLine());
        }
        // - shownEntries * (entry)
        for (int i = 0; i < shownEntries; ++i) {
            terminalBuffer.add(makeEntryLine(i));
        }

        // - if (paged): hidden entries line
        if (paged) {
            terminalBuffer.add(makeMoreEntriesLine());
        }
        // - selection line
        terminalBuffer.add(makeSelectionLine());

        try {
            for (;;) {
                Char c = terminal.read();
                if (c == null) {
                    throw new IOException("End of input");
                }

                int ch = c.asInteger();
                if (ch == Char.EOF || ch == Char.ESC || ch == Char.ABR) {
                    throw new IOException("User interrupted: " + c.asString());
                }

                if (handleDigit(ch) ||
                    handleControl(c)) {
                    continue;
                }

                Command<E> cmd = commandMap.get(c);
                if (cmd != null) {
                    clearExtraLines();

                    int index = currentIndex + currentOffset;

                    E current = entries.get(index);
                    Reaction reaction = cmd.action.call(current, this::printExtraLine);
                    switch (reaction) {
                        case SELECT:
                            return current;
                        case EXIT:
                            return null;
                        case STAY: {
                            // Updates the selected line only.
                            int off = paged ? 2 : 1;
                            terminalBuffer.update(off + index, makeEntryLine(index));
                            break;
                        }
                        case UPDATE_KEEP_ITEM: {
                            // E.g. updated sorting. Number of entries must
                            // remain the same.
                            updateSelectionIndex(current);

                            int off = 1;
                            if (paged) {
                                terminalBuffer.update(1, makeMoreEntriesLine());
                                terminalBuffer.update(shownEntries + 2, makeMoreEntriesLine());
                                off = 2;
                            }
                            for (int i = 0; i < shownEntries; ++i) {
                                terminalBuffer.update(off + i, makeEntryLine(i));
                            }
                            break;
                        }
                        case UPDATE_KEEP_POSITION: {
                            int off = paged ? 2 : 1;
                            for (int i = 0; i < shownEntries; ++i) {
                                terminalBuffer.update(off + i, makeEntryLine(i));
                            }
                            break;
                        }
                    }
                } else {
                    printExtraLine(" --- Not found: " + c.asString());
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean handleDigit(int ch) {
        if ('0' <= ch && ch <= '9') {
            long ts = clock.millis();
            // Forget typed digits after 2 seconds.
            if (ts > digitsTimestamp + 2000) {
                digits = "";
            }
            digitsTimestamp = ts;

            int pos = Integer.parseInt(digits + (char) ch);
            // if this is not the first digit, AND the position has run past
            // the last item, go back and make it the first digit.
            if (digits.length() > 0 && pos >= entries.size()) {
                digits = String.valueOf((char) ch);
                pos = ch - '0';
            } else {
                digits = String.valueOf(pos);
            }

            if (pos <= entries.size()) {
                updateSelection(pos - 1);
            } else {
                updateSelection(entries.size() - 1);
            }
            int off = (paged ? 3 : 1) + shownEntries;
            terminalBuffer.update(off, makeSelectionLine());

            return true;
        } else {
            digits = "";
            digitsTimestamp = 0;

            int off = (paged ? 3 : 1) + shownEntries;
            terminalBuffer.update(off, makeSelectionLine());
        }

        return false;
    }

    private boolean handleControl(Char c) {
        if (c instanceof Control) {
            int last = entries.size() - 1;
            if (c.equals(Control.HOME)) {
                updateSelection(0);
            } else if (c.equals(Control.END)) {
                updateSelection(last);
            } else if (c.equals(Control.UP)) {
                updateSelection(max(0, currentIndex + currentOffset - 1));
            } else if (c.equals(Control.DOWN)) {
                updateSelection(min(last, currentIndex + currentOffset + 1));
            } else if (c.equals(Control.LEFT)) {
                updateSelection(max(0, currentIndex + currentOffset - shownEntries));
            } else if (c.equals(Control.RIGHT)) {
                updateSelection(min(last, currentIndex + currentOffset + shownEntries));
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    private void clearExtraLines() {
        if (extraLines > 0) {
            terminalBuffer.clearLast(extraLines);
        }
        extraLines = 0;
    }

    private void printExtraLine(String line) {
        terminalBuffer.add(line);
        ++extraLines;
    }

    private void updateSelection(int newAbsolute) {
        int currentAbsolute = currentIndex + currentOffset;
        if (newAbsolute != currentAbsolute) {
            // something changed.
            int offset = paged ? pageSize * (newAbsolute / pageSize) : 0;
            int index = newAbsolute - offset;
            int off = paged ? 2 : 1;

            if (offset != currentOffset) {
                // change page.
                currentIndex = index;
                currentOffset = offset;

                terminalBuffer.update(1, makeMoreEntriesLine());
                for (int i = 0; i < shownEntries; ++i) {
                    terminalBuffer.update(off + i, makeEntryLine(i));
                }
                terminalBuffer.update(shownEntries + 2, makeMoreEntriesLine());
            } else {
                int oldIndex = currentIndex;
                currentIndex = index;
                terminalBuffer.update(off + oldIndex, makeEntryLine(oldIndex));
                terminalBuffer.update(off + currentIndex, makeEntryLine(currentIndex));
            }
        }
    }

    private void updateSelectionIndex(E selection) {
        if (selection != null) {
            int currentAbsolute = entries.indexOf(selection);
            if (paged) {
                currentOffset = pageSize * (currentAbsolute / pageSize);
                currentIndex = currentAbsolute - currentOffset;
            } else {
                currentIndex = currentAbsolute;
            }
        }
    }

    private String makePromptLine() {
        return String.format("%s [%s]",
                             prompt,
                             Strings.join(", ",
                                          commands.stream()
                                                  .filter(c -> !c.hidden)
                                                  .map(c -> String.format("%s=%s", c.key, c.name))
                                                  .collect(Collectors.toList())));
    }

    private String makeSelectionLine() {
        return String.format("Your choice (%d..%d or %s): %s",
                             1, entries.size(),
                             Strings.join(",",
                                          commands.stream()
                                                  .filter(c -> !c.hidden)
                                                  .map(c -> String.format("%s", c.key))
                                                  .collect(Collectors.toList())),
                             digits);
    }

    private String makeMoreEntriesLine() {
        int before = currentOffset;
        int after = max(0, entries.size() - currentOffset - pageSize);

        String seeBefore = CharUtil.leftJust(
                before == 0 ? "" : String.format("<-- (%d more)", before), 38);
        String seeAfter = CharUtil.rightJust(
                after == 0 ? "" : String.format("(%d more) -->", after), 38);

        return String.format("  %s%s  ", seeBefore, seeAfter);
    }

    private String makeEntryLine(int index) {
        int absoluteIndex = index + currentOffset;
        if (absoluteIndex >= entries.size()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean selected = index == currentIndex;
        Color bg = selected ? Color.BG_BLUE : null;
        if (selected) {
            builder.append(bg);
        }

        int idxSize = 2;
        if (entries.size() > 9) {
            ++idxSize;
        }
        if (entries.size() > 99) {
            ++idxSize;
        }
        if (entries.size() > 999) {
            ++idxSize;
        }

        builder.append(CharUtil.rightJust(String.valueOf(absoluteIndex + 1), idxSize))
               .append(' ');

        E entry = entries.get(absoluteIndex);
        builder.append(selected ? printer.print(entry, bg) : printer.print(entry));

        String line = builder.toString();

        int pw = CharUtil.printableWidth(line);
        if (pw > lineWidth) {
            line = CharUtil.clipWidth(line, lineWidth);
        } else if (pw < lineWidth && bg != null) {
            builder.append(Color.CLEAR)
                   .append(bg)
                   .append(Strings.times(" ", lineWidth - pw));
            line = builder.toString();
        }
        return line + Color.CLEAR.toString();
    }

    private static int defaultPageSize(Terminal terminal) {
        if (terminal.getTTY().isInteractive()) {
            // 2 rows above, 2 rows below, 5 margin and 3 extra.
            // 22 rows -> 10 entries + 5 margin
            // 44 rows -> 22 entries + 5 margin
            // 80 rows -> 35 entries + 5 margin (max)
            return Math.max(35, terminal.getTTY().getTerminalSize().rows - 12);
        }
        return 20;
    }

    private final Terminal terminal;
    private final TerminalBuffer terminalBuffer;
    private final String prompt;
    private final List<E> entries;
    private final List<Command<E>> commands;
    private final HashMap<Char, Command<E>> commandMap;
    private final EntryPrinter<E> printer;
    private final Clock clock;
    private final int pageSize;
    private final int lineWidth;

    private final boolean paged;
    private final int shownEntries;

    private String digits;
    private long   digitsTimestamp;
    private int    extraLines;

    // The item index relative to the current page.
    private int currentIndex;
    // The index offset to the real start.
    private int currentOffset;
}
