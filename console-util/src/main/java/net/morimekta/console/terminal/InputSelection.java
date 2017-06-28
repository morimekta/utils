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

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.chr.Color;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * Tabular selection with simple navigation. It displays a list of
 * items in a table that can be paginated and navigated between
 * using the arrow keys, numbers, pg-up, pg-down etc.
 * <p>
 * <pre>{@code
 * {message}: [c=command, o=overview]
 *  1 {line 1}
 *  2 {line 2}
 *  3 {line 3}
 * Your choice (1..N or c,o):
 * }</pre>
 * <p>
 * When doing actions, it is possible to add extra output to the user, this
 * will be printed <i>below</i> the `Your choice` line with a {@link LinePrinter}
 * connected back to the {@link InputSelection} instance.
 * <p>
 * By default there are <b>no actions</b> on the items, even <code>exit</code> is
 * not provided. Actions are specified using a list of "commands", that each have
 * an action, where each action has a <code>Reaction</code>. Note that any runtime
 * exception thrown while handling an action exits the selection.
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
            // Since '\r' and 'n' are basically interchangeable.
            this.key = key.asInteger() == '\r' ? new Unicode('\n') : key;
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
        this.lineBuffer = new LineBuffer(terminal);
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
            this.lineWidth = terminal.getTTY().getTerminalSize().cols;
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
        lineBuffer.add(makePromptLine());
        // - if (paged): hidden entries line
        if (paged) {
            lineBuffer.add(makeMoreEntriesLine());
        }
        // - shownEntries * (entry)
        for (int i = 0; i < shownEntries; ++i) {
            lineBuffer.add(makeEntryLine(i));
        }

        // - if (paged): hidden entries line
        if (paged) {
            lineBuffer.add(makeMoreEntriesLine());
        }
        // - selection line
        lineBuffer.add(makeSelectionLine());

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
                if (ch == Char.CR) {
                    ch = Char.LF;
                    c = new Unicode(Char.LF);
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
                            lineBuffer.update(off + currentOffset, makeEntryLine(index));
                            break;
                        }
                        case UPDATE_KEEP_ITEM: {
                            // E.g. updated sorting. Number of entries must
                            // remain the same.
                            updateSelectionIndex(current);

                            LinkedList<String> updates = new LinkedList<>();

                            if (paged) {
                                updates.add(makeMoreEntriesLine());
                            }
                            for (int i = 0; i < shownEntries; ++i) {
                                updates.add(makeEntryLine(i));
                            }
                            if (paged) {
                                updates.add(makeMoreEntriesLine());
                            }
                            lineBuffer.update(1, updates);
                            break;
                        }
                        case UPDATE_KEEP_POSITION: {
                            int off = paged ? 2 : 1;
                            LinkedList<String> updates = new LinkedList<>();
                            for (int i = 0; i < shownEntries; ++i) {
                                updates.add(makeEntryLine(i));
                            }
                            lineBuffer.update(off, updates);
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
            lineBuffer.update(off, makeSelectionLine());

            return true;
        } else {
            digits = "";
            digitsTimestamp = 0;

            int off = (paged ? 3 : 1) + shownEntries;
            lineBuffer.update(off, makeSelectionLine());
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
            lineBuffer.clearLast(extraLines);
        }
        extraLines = 0;
    }

    private void printExtraLine(String line) {
        lineBuffer.add(line);
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

                LinkedList<String> updates = new LinkedList<>();
                updates.add(makeMoreEntriesLine());
                for (int i = 0; i < shownEntries; ++i) {
                    updates.add(makeEntryLine(i));
                }
                updates.add(makeMoreEntriesLine());
                lineBuffer.update(1, updates);
            } else {
                int oldIndex = currentIndex;
                currentIndex = index;
                lineBuffer.update(off + oldIndex, makeEntryLine(oldIndex));
                lineBuffer.update(off + currentIndex, makeEntryLine(currentIndex));
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
        return format("%s [%s]",
                             prompt,
                             Strings.join(", ",
                                          commands.stream()
                                                  .filter(c -> !c.hidden)
                                                  .map(c -> format("%s=%s", c.key, c.name))
                                                  .collect(Collectors.toList())));
    }

    private String makeSelectionLine() {
        return format("Your choice (%d..%d or %s): %s",
                             1, entries.size(),
                             Strings.join(",",
                                          commands.stream()
                                                  .filter(c -> !c.hidden)
                                                  .map(c -> format("%s", c.key))
                                                  .collect(Collectors.toList())),
                             digits);
    }

    private String makeMoreEntriesLine() {
        int before = currentOffset;
        int after = max(0, entries.size() - currentOffset - pageSize);
        int pagesBefore = currentOffset / pageSize;
        int pagesAfter = (int) ceil(after / pageSize);

        String seeBefore = CharUtil.leftJust(
                before == 0 ? "" : format("<-- (pages: %d, items: %d)", pagesBefore, before), 38);
        String seeAfter = CharUtil.rightJust(
                after == 0 ? "" : format("(pages: %d, items: %d) -->", pagesAfter, after), 38);

        return format("  %s%s  ", seeBefore, seeAfter);
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

    private final Terminal                  terminal;
    private final LineBuffer                lineBuffer;
    private final String                    prompt;
    private final List<E>                   entries;
    private final List<Command<E>>          commands;
    private final HashMap<Char, Command<E>> commandMap;
    private final EntryPrinter<E>           printer;
    private final Clock                     clock;
    private final int                       pageSize;
    private final int                       lineWidth;

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
