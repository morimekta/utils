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
package net.morimekta.console.util;

import net.morimekta.console.args.Argument;
import net.morimekta.console.args.ArgumentException;
import net.morimekta.console.args.Option;
import net.morimekta.console.args.Property;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Value parser interface. It converts from a string value (usually the
 * CLI argument value) and converts it into a specific value type. Essentially
 * this interface is meant to bridge two {@link Consumer}s, the target consumer
 * (usually the value setter or adder), and the string consumer that the
 * {@link Option} or {@link Argument} needs.
 * <p>
 * E.g., the following code will create an option '--timestamp' that parses the
 * argument value as a long (i64) and calls <code>bean.setTimestamp(long)</code>
 * with that value:
 * <code>
 *    new Option("--timestamp", null, "The timestamp", i64(bean::setTimestamp));
 * </code>
 */
@FunctionalInterface
public interface Parser<T> {
    @FunctionalInterface
    interface TypedPutter<T> {
        /**
         * Put a typed value
         * @param key The property key.
         * @param value The property value.
         */
        void put(String key, T value);
    }

    /**
     * Parse the value into a typed instance.
     *
     * @param value The string value.
     * @return The typed instance.
     * @throws ArgumentException If the parsing failed.
     */
    T parse(String value);

    /**
     * Make a string consumer to typed value consumer out of the converter.
     *
     * @param consumer The consumer to wrap.
     * @return The string consumer.
     */
    default Consumer<String> andThen(Consumer<T> consumer) {
        return s -> consumer.accept(parse(s));
    }

    /**
     * Make a property putter that calls a typed putter with the parsed value.
     *
     * @param putter The typed putter.
     * @return The property putter.
     */
    default Property.Putter andThen(TypedPutter<T> putter) {
        return (k, v) -> putter.put(k, parse(v));
    }

    /**
     * Make a consumer that puts a specific value with the typed putter.
     *
     * @param putter the typed putter.
     * @param key The property key.
     * @return The string consumer.
     */
    default Consumer<String> andThen(TypedPutter<T> putter, String key) {
        return s -> putter.put(key, parse(s));
    }

    /**
     * Convenience method to put a specific value into a putter.
     *
     * @param putter The putter.
     * @param key The key to put.
     * @return The string consumer.
     */
    static Consumer<String> putInto(Property.Putter putter, String key) {
        return s -> putter.put(key, s);
    }

    /**
     * Make a 32-bit integer parser.
     *
     * @return The parser.
     */
    static Parser<Integer> i32() {
        return new IntegerParser();
    }

    /**
     * Make a 64-bit integer parser.
     *
     * @return The parser.
     */
    static Parser<Long> i64() {
        return new LongParser();
    }

    /**
     * Make a double parser.
     *
     * @return The parser.
     */
    static Parser<Double> dbl() {
        return new DoubleParser();
    }

    /**
     * Make an enum value parser.
     *
     * @param klass The enum class.
     * @return The parser.
     * @param <E> The enum type.
     */
    static <E extends Enum<E>> Parser<E> oneOf(Class<E> klass) {
        return new EnumParser<>(klass);
    }

    /**
     * Make a file parser that refers to an existing file.
     *
     * @return The parser.
     */
    static Parser<File> file() {
        return new FileParser();
    }

    /**
     * Make a file parser that refers to an existing directory.
     *
     * @return The consumer wrapper.
     */
    static Parser<File> dir() {
        return new DirParser();
    }

    /**
     * Make a file parser that refers either to a non-existing entry or an
     * existing file, but not a directory or special device.
     *
     * @return The consumer wrapper.
     */
    static Parser<File> outputFile() {
        return new OutputFileParser();
    }

    /**
     * Make a parser that refers either to a non-existing entry or an
     * existing directory, but not a file or special device.
     *
     * @return The parser.
     */
    static Parser<File> outputDir() {
        return new OutputDirParser();
    }

    /**
     * Make a parser that parses a path.
     *
     * @return The parser.
     */
    static Parser<Path> path() {
        return new PathParser();
    }

    /**
     * A converter to path values.
     */
    class PathParser implements Parser<Path> {
        @Override
        public Path parse(String s) {
            return Paths.get(s);
        }
    }

    /**
     * A converter to long values.
     */
    class LongParser implements Parser<Long> {
        @Override
        public Long parse(String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException nfe) {
                throw new ArgumentException(nfe, "Invalid long value " + s);
            }
        }
    }

    /**
     * A converter to integer values.
     */
    class IntegerParser implements Parser<Integer> {
        @Override
        public Integer parse(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                throw new ArgumentException(nfe, "Invalid integer value " + s);
            }
        }
    }

    /**
     * A converter to file instances, with validator &amp; error message.
     */
    class FileParser implements Parser<File> {
        @Override
        public File parse(String s) {
            File result = new File(s);
            if (!result.exists()) {
                throw new ArgumentException("No such file " + s);
            }
            if (!result.isFile()) {
                throw new ArgumentException(s + " is not a file");
            }
            return result;
        }
    }

    /**
     * A converter to file instances, with validator &amp; error message.
     */
    class DirParser implements Parser<File> {
        @Override
        public File parse(String s) {
            File result = new File(s);
            if (!result.exists()) {
                throw new ArgumentException("No such directory " + s);
            }
            if (!result.isDirectory()) {
                throw new ArgumentException(s + " is not a directory");
            }
            return result;
        }
    }

    /**
     * A converter to file instances, with validator &amp; error message.
     */
    class OutputFileParser implements Parser<File> {
        @Override
        public File parse(String s) {
            File result = new File(s);
            if (result.exists() && !result.isFile()) {
                throw new ArgumentException(s + " exists and is not a file");
            }
            return result;
        }
    }

    /**
     * A converter to file instances, with validator &amp; error message.
     */
    class OutputDirParser implements Parser<File> {
        @Override
        public File parse(String s) {
            File result = new File(s);
            if (result.exists() && !result.isDirectory()) {
                throw new ArgumentException(s + " exists and is not a directory");
            }
            return result;
        }
    }

    /**
     * A converter to enum constant values.
     */
    class EnumParser<E extends Enum<E>> implements Parser<E> {
        private final Class<E> klass;

        public EnumParser(Class<E> klass) {
            this.klass = klass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E parse(String name) {
            try {
                return (E) klass.getDeclaredMethod("valueOf", String.class)
                                .invoke(null, name);
            } catch (InvocationTargetException e) {
                throw new ArgumentException(e.getCause(),
                                            "Invalid " + klass.getSimpleName() + " value " + name);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // Should generally be impossible, since enums are declares via native syntax.
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    /**
     * A converter to double values.
     */
    class DoubleParser implements Parser<Double> {
        @Override
        public Double parse(String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException nfe) {
                throw new ArgumentException(nfe, "Invalid double value " + s);
            }
        }
    }
}
