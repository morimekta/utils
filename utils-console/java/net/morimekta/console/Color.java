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

import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Unix terminal color helper. The printed syntax of the color is:
 * <pre>
 * \033[${code}m
 * \033[${code};${code}m
 * </pre>
 * etc.
 */
public class Color implements Char {
    public static final Color CLEAR = new Color(0);

    public static final Color DEFAULT = new Color(39);
    public static final Color BLACK   = new Color(30);
    public static final Color RED     = new Color(31);
    public static final Color GREEN   = new Color(32);
    public static final Color YELLOW  = new Color(33);
    public static final Color BLUE    = new Color(34);
    public static final Color MAGENTA = new Color(35);
    public static final Color CYAN    = new Color(36);
    public static final Color WHITE   = new Color(37);

    public static final Color BG_DEFAULT = new Color(49);
    public static final Color BG_BLACK   = new Color(40);
    public static final Color BG_RED     = new Color(41);
    public static final Color BG_GREEN   = new Color(42);
    public static final Color BG_YELLOW  = new Color(43);
    public static final Color BG_BLUE    = new Color(44);
    public static final Color BG_MAGENTA = new Color(45);
    public static final Color BG_CYAN    = new Color(46);
    public static final Color BG_WHITE   = new Color(47);

    public static final Color BOLD      = new Color(1);
    public static final Color DIM       = new Color(2);
    public static final Color UNDERLINE = new Color(4);
    public static final Color STROKE    = new Color(9);
    public static final Color INVERT    = new Color(7);
    public static final Color HIDDEN    = new Color(8);

    public static final Color UNSET_BOLD      = new Color(21);
    public static final Color UNSET_DIM       = new Color(22);
    public static final Color UNSET_UNDERLINE = new Color(24);
    public static final Color UNSET_STROKE    = new Color(29);
    public static final Color UNSET_INVERT    = new Color(27);
    public static final Color UNSET_HIDDEN    = new Color(28);

    private final int[]  mods;
    private final String str;

    /**
     * Create a color with the given modifiers.
     *
     * @param values List of modifiers.
     */
    public Color(int... values) {
        TreeSet<Integer> mods = new TreeSet<>();

        int fg = 0;
        int bg = 0;
        for (int i : values) {
            if (i == 0) {
                fg = 0;
                bg = 0;
                mods.clear();
                mods.add(0);
                break;
            } else if (i < 10) {  // 1..9: modifiers
                // Text modifier.
                mods.add(i);
                mods.remove(i + 20);
            } else if (i < 30) {  // 21..29, unset mods.
                // Negative (unset) text modifier.
                mods.add(i);
                mods.remove(i - 20);
            } else if (i < 40) {  // 30..39, text color.
                // Foreground (text) color.
                fg = i;
            } else if (i < 50) {  // 40..49, background color.
                // Background color.
                bg = i;
            }
        }
        if (fg != 0) {
            mods.add(fg);
        }
        if (bg != 0) {
            mods.add(bg);
        }
        this.mods = new int[mods.size()];
        int i = 0;
        for (int v : mods) {
            this.mods[i] = v;
            ++i;
        }
        this.str = mkString(mods);
    }

    /**
     * Combine the given colors.
     *
     * @param colors The colors to combine.
     */
    public Color(Color... colors) {
        TreeSet<Integer> mods = new TreeSet<>();

        int fg = 0;
        int bg = 0;
        all:
        for (Color c : colors) {
            for (int i : c.mods) {
                if (i == 0) {
                    fg = 0;
                    bg = 0;
                    mods.clear();
                    mods.add(0);
                    break all;
                } else {
                    mods.remove(0);

                    if (i < 10) {
                        mods.add(i);
                        mods.remove(i + 20);
                    } else if (i < 30) {
                        mods.add(i);
                        mods.remove(i - 20);
                    } else if (i < 40) {
                        fg = i;
                    } else if (i < 50) {
                        bg = i;
                    }
                }
            }
        }
        if (fg != 0) {
            mods.add(fg);
        }
        if (bg != 0) {
            mods.add(bg);
        }
        this.mods = new int[mods.size()];
        int i = 0;
        for (int v : mods) {
            this.mods[i] = v;
            ++i;
        }
        this.str = mkString(mods);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Color)) {
            return false;
        }
        Color other = (Color) o;

        return Arrays.equals(mods, other.mods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Color.class, Arrays.hashCode(mods));
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public int codepoint() {
        return -1;
    }

    @Override
    public int printableWidth() {
        // colors never take up  screen realestate.
        return 0;
    }

    @Override
    public int length() {
        return str.length();
    }

    /**
     * Generate the color string.
     *
     * @param values The values combine into the color string.
     * @return The shell color-settings string.
     */
    private static String mkString(TreeSet<Integer> values) {
        StringBuilder builder = new StringBuilder();
        builder.append("\033["); // escape.
        boolean first = true;
        for (int i : values) {
            if (first) {
                first = false;
            } else {
                builder.append(';');
            }
            builder.append(String.format("%02d", i));
        }
        builder.append("m");
        return builder.toString();
    }
}
