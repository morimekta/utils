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
import net.morimekta.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Class that handled reading a password (hidden string) from terminal
 * input with an optional character replacement char. If the replacement
 * string is empty, no input or output will show from entering the
 * password.
 */
public class InputPassword {
    /**
     * Constructor for simple line-input.
     *
     * @param terminal Terminal to use.
     * @param message Message to print.
     */
    public InputPassword(Terminal terminal,
                         String message) {
        this(terminal, message, "*");
    }

    /**
     * Constructor for complete line-input.
     *
     * @param terminal Terminal to use.
     * @param message Message to print.
     * @param charReplacement The character replacement string, e.g. "*".
     */
    public InputPassword(Terminal terminal,
                         String message,
                         String charReplacement) {
        this.terminal = terminal;
        this.message = message;
        this.charReplacement = charReplacement;
    }

    /**
     * Read password from terminal.
     *
     * @return The resulting line.
     */
    public String readPassword() {
        this.before = "";
        this.after = "";

        terminal.formatln("%s: ", message);

        try {
            for (; ; ) {
                Char c = terminal.read();
                if (c == null) {
                    throw new IOException("End of input.");
                }

                int ch = c.asInteger();

                if (ch == Char.CR) {
                    return before + after;
                }

                if (ch == Char.DEL || ch == Char.BS) {
                    // backspace...
                    if (before.length() > 0) {
                        before = before.substring(0, before.length() - 1);
                        printInputLine();
                    }
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
                    } else if (c.equals(Control.RIGHT)) {
                        if (after.length() > 0) {
                            before = before + after.charAt(0);
                            after = after.substring(1);
                        }
                    } else if (c.equals(Control.END)) {
                        before = before + after;
                        after = "";
                    } else {
                        // Silently ignore unknown control chars.
                        continue;
                    }
                    printInputLine();
                    continue;
                }

                if (ch == Char.ESC || ch == Char.ABR || ch == Char.EOF) {
                    throw new IOException("User interrupted: " + c.asString());
                }

                if (ch < 0x20) {
                    // Silently ignore unknown ASCII control characters.
                    continue;
                }

                before = before + c.toString();
                printInputLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printInputLine() {
        String bf = Strings.times(charReplacement, before.length());
        String af = Strings.times(charReplacement, after.length());
        terminal.format("\r%s%s: %s",
                        Control.CURSOR_ERASE,
                        message, bf);
        if (af.length() > 0) {
            terminal.format("%s%s", af, Control.cursorLeft(af.length()));
        }
    }

    private final Terminal terminal;
    private final String message;
    private final String charReplacement;

    private String before;
    private String after;
}
