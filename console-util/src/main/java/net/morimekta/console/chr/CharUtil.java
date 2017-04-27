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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

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
            if (c instanceof Unicode) {
                if (Strings.isConsolePrintable(c.asInteger()) ||
                        c.asInteger() == Char.CR || c.asInteger() == Char.LF ||
                        c.equals(Unicode.NBSP)) {
                    builder.append(c.toString());
                }
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
            if (remaining.get() == 0) {
                // Only add non-unicode after the end (control & color)
                if (!(c instanceof Unicode)) {
                    builder.append(c.toString());
                }
            } else if (pw <= remaining.get()) {
                builder.append(c.toString());
                remaining.addAndGet(-pw);
            } else {
                // To avoid a CJK char to be removed, but the ASCII
                // char after is kept.
                remaining.set(0);
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

    /**
     * Make a uncode boder symbol matching the lines given. The four valies are for top
     * (upward line), right, bottom and left.
     *
     * 0 - no line.
     * 1 - thin line
     * 2 - thick line
     * 3 - double line
     *
     * @param u The upward line.
     * @param r The rightward line.
     * @param d The downward line.
     * @param l The leftward line.
     * @return The unicode instance, or null if none matching.
     */
    public static Unicode makeBorder(int u, int r, int d, int l) {
        if (u < 0 || u > 3 ||
            r < 0 || r > 3 ||
            d < 0 || d > 3 ||
            l < 0 || l > 3) {
            throw new IllegalArgumentException(String.format("No border possible for %d,%d,%d,%d.", u, r, d, l));
        }

        int id = (u << 12) |
                 (r << 8) |
                 (d << 4) |
                 (l);
        switch (id) {
            // Stumps
            case 0x0001:
                return new Unicode(0x2574);
            case 0x1000:
                return new Unicode(0x2575);
            case 0x0100:
                return new Unicode(0x2576);
            case 0x0010:
                return new Unicode(0x2577);
            case 0x0002:
                return new Unicode(0x2578);
            case 0x2000:
                return new Unicode(0x2579);
            case 0x0200:
                return new Unicode(0x257A);
            case 0x0020:
                return new Unicode(0x257B);

            // Lines (simple)
            case 0x0101:
                return new Unicode(0x2500);
            case 0x0202:
                return new Unicode(0x2501);
            case 0x1010:
                return new Unicode(0x2502);
            case 0x2020:
                return new Unicode(0x2503);

            // Half-and-half lines
            case 0x0201:
                return new Unicode(0x257C);
            case 0x1020:
                return new Unicode(0x257D);
            case 0x0102:
                return new Unicode(0x257E);
            case 0x2010:
                return new Unicode(0x257F);

            // Corners (simple)
            case 0x0110:
                return new Unicode(0x250C);
            case 0x0210:
                return new Unicode(0x250D);
            case 0x0120:
                return new Unicode(0x250E);
            case 0x0220:
                return new Unicode(0x250F);
            case 0x0011:
                return new Unicode(0x2510);
            case 0x0012:
                return new Unicode(0x2511);
            case 0x0021:
                return new Unicode(0x2512);
            case 0x0022:
                return new Unicode(0x2513);
            case 0x1100:
                return new Unicode(0x2514);
            case 0x1200:
                return new Unicode(0x2515);
            case 0x2100:
                return new Unicode(0x2516);
            case 0x2200:
                return new Unicode(0x2517);
            case 0x1001:
                return new Unicode(0x2518);
            case 0x1002:
                return new Unicode(0x2519);
            case 0x2001:
                return new Unicode(0x251A);
            case 0x2002:
                return new Unicode(0x251B);

            // T-crossings (urd)
            case 0x1110:
                return new Unicode(0x251C);
            case 0x1210:
                return new Unicode(0x251D);
            case 0x2110:
                return new Unicode(0x251E);
            case 0x1120:
                return new Unicode(0x251F);
            case 0x2120:
                return new Unicode(0x2520);
            case 0x2210:
                return new Unicode(0x2521);
            case 0x1220:
                return new Unicode(0x2522);
            case 0x2220:
                return new Unicode(0x2523);

            // T-crossings (udl)
            case 0x1011:
                return new Unicode(0x2514);
            case 0x1012:
                return new Unicode(0x2515);
            case 0x1021:
                return new Unicode(0x2516);
            case 0x2011:
                return new Unicode(0x2517);
            case 0x2021:
                return new Unicode(0x2528);
            case 0x2012:
                return new Unicode(0x2529);
            case 0x1022:
                return new Unicode(0x252A);
            case 0x2022:
                return new Unicode(0x252B);

            // T-crossings (rdl)
            case 0x0111:
                return new Unicode(0x251C);
            case 0x0112:
                return new Unicode(0x251D);
            case 0x0211:
                return new Unicode(0x251E);
            case 0x0212:
                return new Unicode(0x251F);
            case 0x0121:
                return new Unicode(0x2520);
            case 0x0122:
                return new Unicode(0x2521);
            case 0x0221:
                return new Unicode(0x2522);
            case 0x0222:
                return new Unicode(0x2523);

            // T-crossings (url)
            case 0x1101:
                return new Unicode(0x2524);
            case 0x1102:
                return new Unicode(0x2525);
            case 0x1201:
                return new Unicode(0x2526);
            case 0x1202:
                return new Unicode(0x2527);
            case 0x2101:
                return new Unicode(0x2528);
            case 0x2102:
                return new Unicode(0x2529);
            case 0x2201:
                return new Unicode(0x252A);
            case 0x2202:
                return new Unicode(0x252B);

            // Full Crosses
            case 0x1111:
                return new Unicode(0x253C);
            case 0x1112:
                return new Unicode(0x253D);
            case 0x1211:
                return new Unicode(0x253E);
            case 0x1212:
                return new Unicode(0x253F);
            case 0x2111:
                return new Unicode(0x2540);
            case 0x1121:
                return new Unicode(0x2541);
            case 0x2121:
                return new Unicode(0x2542);
            case 0x2112:
                return new Unicode(0x2543);
            case 0x2211:
                return new Unicode(0x2544);
            case 0x1122:
                return new Unicode(0x2545);
            case 0x1221:
                return new Unicode(0x2546);
            case 0x2212:
                return new Unicode(0x2547);
            case 0x1222:
                return new Unicode(0x2548);
            case 0x2122:
                return new Unicode(0x2549);
            case 0x2221:
                return new Unicode(0x254A);
            case 0x2222:
                return new Unicode(0x254B);

            // Double lines
            case 0x0303:
                return new Unicode(0x2550);
            case 0x3030:
                return new Unicode(0x2551);

            // Double and single+double corners.
            case 0x0310:
                return new Unicode(0x2552);
            case 0x0130:
                return new Unicode(0x2553);
            case 0x0330:
                return new Unicode(0x2554);
            case 0x0013:
                return new Unicode(0x2555);
            case 0x0031:
                return new Unicode(0x2556);
            case 0x0033:
                return new Unicode(0x2557);
            case 0x1300:
                return new Unicode(0x2558);
            case 0x3100:
                return new Unicode(0x2559);
            case 0x3300:
                return new Unicode(0x255A);
            case 0x1003:
                return new Unicode(0x255B);
            case 0x3001:
                return new Unicode(0x255C);
            case 0x3003:
                return new Unicode(0x255D);

            // Double and single+double T-crosses.
            case 0x1310:
                return new Unicode(0x255E);
            case 0x3130:
                return new Unicode(0x255F);
            case 0x3330:
                return new Unicode(0x2560);
            case 0x1013:
                return new Unicode(0x2561);
            case 0x3031:
                return new Unicode(0x2562);
            case 0x3033:
                return new Unicode(0x2563);
            case 0x0313:
                return new Unicode(0x2564);
            case 0x0131:
                return new Unicode(0x2565);
            case 0x0333:
                return new Unicode(0x2566);
            case 0x1303:
                return new Unicode(0x2567);
            case 0x3101:
                return new Unicode(0x2568);
            case 0x3303:
                return new Unicode(0x2569);

            // Double and single+double full-crosses.
            case 0x1313:
                return new Unicode(0x256A);
            case 0x3131:
                return new Unicode(0x256B);
            case 0x3333:
                return new Unicode(0x256C);
        }

        throw new IllegalArgumentException(String.format("No border for (u:%d,r:%d,d:%d,l:%d).", u, r, d, l));
    }

    /**
     * Gets the unicode char representing a circled number.
     * @param num Number to get unicode char for.
     * @return The unicode char representation.
     */
    public static Unicode makeNumeric(int num) {
        if (num > 0) {
            if (num <= 20) {
                return new Unicode(0x2460 - 1 + num);
            }
            if (num <= 35) {
                return new Unicode(0x3250 - 20 + num);
            }
            if (num <= 50) {
                return new Unicode(0x32b0 - 35 + num);
            }
        }
        throw new IllegalArgumentException("No circled numeric for " + num);
    }

    /**
     * Make a byte array representing the input bytes for generating
     * the given input. See {@link CharReader}, {@link CharStream}.
     * The input accepts: {@link Character} (char), {@link Integer}
     * and any {@link Char} instance as input. And everything else is
     * {@link Object#toString()}'ed and handled as a UTF-8 string.
     *
     * @param in The input objects.
     * @return The input bytes.
     */
    public static byte[] inputBytes(Object... in) {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        try {
            for (Object c : in) {
                if (c instanceof Character) {
                    if ((Character) c == Char.ESC) {
                        tmp.write(Char.ESC);
                    }
                    tmp.write((Character) c);
                } else if (c instanceof Integer) {
                    if ((Integer) c == (int) Char.ESC) {
                        tmp.write(Char.ESC);
                    }
                    // raw unicode codepoint.
                    tmp.write(new Unicode((Integer) c).toString().getBytes(UTF_8));
                } else if (c instanceof Char) {
                    if (((Char) c).asInteger() == Char.ESC) {
                        tmp.write(Char.ESC);
                        tmp.write(Char.ESC);
                    } else {
                        tmp.write(c.toString().getBytes(UTF_8));
                    }
                } else {
                    tmp.write(c.toString().getBytes(UTF_8));
                }
            }
        } catch (IOException e) {
            // Should be impossible.
            throw new UncheckedIOException(e);
        }
        return tmp.toByteArray();
    }

    /**
     * Make a list of input {@link Char}s that e.g. can be used
     * in testing input or output.
     *
     * @param in the input objects.
     * @return The char list representing the input.
     */
    public static List<Char> inputChars(Object... in) {
        LinkedList<Char> list = new LinkedList<>();

        for (Object c : in) {
            /*  */ if (c instanceof Character) {
                list.add(new Unicode((Character) c));
            } else if (c instanceof Integer) {
                list.add(new Unicode((Integer) c));
            } else if (c instanceof Char) {
                list.add((Char) c);
            } else {
                CharStream.stream(c.toString())
                          .forEachOrdered(list::add);
            }
        }

        return list;
    }

    public static Char alt(char c) {
        if (('a' <= c && c <= 'z') ||
            ('A' <= c && c <= 'Z' && c != 'O') ||
            ('0' <= c && c <= '9')) {
            return new Control(format("\033%c", c));
        }
        throw new IllegalArgumentException("Not suitable for <alt> modifier: '" + Strings.escape(c) + "'");
    }

    // Defeat instantiation.
    private CharUtil() {}
}
