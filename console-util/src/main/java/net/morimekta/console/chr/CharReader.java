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
package net.morimekta.console.chr;

import net.morimekta.util.Strings;
import net.morimekta.util.io.Utf8StreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * A keystroke char reader. returns a Char object at a time related to the
 * single keystroke the user typed.
 */
public class CharReader {
    private final Reader in;

    public CharReader(InputStream in) {
        this(new Utf8StreamReader(in));
    }

    public CharReader(Reader in) {
        this.in = in;
    }

    /**
     * Read the next char.
     *
     * @return The next char, or null of input stream is closed.
     * @throws IOException If unable to read a char.
     */
    public Char read() throws IOException {
        int cp = in.read();
        if (cp < 0) {
            return null;
        }

        if (cp == '\033') {
            // We have received an 'esc' and nothing else...
            if (!in.ready()) {
                return new Unicode(cp);
            }

            // 'esc' was last char in stream.
            int c2 = in.read();
            if (c2 < 0) {
                return new Unicode(cp);
            }

            StringBuilder charBuilder = new StringBuilder();
            charBuilder.append((char) cp);
            charBuilder.append((char) c2);

            if (c2 == '\033') {
                // Treat double 'esc' as a single 'esc'. Otherwise pressing 'esc'
                // will consistently crash the application.
                return new Unicode(cp);
            } else if (c2 == '[') {
                char c3 = expect();
                charBuilder.append(c3);
                if ('A' <= c3 && c3 <= 'Z') {
                    // \033 [ A-Z
                    return new Control(charBuilder.toString());
                }
                while (('0' <= c3 && c3 <= '9') || c3 == ';') {
                    c3 = expect();
                    charBuilder.append(c3);
                }
                if (c3 == '~' ||
                    ('a' <= c3 && c3 <= 'z') ||
                    ('A' <= c3 && c3 <= 'Z')) {
                    // \033 [ (number) ~ (F1, F2 ... Fx)
                    // \033 [ (number...;) [A-D] (numbered cursor movement)
                    // \033 [ (number...;) [su] (cursor save / restore, ...)
                    // \033 [ (number...;) m (color)
                    if (c3 == 'm') {
                        try {
                            return new Color(charBuilder.toString());
                        } catch (IllegalArgumentException e) {
                            throw new IOException(e.getMessage(), e);
                        }
                    }
                    return new Control(charBuilder.toString());
                }
            } else if (c2 == 'O') {
                char c3 = expect();
                charBuilder.append(c3);
                if ('A' <= c3 && c3 <= 'Z') {
                    // \033 O [A-Z]
                    return new Control(charBuilder.toString());
                }
            } else if (('a' <= c2 && c2 <= 'z') ||
                       ('0' <= c2 && c2 <= '9') ||
                       ('A' <= c2 && c2 <= 'Z')) {
                // \033 [a-z]: <alt-{c}> aka <M-{c}>.
                // \033 [0-9]: <alt-{c}> aka <M-{c}>.
                // \033 [A-N]: <alt-shift-{c}> aka <M-S-{c}>.
                // \033 [P-Z]: <alt-shift-{c}> aka <M-S-{c}>.
                return new Control(charBuilder.toString());
            }

            throw new IOException("Invalid escape sequence: \"" + Strings.escape(charBuilder.toString()) + "\"");
        } else {
            // Make sure to consume both surrogates on 32-bit code-points.
            if (Character.isHighSurrogate((char) cp)) {
                cp = Character.toCodePoint((char) cp, expect());
            }
            return new Unicode(cp);
        }
    }

    private char expect() throws IOException {
        int cp = in.read();
        if (cp < 0) {
            throw new IOException("Unexpected end of stream.");
        }
        return (char) cp;
    }
}
