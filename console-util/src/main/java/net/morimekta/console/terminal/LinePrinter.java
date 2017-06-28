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

import net.morimekta.console.chr.Color;

import static java.lang.String.format;

/**
 * LinePrinter interface.
 */
@FunctionalInterface
public interface LinePrinter {
    /**
     * Print a new line to the terminal.
     *
     * @param message The message to write.
     */
    void println(String message);

    /**
     * Format and print a string message.
     *
     * @param format The message format.
     * @param params Printf like params to format.
     */
    default void formatln(String format, Object... params) {
        println(format(format, params));
    }

    /**
     * Print an info string message.
     *
     * @param format The info message.
     * @param params Printf like params to format.
     */
    default void info(String format, Object... params) {
        String message = params.length == 0 ? format : format(format, params);
        println(format("%s[info]%s %s",
                       Color.GREEN,
                       Color.CLEAR,
                       message));
    }

    /**
     * Print a warning string message.
     *
     * @param format The warning message.
     * @param params Printf like params to format.
     */
    default void warn(String format, Object... params) {
        String message = params.length == 0 ? format : format(format, params);
        println(format("%s[warn]%s %s",
                       Color.YELLOW,
                       Color.CLEAR,
                       message));
    }

    /**
     * Print an error string message.
     *
     * @param format The error message.
     * @param params Printf like params to format.
     */
    default void error(String format, Object... params) {
        String message = params.length == 0 ? format : format(format, params);
        println(format("%s[error]%s %s",
                       Color.RED,
                       Color.CLEAR,
                       message));
    }

    /**
     * Print a fatal string message.
     *
     * @param format The fatal message.
     * @param params Printf like params to format.
     */
    default void fatal(String format, Object... params) {
        String message = params.length == 0 ? format : format(format, params);
        println(format("%s[FATAL]%s %s",
                       new Color(Color.RED, Color.BOLD),
                       Color.CLEAR,
                       message));
    }
}
