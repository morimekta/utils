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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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
        return Spliterators.iterator(new CharSpliterator(str, false));
    }

    public static Iterator<Char> lenientIterator(CharSequence str) {
        return Spliterators.iterator(new CharSpliterator(str, true));
    }

    public static Stream<Char> stream(CharSequence str) {
        return StreamSupport.stream(new CharSpliterator(str, false), false);
    }

    public static Stream<Char> lenientStream(CharSequence str) {
        return StreamSupport.stream(new CharSpliterator(str, true), false);
    }

    private CharStream() {}

    private static class CharSpliterator implements Spliterator<Char> {
        private final CharReader reader;
        private final boolean lenient;
        private final ByteArrayInputStream in;

        private CharSpliterator(CharSequence cstr, boolean lenient) {
            this.in = new ByteArrayInputStream(cstr.toString().getBytes(StandardCharsets.UTF_8));
            this.reader = new CharReader(in);
            this.lenient = lenient;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Char> consumer) {
            try {
                if (lenient) {
                    in.mark(10);
                }
                Char c = reader.read();
                if (c != null) {
                    consumer.accept(c);
                    return true;
                }
                return false;
            } catch (IOException e) {
                if (lenient) {
                    in.reset();
                    if (in.skip(1) > 0) {
                        consumer.accept(new Unicode(Char.ESC));
                        return true;
                    }
                }
                throw new UncheckedIOException(e);
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
