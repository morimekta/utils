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
package net.morimekta.util;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * String utilities.
 */
public class Strings {
    private static final String NULL = "null";

    /**
     * Properly java-escape the string for printing to console.
     * @param string The string to escape.
     * @return The escaped string.
     */
    public static String escape(CharSequence string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '\b':
                    builder.append('\\').append('b');
                    break;
                case '\t':
                    builder.append('\\').append('t');
                    break;
                case '\n':
                    builder.append('\\').append('n');
                    break;
                case '\f':
                    builder.append('\\').append('f');
                    break;
                case '\r':
                    builder.append('\\').append('r');
                    break;
                case '"':
                case '\'':
                    builder.append('\\').append(c);
                    break;
                case '\\':
                    builder.append('\\').append('\\');
                    break;
                default:
                    if (c < 32 || c == 127) {
                        builder.append(String.format("\\%03o", (int) c));
                    } else if ((127 < c && c < 160) || (8192 <= c && c < 8448) || !Character.isDefined(c)) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        return builder.toString();
    }

    /**
     * Escape a single character. It is escaped into a string, as it may become
     * more than one char when escaped.
     *
     * @param c The char to escape.
     * @return The escaped char string.
     */
    public static String escape(char c) {
        switch (c) {
            case '\b':
                return "\\b";
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\f':
                return "\\f";
            case '\r':
                return "\\r";
            case '"':
                return "\\\"";
            case '\'':
                return "\\'";
            case '\\':
                return "\\\\";
            default:
                if (c < 32 || c == 127) {
                    return String.format("\\%03o", (int) c);
                } else if ((127 < c && c < 160) || (8192 <= c && c < 8448) || !Character.isDefined(c)) {
                    return String.format("\\u%04x", (int) c);
                }
                return String.valueOf(c);
        }
    }

    /**
     * Unescape selected chars for compatability with JavaScript's encodeURI.
     * In speed critical applications this could be dropped since the
     * receiving application will certainly decode these fine.
     * Note that this function is case-sensitive.  Thus "%3f" would not be
     * unescaped.  But this is ok because it is only called with the output of
     * URLEncoder.encode which returns uppercase hex.
     * <p>
     * Example: "%3F" -&gt; "?", "%24" -&gt; "$", etc.
     *
     * @param str The string to escape.
     * @return The escaped string.
     */
    public static String unescapeForEncodeUriCompatability(String str) {
        return str.replace("%21", "!").replace("%7E", "~")
                  .replace("%27", "'").replace("%28", "(").replace("%29", ")")
                  .replace("%3B", ";").replace("%2F", "/").replace("%3F", "?")
                  .replace("%3A", ":").replace("%40", "@").replace("%26", "&")
                  .replace("%3D", "=").replace("%2B", "+").replace("%24", "$")
                  .replace("%2C", ",").replace("%23", "#");
    }

    /**
     * Join set of arbitrary values with delimiter.
     *
     * @param delimiter The delimiter.
     * @param values    The values to join.
     * @return The joined string.
     */
    public static String join(String delimiter, Object... values) {
        // Since primitive arrays does not pass as a values array, but as it's
        // single first element.
        if (values.length == 1) {
            Class<?> type = values[0].getClass();
            if (char[].class.equals(type)) {
                return joinP(delimiter, (char[]) values[0]);
            } else if (int[].class.equals(type)) {
                return joinP(delimiter, (int[]) values[0]);
            } else if (long[].class.equals(type)) {
                return joinP(delimiter, (long[]) values[0]);
            } else if (double[].class.equals(type)) {
                return joinP(delimiter, (double[]) values[0]);
            } else if (boolean[].class.equals(type)) {
                return joinP(delimiter, (boolean[]) values[0]);
            }
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(Objects.toString(value));
        }

        return builder.toString();
    }

