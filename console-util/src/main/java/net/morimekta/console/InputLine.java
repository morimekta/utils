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
import net.morimekta.console.chr.Control;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Class that handled reading a line from terminal input with
 * character and line validators, and optional tab completion.
 */
public class InputLine {
    public interface LineValidator {
        boolean validate(String line, LinePrinter errorPrinter);
    }

    public interface CharValidator {
        boolean validate(Char ch, LinePrinter errorPrinter);
    }

    /**
     * Tab completion interface.
     */
    public interface TabCompletion {
        /**
         * Try to complete the given string.
         *
         * @param before The string to be completed. What is before the carriage.
         * @param errorPrinter Print errors or alternatives to this line printer.
         * @return the completed string, or null if no completion.
         */
        String complete(String before, LinePrinter errorPrinter);
    }

    /**
     * Constructor for simple line-input.
     *
     * @param terminal Terminal to use.
     * @param message Message to print.
     */
    public InputLine(Terminal terminal,
                     String message) {
        this(terminal, message, null, null, null);
    }

    /**
     * Constructor for complete line-input.
     *
     * @param terminal Terminal to use.
     * @param message Message to print.
     * @param charValidator The character validcator or null.
     * @param lineValidator The line validator or null.
     * @param tabCompletion The tab expander or null.
     */
    public InputLine(Terminal terminal,
                     String message,
                     CharValidator charValidator,
                     LineValidator lineValidator,
                     TabCompletion tabCompletion) {
        if (charValidator == null) {
            charValidator = (c, o) -> {
                int ch = c.asInteger();
                // Just check for printable ASCII character
                if (ch < 0x20 || 0x7f <= ch) {
                    o.println("Invalid character: " + c.asString());
                    return false;
                }
                return true;
            };
        }
        if (lineValidator == null) {
            lineValidator = (l, o) -> {
                // Accept all non-empty lines.
                if (l.length() == 0) {
                    o.println("Output needs at least 1 character.");
                    return false;
                }
                return true;
            };
        }

        this.terminal = terminal;
        this.message = message;
        this.charValidator = charValidator;
        this.lineValidator = lineValidator;
        this.tabCompletion = tabCompletion;
    }

    /**
     * Read line from terminal.
     *
     * @return The resulting line.
     */
    public String readLine() {
        return readLine(null);
    }

    /**
     * Read line from terminal.
     * @param initial The initial (default) value.
     * @return The resulting line.
     */
    public String readLine(String initial) {
        this.before = initial == null ? "" : initial;
        this.after = "";
        this.printedError = null;

        if (initial != null && initial.length() > 0 &&
                !lineValidator.validate(initial, e -> {})) {
            throw new IllegalArgumentException("Invalid initial value: " + initial);
        }

        terminal.formatln("%s: %s", message, before);

        try {
            for (; ; ) {
                Char c = terminal.read();
                if (c == null) {
                    throw new IOException("End of input.");
                }

                int ch = c.asInteger();
                if (ch == Char.TAB && tabCompletion != null) {
                    String completed = tabCompletion.complete(before, this::printAbove);
                    if (completed != null) {
                        before = completed;
                        printInputLine();
                    }
                    continue;
                }

                if (ch == Char.CR) {
                    String line = before + after;
                    if (lineValidator.validate(line, this::printAbove)) {
                        return line;
                    }
                    continue;
                }

                if (ch == Char.DEL) {
                    // backspace...
                    before = before.substring(0, before.length() - 1);
                    printInputLine();
                    continue;
                }

                if (c instanceof Control) {
                    if (c.equals(Control.DELETE)) {
                        if (after.length() > 0) {
                            after = after.substring(1);
                        }
                    } else if (c.equals(Control.LEFT)) {
                        if (before.length() > 0) {
                            after = "" + before.charAt(before.length() - 1) + after;
                            before = before.substring(0, before.length() - 1);
                        }
                    } else if (c.equals(Control.HOME)) {
                        after = before + after;
                        before = "";
                    } else if (c.equals(Control.CTRL_LEFT)) {
                        if (before.length() > 0) {
                            // Skip all ending spaces.
                            int lastSpace = before.length() - 1;
                            while (lastSpace > 0 && before.charAt(lastSpace) == ' ') {
                                --lastSpace;
                            }
                            lastSpace = before.lastIndexOf(" ", lastSpace);
                            if (lastSpace > 0) {
                                after = before.substring(lastSpace) + after;
                                before = before.substring(0, lastSpace);
                            } else {
                                after = before + after;
                                before = "";
                            }
                        } else {
                            after = before + after;
                            before = "";
                        }
                    } else if (c.equals(Control.RIGHT)) {
                        if (after.length() > 0) {
                            before = before + after.charAt(0);
                            after = after.substring(1);
                        }
                    } else if (c.equals(Control.END)) {
                        before = before + after;
                        after = "";
                    } else if (c.equals(Control.CTRL_RIGHT)) {
                        if (after.length() > 0) {
                            int firstSpace = 0;
                            while (firstSpace < after.length() && after.charAt(firstSpace) == ' ') {
                                ++firstSpace;
                            }
                            firstSpace = after.indexOf(" ", firstSpace);
                            if (firstSpace > 0) {
                                before = before + after.substring(0, firstSpace);
                                after = after.substring(firstSpace);
                            } else {
                                before = before + after;
                                after = "";
                            }
                        }
                    } else if (c.equals(ALT_D)) {
                        // delete everything after the corsor.
                        after = "";
                    } else {
                        printAbove("Invalid control: " + c.asString());
                        continue;
                    }
                    printInputLine();
                    continue;
                }

                if (ch == Char.ESC || ch == Char.ABR || ch == Char.EOF) {
                    throw new IOException("User interrupted: " + c.asString());
                }

                if (charValidator.validate(c, this::printAbove)) {
                    before = before + c.toString();
                    printInputLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printAbove(String error) {
        if (printedError != null) {
            terminal.format("\r%s%s",
                            Control.UP,
                            Control.CURSOR_ERASE);
        } else {
            terminal.format("\r%s", Control.CURSOR_ERASE);
        }
        printedError = error;
        terminal.print(error);
        terminal.println();
        printInputLine();
    }

    private void printInputLine() {
        terminal.format("\r%s%s: %s",
                        Control.CURSOR_ERASE,
                        message,
                        before);
        if (after.length() > 0) {
            terminal.format("%s%s", after, Control.cursorLeft(after.length()));
        }
    }

    private static final Char ALT_D = new Control("\033d");
    private static final Char ALT_W = new Control("\033w");

    private final Terminal terminal;
    private final String message;
    private final CharValidator charValidator;
    private final LineValidator lineValidator;
    private final TabCompletion tabCompletion;

    private String before;
    private String after;
    private String printedError;
}
