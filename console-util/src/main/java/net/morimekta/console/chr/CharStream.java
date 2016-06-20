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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Java 8 streams handling of character sequences.
 */
public class CharStream {
    public static Iterator<Char> iterator(CharSequence str) {
        return Spliterators.iterator(new CharSpliterator(str));
    }

    public static Stream<Char> stream(CharSequence str) {
        return StreamSupport.stream(new CharSpliterator(str), false);
    }

    private CharStream() {}

    private static class CharSpliterator implements Spliterator<Char> {
        private final CharSequence cstr;

        private volatile int pos;

        private CharSpliterator(CharSequence cstr) {
            this.cstr = cstr;
            this.pos = 0;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Char> consumer) {
            if (pos >= cstr.length()) {
                return false;
            }

            char c = cstr.charAt(pos);
            if (c == '\033') {  // esc, \033
                int r = (cstr.length() - pos);
                if (r == 1) {
                    // just the escape char, and nothing else...
                    consumer.accept(new Unicode(c));
                    ++pos;
                    return true;
                }
                char c2 = cstr.charAt(pos + 1);
                if (r > 2 && c2 == '[') {
                    char c3 = cstr.charAt(pos + 2);
                    if ('A' <= c3 && c3 <= 'Z') {
                        // \033 [ A-Z
                        consumer.accept(new Control(cstr.subSequence(pos, pos + 3)));
                        pos += 3;
                        return true;
                    }
                    int n = 2;
                    while (('0' <= c3 && c3 <= '9') || c3 == ';') {
                        ++n;
                        if ((pos + n) == cstr.length()) {
                            // It just ended in the middle, use the single escape char and advance one.
                            consumer.accept(new Unicode(c));
                            ++pos;
                            return true;
                        }
                        c3 = cstr.charAt(pos + n);
                    }
                    if (c3 == '~' ||
                        ('a' <= c3 && c3 <= 'z') ||
                        ('A' <= c3 && c3 <= 'Z')) {
                        // \033 [ (number) ~ (F1, F2 ... Fx)
                        // \033 [ (number...;) [A-D] (numbered cursor movement)
                        // \033 [ (number...;) [su] (cursor save / restore, ...)
                        // \033 [ (number...;) m (color)
                        if (c3 == 'm') {
                            consumer.accept(new Color(cstr.subSequence(pos, pos + n + 1)));
                        } else {
                            consumer.accept(new Control(cstr.subSequence(pos, pos + n + 1)));
                        }
                        pos += (n + 1);
                        return true;
                    }
                } else if (('a' <= c2 && c2 <= 'z') ||
                           ('A' <= c2 && c2 <= 'Z')) {
                    if (r > 2 && c2 == 'O') {
                        char c3 = cstr.charAt(pos + 2);
                        if ('A' <= c3 && c3 <= 'Z') {
                            // \033 O [A-Z]
                            consumer.accept(new Control(cstr.subSequence(pos, pos + 3)));
                            pos += 3;
                            return true;
                        }
                    }
                    // \033 [a-zA-NP-Z]
                    consumer.accept(new Control(cstr.subSequence(pos, pos + 2)));
                    pos += 3;
                    return true;
                }

                // just use the escape char, and nothing else...
                consumer.accept(new Unicode(c));
                ++pos;
                return true;
            } else {
                int cp = c;

                // Make sure to consume both surrogates on 32-bit code-points.
                if (Character.isHighSurrogate(c)) {
                    ++pos;
                    cp = Character.toCodePoint(c, cstr.charAt(pos));
                }
                consumer.accept(new Unicode(cp));
                ++pos;
                return true;
            }
        }

        @Override
        public Spliterator<Char> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
        }
    }
}