    /**
     * Join array with delimiter.
     *
     * @param delimiter The delimiter.
     * @param chars     The char array to join.
     * @return The joined string.
     */
    public static String joinP(String delimiter, char... chars) {
        StringBuilder builder = new StringBuilder(chars.length + (delimiter.length() * chars.length));
        boolean first = true;
        for (char c : chars) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Join array with delimiter.
     *
     * @param delimiter The delimiter.
     * @param values    The int array to join.
     * @return The joined string.
     */
    public static String joinP(String delimiter, int... values) {
        StringBuilder builder = new StringBuilder(values.length + (delimiter.length() * values.length));
        boolean first = true;
        for (int i : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(Integer.toString(i));
        }
        return builder.toString();
    }

    /**
     * Join array with delimiter.
     *
     * @param delimiter The delimiter.
     * @param values    The int array to join.
     * @return The joined string.
     */
    public static String joinP(String delimiter, long... values) {
        StringBuilder builder = new StringBuilder(values.length + (delimiter.length() * values.length));
        boolean first = true;
        for (long i : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(Long.toString(i));
        }
        return builder.toString();
    }

    /**
     * Join array with delimiter.
     *
     * @param delimiter The delimiter.
     * @param values    The double array to join.
     * @return The joined string.
     */
    public static String joinP(String delimiter, double... values) {
        StringBuilder builder = new StringBuilder(values.length + (delimiter.length() * values.length));
        boolean first = true;
        for (double d : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(asString(d));
        }
        return builder.toString();
    }

    /**
     * Join array with delimiter.
     *
     * @param delimiter The delimiter.
     * @param values    The double array to join.
     * @return The joined string.
     */
    public static String joinP(String delimiter, boolean... values) {
        StringBuilder builder = new StringBuilder(values.length + (delimiter.length() * values.length));
        boolean first = true;
        for (boolean d : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(d);
        }
        return builder.toString();
    }

    /**
     * Join collection with delimiter.
     *
     * @param <T> Collection item type.
     * @param delimiter The delimiter.
     * @param strings   The string collection to join.
     * @return The joined string.
     */
    public static <T> String join(String delimiter, Collection<T> strings) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (T o : strings) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(Objects.toString(o));
        }
        return builder.toString();
    }

    /**
     * Check if the string is representing an integer (or long) value.
     *
     * @param key The key to check if is an integer.
     * @return True if key is an integer.
     */
    public static boolean isInteger(String key) {
        return INT.matcher(key)
                  .matches();
    }

    /**
     * Multiply a string N times.
     *
     * @param s   The string to multiply.
     * @param num N
     * @return The result.
     */
    public static String times(String s, int num) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; ++i) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Format a prefixed name as camelCase. The prefix is kept verbatim, while
     * tha name is split on '_' chars, and joined with each part capitalized.
     *
     * @param prefix The prefix.
     * @param name   The name to camel-case.
     * @return theCamelCasedName
     */
    public static String camelCase(String prefix, String name) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);

        String[] parts = name.split("[-._]");
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            builder.append(capitalize(part));
        }
        return builder.toString();
    }

    /**
     * Format a prefixed name as c_case. The prefix is kept verbatim, while the
     * name has a '_' character inserted before each upper-case letter, not
     * including the first character. Then the whole thing is lower-cased.
     *
     * @param prefix The prefix.
     * @param name   The name to c-case.
     * @param suffix The suffix.
     * @return the_c_cased_name
     */
    public static String c_case(String prefix, String name, String suffix) {
        // Assume we insert at most 4 '_' chars for a majority of names.
        StringBuilder builder = new StringBuilder(prefix.length() + name.length() + 5);
        builder.append(prefix);

        boolean lastUpper = true;
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                if (!lastUpper) {
                    builder.append('_');
                }
                lastUpper = true;
            } else if (c == '_' || c == '.' || c == '-') {
                builder.append('_');
                lastUpper = true;
                continue;
            } else {
                lastUpper = false;
            }
            builder.append(Character.toLowerCase(c));
        }
        builder.append(suffix);

        return builder.toString();
    }

    /**
     * Format a prefixed name as c_case. The prefix is kept verbatim, while the
     * name has a '_' character inserted before each upper-case letter, not
     * including the first character. Then the whole thing is lower-cased.
     * <p>
     * Note that this will mangle upper-case abbreviations.
     * </p>
     * @param prefix The prefix.
     * @param name   The name to c-case.
     * @return the_c_cased_name
     */
    public static String c_case(String prefix, String name) {
        return c_case(prefix, name, "");
    }

    public static String capitalize(String string) {
        return string.substring(0, 1)
                     .toUpperCase() + string.substring(1);
    }

    /**
     * Make a minimal printable string from a double value.
     *
     * @param d The double value.
     * @return The string value.
     */
    public static String asString(double d) {
        long l = (long) d;
        if (d > ((10 << 9) - 1) || (1 / d) > (10 << 6)) {
            // Scientific notation should be used.
            return new DecimalFormat("0.#########E0").format(d);
        } else if (d == (double) l) {
            // actually an integer or long value.
            return Long.toString(l);
        } else {
            return Double.toString(d);
        }
    }

    /**
     * Make a printable string from a collection using the tools here.
     *
     * @param collection The collection to stringify.
     * @return The collection string value.
     */
    public static String asString(Collection<?> collection) {
        if (collection == null) {
            return NULL;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        boolean first = true;
        for (Object item : collection) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(asString(item));
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Make a minimal printable string value from a typed map.
     *
     * @param map The map to stringify.
     * @return The resulting string.
     */
    public static String asString(Map<?, ?> map) {
        if (map == null) {
            return NULL;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(asString(entry.getKey()))
                   .append(':')
                   .append(asString(entry.getValue()));
        }
        builder.append('}');
        return builder.toString();
    }

    /**
     * Make an object into a string using the typed tools here.
     *
     * @param o The object to stringify.
     * @return The resulting string.
     */
    public static String asString(Object o) {
        if (o == null) {
            return NULL;
        } else if (o instanceof Stringable) {
            return ((Stringable) o).asString();
        } else if (o instanceof Numeric) {
            return String.format("%d", ((Numeric) o).asInteger());
        } else if (o instanceof CharSequence) {
            return String.format("\"%s\"", escape((CharSequence) o));
        } else if (o instanceof Double) {
            return asString(((Double) o).doubleValue());
        } else if (o instanceof Collection) {
            return asString((Collection<?>) o);
        } else if (o instanceof Map) {
            return asString((Map<?, ?>) o);
        } else {
            return o.toString();
        }
    }

    /*
     * The following functions are copied from the java version of
     * http://code.google.com/p/google-diff-match-patch/
     *
     * Copyright 2006 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    /**
     * Determine the common prefix of two strings
     * @param text1 First string.
     * @param text2 Second string.
     * @return The number of characters common to the start of each string.
     */
    public static int commonPrefix(String text1, String text2) {
        // Performance analysis: http://neil.fraser.name/news/2007/10/09/
        int n = Math.min(text1.length(), text2.length());
        for (int i = 0; i < n; i++) {
            if (text1.charAt(i) != text2.charAt(i)) {
                return i;
            }
        }
        return n;
    }

    /**
     * Determine the common suffix of two strings
     * @param text1 First string.
     * @param text2 Second string.
     * @return The number of characters common to the end of each string.
     */
    public static int commonSuffix(String text1, String text2) {
        // Performance analysis: http://neil.fraser.name/news/2007/10/09/
        int text1_length = text1.length();
        int text2_length = text2.length();
        int n = Math.min(text1_length, text2_length);
        for (int i = 1; i <= n; i++) {
            if (text1.charAt(text1_length - i) != text2.charAt(text2_length - i)) {
                return i - 1;
            }
        }
        return n;
    }

    /**
     * Determine if the suffix of one string is the prefix of another.
     * @param text1 First string.
     * @param text2 Second string.
     * @return The number of characters common to the end of the first
     *         string and the start of the second string.
     */
    public static int commonOverlap(String text1, String text2) {
        // Cache the text lengths to prevent multiple calls.
        int text1_length = text1.length();
        int text2_length = text2.length();
        // Eliminate the null case.
        if (text1_length == 0 || text2_length == 0) {
            return 0;
        }
        // Truncate the longer string.
        if (text1_length > text2_length) {
            text1 = text1.substring(text1_length - text2_length);
        } else if (text1_length < text2_length) {
            text2 = text2.substring(0, text1_length);
        }
        int text_length = Math.min(text1_length, text2_length);
        // Quick check for the worst case.
        if (text1.equals(text2)) {
            return text_length;
        }

        // Start by looking for a single character match
        // and increase length until no match is found.
        // Performance analysis: http://neil.fraser.name/news/2010/11/04/
        int best = 0;
        int length = 1;
        while (true) {
            String pattern = text1.substring(text_length - length);
            int found = text2.indexOf(pattern);
            if (found == -1) {
                return best;
            }
            length += found;
            if (found == 0 || text1.substring(text_length - length).equals(
                    text2.substring(0, length))) {
                best = length;
                length++;
            }
        }
    }

    // --- constants and helpers.

    // defeat instantiation.
    private Strings() {}

    private static final Pattern INT = Pattern.compile("-?[0-9]+");
}
