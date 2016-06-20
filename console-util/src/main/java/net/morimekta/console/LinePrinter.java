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

import net.morimekta.console.chr.Color;

/**
 * LinePrinter interface.
 */
public interface LinePrinter {
    /**
     * Print a new line to the terminal.
     * @param message The message to write.
     */
    void println(String message);

    /**
     * Print an info string message.
     *
     * @param message The info message.
     */
    default void info(String message) {
        println(String.format("%s[info]%s %s",
                              Color.GREEN,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print a warning string message.
     *
     * @param message The warning message.
     */
    default void warn(String message) {
        println(String.format("%s[warn]%s %s",
                              Color.YELLOW,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print an error string message.
     *
     * @param message The error message.
     */
    default void error(String message) {
        println(String.format("%s[error]%s %s",
                              Color.RED,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print a fatal string message.
     *
     * @param message The fatal message.
     */
    default void fatal(String message) {
        println(String.format("%s[FATAL]%s %s",
                              new Color(Color.RED, Color.BOLD),
                              Color.CLEAR,
                              message));
    }
}
