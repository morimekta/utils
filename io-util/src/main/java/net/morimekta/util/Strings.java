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
     * Join set of strings with delimiter.
     *
     * @param delimiter The delimiter.
     * @param strings   The strings to join.
     * @return The joined string.
     */
    public static String join(String delimiter, String... strings) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String string : strings) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(string);
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
    public static String join(String delimiter, char... chars) {
        StringBuilder builder = new StringBuilder(chars.length + (delimiter.length() * chars.length));
        boolean first = true;
        for (char string : chars) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(string);
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
            builder.append(o.toString());
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
            if (Character.isUpperCase(c)) {
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
     * Make a minimal printable string from a binary value.
     *
     * @param bytes The binary value.
     * @return The string value.
     */
    public static String asString(Binary bytes) {
        if (bytes == null) {
            return NULL;
        }
        return String.format("b64(%s)", bytes.toBase64());
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
        } else if (o instanceof CharSequence) {
            return String.format("\"%s\"", escape((CharSequence) o));
        } else if (o instanceof Map) {
            return asString((Map<?, ?>) o);
        } else if (o instanceof Collection) {
            return asString((Collection<?>) o);
        } else if (o instanceof Binary) {
            return asString((Binary) o);
        } else if (o instanceof Double) {
            return asString(((Double) o).doubleValue());
        } else {
            return o.toString();
        }
    }

    // defeat instantiation.
    private Strings() {}

    private static final Pattern INT = Pattern.compile("-?[0-9]+");
}
