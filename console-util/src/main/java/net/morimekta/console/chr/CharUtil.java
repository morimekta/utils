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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common character and console utilities. It can contain both standard unicode characters
 * and unix console control sequences. Here also resides lots of helper
 * methods related to  calculating visible string length, that takes into
 * account control sequences, non-width characters and double width characters.
 */
public class CharUtil {
    public static final int TAB_WIDTH = 4;

    /**
     * How many single-characters worth of console real-estate will be taken
     * up by this string if printed. Control characters will be ignored, and
     * double-width characters (CJK) will count as 2 width each.
     *
     * Strings containing carriage movement, CR, LF, unexpanded tabs etc are
     * not allowed, and will cause an IllegalArgumentException.
     *
     * @param string The string to measure.
     * @return The printed width.
     */
    public static int printableWidth(String string) {
        AtomicInteger width = new AtomicInteger(0);
        CharStream.stream(string).forEach(c -> width.addAndGet(c.printableWidth()));
        return width.get();
    }

    /**
     * Expand tabs in string.
     *
     * @param string The string to expand.
     * @return The expanded string.
     */
    public static String expandTabs(String string) {
        return expandTabs(string, TAB_WIDTH);
    }

    /**
     * Expand tabs in string.
     *
     * @param string The string to expand.
     * @param tabWidth The tab width.
     * @return The expanded string.
     */
    public static String expandTabs(String string, int tabWidth) {
        return expandTabs(string, tabWidth, 0);
    }

    /**
     * Expand tabs in string.
     *
     * @param string The string to expand.
     * @param tabWidth The tab width.
     * @param offset The initial offset.
     * @return The expanded string.
     */
    public static String expandTabs(String string, int tabWidth, int offset) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger off = new AtomicInteger(offset);
        CharStream.stream(string).forEachOrdered(c -> {
            if (c.asInteger() == '\t') {
                int l = tabWidth - (off.get() % tabWidth);
                for (int i = 0; i < l; ++i) {
                    builder.append(' ');
                }
                off.addAndGet(l);
            } else {
                builder.append(c);
                off.addAndGet(c.printableWidth());
            }
        });
        return builder.toString();
    }

    /**
     * Strip string of all non-printable characters.
     *
     * @param string The source string.
     * @return The result without non-printable chars.
     */
    public static String stripNonPrintable(String string) {
        StringBuilder builder = new StringBuilder();
        CharStream.stream(string).forEachOrdered(c -> {
            if (c.printableWidth() > 0) {
                builder.append(c.toString());
            }
        });
        return builder.toString();
    }

    /**
     * Remove all printable characters after 'width' characters have been
     * filled. All control chars will be left in place.
     *
     * @param string The base string.
     * @param width The printed width.
     * @return The clipped string.
     */
    public static String clipWidth(String string, int width) {
        AtomicInteger remaining = new AtomicInteger(width);
        StringBuilder builder = new StringBuilder();
        CharStream.stream(string).forEachOrdered(c -> {
            int pw = c.printableWidth();
            if (pw <= remaining.get()) {
                builder.append(c.toString());
                remaining.addAndGet(-pw);
            }
        });
        return builder.toString();
    }

    public static String leftJust(String string, int width) {
        int pw = printableWidth(string);
        if (pw < width) {
            return string + Strings.times(" ", width - pw);
        }
        return string;
    }

    public static String rightJust(String string, int width) {
        int pw = printableWidth(string);
        if (pw < width) {
            return Strings.times(" ", width - pw) + string;
        }
        return string;
    }

    private CharUtil() {}
}
