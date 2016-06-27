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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Value converter interface. It converts from a string value (usually the
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
    default Consumer<String> into(Consumer<T> consumer) {
        return s -> consumer.accept(parse(s));
    }

    /**
     * Make a 32-bit integer consumer wrapper.
     *
     * @param consumer The int consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> i32(Consumer<Integer> consumer) {
        return new IntegerParser().into(consumer);
    }

    /**
     * Make a 64-bit integer consumer wrapper.
     *
     * @param consumer The long consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> i64(Consumer<Long> consumer) {
        return new LongParser().into(consumer);
    }

    /**
     * Make a double consumer wrapper.
     *
     * @param consumer The double consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> dbl(Consumer<Double> consumer) {
        return new DoubleParser().into(consumer);
    }

    /**
     * Make an enum value consumer wrapper.
     *
     * @param klass The enum class.
     * @param consumer The enum value consumer.
     * @return The consumer wrapper.
     * @param <E> The enum type.
     */
    static <E extends Enum<E>> Consumer<String> oneOf(Class<E> klass, Consumer<E> consumer) {
        return new EnumParser<>(klass).into(consumer);
    }

    /**
     * Make a file consumer that refers to an existing file.
     *
     * @param consumer The file consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> file(Consumer<File> consumer) {
        return new FileParser(f -> f.exists() && f.isFile(),
                              "%s is not a file.").into(consumer);
    }

    /**
     * Make a file consumer that refers to an existing directory.
     *
     * @param consumer The file consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> dir(Consumer<File> consumer) {
        return new FileParser(f -> f.exists() && f.isDirectory(),
                              "%s is not a directory.").into(consumer);
    }

    /**
     * Make a file consumer that refers either to a non-existing entry or an
     * existing file, but not a directory or special device.
     *
     * @param consumer The file consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> outputFile(Consumer<File> consumer) {
        return new FileParser(f -> !f.exists() || f.isFile(),
                              "%s must be file or not created yet.").into(consumer);
    }


    /**
     * Make a consumer that refers either to a non-existing entry or an
     * existing directory, but not a file or special device.
     *
     * @param consumer The file consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> outputDir(Consumer<File> consumer) {
        return new FileParser(f -> !f.exists() || f.isDirectory(),
                              "%s must be directory or not created yet.").into(consumer);
    }

    /**
     * Make a consumer that parses a path.
     *
     * @param consumer The path consumer.
     * @return The consumer wrapper.
     */
    static Consumer<String> path(Consumer<Path> consumer) {
        return new PathParser().into(consumer);
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
        private final Predicate<File> validator;
        private final String          message;

        /**
         * Create a file converter with a validator and a custom error message.
         *
         * @param validator The file validator.
         * @param message The error message if validation fails.
         */
        public FileParser(Predicate<File> validator, String message) {
            this.validator = validator;
            this.message = message;
        }

        @Override
        public File parse(String s) {
            File result = new File(s);
            if (!validator.test(result)) {
                throw new ArgumentException(String.format(message, result.getPath()));
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
