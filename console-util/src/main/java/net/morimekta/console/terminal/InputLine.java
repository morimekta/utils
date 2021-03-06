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
import net.morimekta.console.chr.Control;
import net.morimekta.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.regex.Pattern;

import static net.morimekta.console.chr.CharUtil.alt;

/**
 * Class that handled reading a line from terminal input with
 * character and line validators, and optional tab completion.
 * <p>
 * When writing input into an app it is problematic if the app
 * crashes or exits every time you makes invalid input. This can be
 * solved with the {@link CharValidator} and {@link LineValidator}
 * interfaces. The <code>CharValidator</code> validates that any single
 * char input is valid, and will block if not. The <code>LineValidator</code>
 * is triggered when the user finishes the input and checks if the
 * line is valid as a whole, and blocks completion if not.
 * <p>
 * In addition a {@link TabCompletion} interface may be provided that
 * can complete input based on the current content <i>before</i> the
 * cursor. If the <code>complete()</code> method returns a string, it
 * will <b>replace</b> what was before.
 */
public class InputLine {
    /**
     * Line validator interface.
     */
    @FunctionalInterface
    public interface LineValidator {
        /**
         * Validate the full line.
         *
         * @param line The line to validate.
         * @param errorPrinter Printer to print out error messages.
         * @return True if valid, false otherwise.
         */
        boolean validate(String line, LinePrinter errorPrinter);
    }

    /**
     * Character validator interface.
     */
    @FunctionalInterface
    public interface CharValidator {
        /**
         * Validate the given char.
         *
         * @param ch The char to validate.
         * @param errorPrinter Printer to print out error messages.
         * @return True if valid, false otherwise.
         */
        boolean validate(Char ch, LinePrinter errorPrinter);
    }

    /**
     * Tab completion interface.
     */
    @FunctionalInterface
    public interface TabCompletion {
        /**
         * Try to complete the given string.
         *
         * The function should <b>only</b> print to the line printer if there
         * is <b>no</b> selected completion.
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
        this(terminal, message, charValidator, lineValidator, tabCompletion, Pattern.compile("[-/.\\s\\\\]"));
    }

    /**
     * Constructor for complete line-input.
     *
     * @param terminal Terminal to use.
     * @param message Message to print.
     * @param charValidator The character validcator or null.
     * @param lineValidator The line validator or null.
     * @param tabCompletion The tab expander or null.
     * @param delimiterPattern Pattern matching a character that delimit 'words' that
     *                      are skipped or deleted with [ctrl-left], [ctrl-right],
     *                      [alt-w] and [alt-d].
     */
    public InputLine(Terminal terminal,
                     String message,
                     CharValidator charValidator,
                     LineValidator lineValidator,
                     TabCompletion tabCompletion,
                     Pattern delimiterPattern) {
        if (charValidator == null) {
            charValidator = (c, o) -> {
                if (c.asInteger() < 0x20 ||
                    !Strings.isConsolePrintable(c.asInteger())) {
                    o.println("Invalid character: '" + c.asString() + "'");
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
        this.delimiterPattern = delimiterPattern;
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
     *
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

                if (ch == Char.CR || ch == Char.LF) {
                    String line = before + after;
                    if (lineValidator.validate(line, this::printAbove)) {
                        return line;
                    }
                    continue;
                }

                handleInterrupt(ch, c);

                if (handleTab(ch) ||
                    handleBackSpace(ch) ||
                    handleControl(c)) {
                    continue;
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

    /**
     * Handle tab and tab completion.
     *
     * @param ch The character code point.
     * @return True if handled.
     */
    private boolean handleTab(int ch) {
        if (ch == Char.TAB && tabCompletion != null) {
            String completed = tabCompletion.complete(before, this::printAbove);
            if (completed != null) {
                before = completed;
                printInputLine();
            }
            return true;
        }

        return false;
    }

    /**
     * Handle backspace. These are not control sequences, so must be handled separately
     * from those.
     *
     * @param ch The character code point.
     * @return True if handled.
     */
    private boolean handleBackSpace(int ch) {
        if (ch == Char.DEL || ch == Char.BS) {
            // backspace...
            if (before.length() > 0) {
                before = before.substring(0, before.length() - 1);
                printInputLine();
            }
            return true;
        }

        return false;
    }

    /**
     * Handle user interrupts.
     * @param ch The character code point.
     * @param c The char instance.
     * @throws IOException
     */
    private void handleInterrupt(int ch, Char c) throws IOException {
        if (ch == Char.ESC || ch == Char.ABR || ch == Char.EOF) {
            throw new IOException("User interrupted: " + c.asString());
        }
    }

    /**
     * Handle control sequence chars.
     *
     * @param c The control char.
     * @return If the char was handled.
     */
    private boolean handleControl(Char c) {
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
                int cut = cutWordBefore();
                if (cut > 0) {
                    after = before.substring(cut) + after;
                    before = before.substring(0, cut);
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
                int cut = cutWordAfter();
                if (cut > 0) {
                    before = before + after.substring(0, cut);
                    after = after.substring(cut);
                } else {
                    before = before + after;
                    after = "";
                }
            } else if (c.equals(ALT_W)) {
                // delete word before the cursor.
                int cut = cutWordBefore();
                if (cut > 0) {
                    before = before.substring(0, cut);
                } else {
                    before = "";
                }
            } else if (c.equals(ALT_D)) {
                // delete word after the cursor.
                int cut = cutWordAfter();
                if (cut > 0) {
                    after = after.substring(cut);
                } else {
                    after = "";
                }
            } else if (c.equals(ALT_K)) {
                // delete everything after the cursor.
                after = "";
            } else if (c.equals(ALT_U)) {
                // delete everything before the cursor.
                before = "";
            } else {
                printAbove("Invalid control: " + c.asString());
                return true;
            }
            printInputLine();
            return true;
        }
        return false;
    }

    /**
     * Find the position of the first character of the last word before
     * the cursor that is preceded by a delimiter.
     *
     * @return The word position or -1.
     */
    private int cutWordBefore() {
        if (before.length() > 0) {
            int cut = before.length() - 1;
            while (cut >= 0 && isDelimiter(before.charAt(cut))) {
                --cut;
            }
            // We know the 'cut' position character is not a
            // delimiter. Also cut all characters that is not
            // preceded by a delimiter.
            while (cut > 0) {
                if (isDelimiter(before.charAt(cut - 1))) {
                    return cut;
                }
                --cut;
            }
        }
        return -1;
    }

    /**
     * Find the position of the first delimiter character after the first word
     * after the cursor.
     *
     * @return The delimiter position or -1.
     */
    private int cutWordAfter() {
        if (after.length() > 0) {
            int cut = 0;
            while (cut < after.length() && isDelimiter(after.charAt(cut))) {
                ++cut;
            }
            final int last = after.length() - 1;
            while (cut <= last) {
                if (isDelimiter(after.charAt(cut))) {
                    return cut;
                }
                ++cut;
            }
        }
        return -1;
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

    private boolean isDelimiter(char c) {
        return delimiterPattern.matcher(String.valueOf(c)).matches();
    }

    private static final Char ALT_D = alt('d');  // delete word after
    private static final Char ALT_W = alt('w');  // delete word before
    private static final Char ALT_K = alt('k');  // delete line after
    private static final Char ALT_U = alt('u');  // delete line before

    private final Terminal terminal;
    private final String message;
    private final CharValidator charValidator;
    private final LineValidator lineValidator;
    private final TabCompletion tabCompletion;
    private final Pattern delimiterPattern;

    private String before;
    private String after;
    private String printedError;
}
