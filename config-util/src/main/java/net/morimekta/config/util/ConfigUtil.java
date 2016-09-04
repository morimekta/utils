/*
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
package net.morimekta.config.util;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.IncompatibleValueException;
import net.morimekta.config.format.ConfigParser;
import net.morimekta.config.format.JsonConfigParser;
import net.morimekta.config.format.PropertiesConfigParser;
import net.morimekta.config.format.TomlConfigParser;
import net.morimekta.util.Numeric;
import net.morimekta.util.Stringable;
import net.morimekta.util.Strings;

import com.google.common.base.MoreObjects;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * General utility functions for config.
 */
public class ConfigUtil {
    /**
     * Get the parser that matches the file format associated with the given
     * file suffix. The given string must include the last '.' in order to be
     * valid.
     *
     * @param name The file name.
     * @return The associated config parser.
     * @throws ConfigException If no known parser is associated with the file
     *         suffix, or the file name does not have a dot.
     */
    public static ConfigParser getParserForName(String name) {
        int lastDot = name.lastIndexOf(".");
        if (lastDot < 0)  {
            throw new ConfigException("No file suffix in name: " + name);
        }
        String suffix = name.substring(lastDot);
        switch (suffix.toLowerCase()) {
            case ".toml":
            case ".ini":
                return new TomlConfigParser();
            case ".json":
                return new JsonConfigParser();
            case ".properties":
                return new PropertiesConfigParser();
            default:
                throw new ConfigException("Unknown config file suffix: " + suffix);
        }
    }

    /**
     * Convert the value to a boolean.
     *
     * @param value The value instance.
     * @return The boolean value.
     */
    public static boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Double || value instanceof Float) {
            throw new IncompatibleValueException("Unable to convert double value to boolean");
        } else if (value instanceof Number) {
            long l = ((Number) value).longValue();
            if (l == 0L) return false;
            if (l == 1L) return true;
            throw new IncompatibleValueException("Unable to convert number " + l + " to boolean");
        } else if (value instanceof CharSequence) {
            switch (value.toString().toLowerCase()) {
                case "0":
                case "n":
                case "f":
                case "no":
                case "false":
                    return false;
                case "1":
                case "y":
                case "t":
                case "yes":
                case "true":
                    return true;
                default:
                    throw new IncompatibleValueException(String.format(
                            "Unable to parse the string \"%s\" to boolean",
                            Strings.escape(value.toString())));
            }
        }
        throw new IncompatibleValueException("Unable to convert " + value.getClass().getSimpleName() + " to a boolean");
    }

    /**
     * Convert the value to an integer.
     *
     * @param value The value instance.
     * @return The integer value.
     */
    public static int asInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof Numeric) {
            return ((Numeric) value).asInteger();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof CharSequence) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException nfe) {
                throw new IncompatibleValueException(
                        "Unable to parse string \"" + Strings.escape(value.toString()) +
                        "\" to an int", nfe);
            }
        } else if (value instanceof Date) {
            // Convert date timestamp to seconds since epoch.
            return (int) (((Date) value).getTime() / 1000);
        }
        throw new IncompatibleValueException("Unable to convert " + value.getClass().getSimpleName() + " to an int");
    }

    /**
     * Convert the value to a long.
     *
     * @param value The value instance.
     * @return The long value.
     */
    public static long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof Numeric) {
            return ((Numeric) value).asInteger();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        } else if (value instanceof CharSequence) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException nfe) {
                throw new IncompatibleValueException("Unable to parse string \"" + Strings.escape(value.toString()) +
                                                     "\" to a long", nfe);
            }
        } else if (value instanceof Date) {
            // Return date timestamp.
            return ((Date) value).getTime();
        }
        throw new IncompatibleValueException("Unable to convert " + value.getClass().getSimpleName() + " to a long");

    }

    /**
     * Convert the value to a souble.
     *
     * @param value The value instance.
     * @return The double value.
     */
    public static double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof Numeric) {
            return ((Numeric) value).asInteger();
        } else if (value instanceof CharSequence) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException nfe) {
                throw new IncompatibleValueException("Unable to parse string \"" + Strings.escape(value.toString()) +
                                                     "\" to a double", nfe);
            }
        }
        throw new IncompatibleValueException(
                "Unable to convert " + value.getClass().getSimpleName() + " to a double");
    }

    /**
     * Convert the value to a string.
     *
     * @param value The value instance.
     * @return The string value.
     */
    public static String asString(Object value) {
        if (value instanceof Collection || value instanceof Map || value instanceof Config) {
            throw new IncompatibleValueException(
                    "Unable to convert " + value.getClass().getSimpleName() + " to a string");
        } else if (value instanceof Stringable) {
            return ((Stringable) value).asString();
        } else if (value instanceof Date) {
            Instant instant = ((Date) value).toInstant();
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        }
        return Objects.toString(value);
    }

    /**
     * Convert the value to a date.
     *
     * @param value The value instance.
     * @return The string value.
     */
    public static Date asDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof CharSequence) {
            String date = value.toString().trim();
            try {
                LocalDateTime time;
                if (date.endsWith("Z")) {
                    time = LocalDateTime.parse((CharSequence) value,
                                               DateTimeFormatter.ISO_INSTANT.withZone(Clock.systemUTC().getZone()));
                } else {
                    time = LocalDateTime.parse((CharSequence) value,
                                               DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(Clock.systemUTC().getZone()));
                }
                return new Date(time.atZone(Clock.systemUTC()
                                                 .getZone()).toInstant().toEpochMilli());
            } catch (RuntimeException e) {
                throw new ConfigException(e, "Unable to parse date: \"" + date + "\": " + e.getMessage());
            }
        } else if (value instanceof Long) {
            // Longs are assumed to be java time (millis since epoch).
            return new Date((Long) value);
        } else if (value instanceof Integer) {
            // Integers are assumed to be unix time (seconds since epoch).
            return new Date(((Integer) value).longValue() * 1000L);
        } else {
            throw new IncompatibleValueException("Unable to convert " + value.getClass().getSimpleName() + " to a date");
        }
    }

    /**
     * Convert the value to a collection.
     *
     * @param value The value instance.
     * @param <T> The collection item type.
     * @return The collection value.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> asCollection(Object value) {
        if (value instanceof Collection) {
            return (Collection) value;
        }
        throw new IncompatibleValueException(
                "Unable to convert " + value.getClass().getSimpleName() + " to a collection");
    }

    /**
     * Make collection into a string array.
     *
     * @param value The value instance.
     * @return The array.
     */
    public static String[] asStringArray(Object value) {
        List<String> collection = asCollection(value).stream()
                                                     .map(ConfigUtil::asString)
                                                     .collect(Collectors.toList());
        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Make collection into a boolean array.
     *
     * @param collection The value instance.
     * @return The array.
     */
    public static boolean[] asBooleanArray(Collection collection) {
        boolean[] result = new boolean[collection.size()];
        int i = 0;
        for (Object c : collection) {
            result[i++] = asBoolean(c);
        }
        return result;
    }

    /**
     * Make collection into an integer array.
     *
     * @param collection The value instance.
     * @return The array.
     */
    public static int[] asIntegerArray(Collection collection) {
        int[] result = new int[collection.size()];
        int i = 0;
        for (Object c : collection) {
            result[i++] = asInteger(c);
        }
        return result;
    }

    /**
     * Make collection into a long array.
     *
     * @param collection The value instance.
     * @return The array.
     */
    public static long[] asLongArray(Collection collection) {
        long[] result = new long[collection.size()];
        int i = 0;
        for (Object c : collection) {
            result[i++] = asLong(c);
        }
        return result;
    }

    /**
     * Make collection into a double array.
     *
     * @param collection The value instance.
     * @return The array.
     */
    public static double[] asDoubleArray(Collection collection) {
        double[] result = new double[collection.size()];
        int i = 0;
        for (Object c : collection) {
            result[i++] = asDouble(c);
        }
        return result;
    }

    /**
     * Proper equality between configs.
     *
     * @param first The first config.
     * @param second The second config.
     * @return True if the two configs are equal.
     */
    public static boolean equals(Config first, Config second) {
        if (first == second) {
            return true;
        } else if (first == null || second == null) {
            return false;
        }

        Set<String> keys = first.keySet();
        if (!keys.equals(second.keySet())) {
            return false;
        }
        for (String key : keys) {
            if (!equals(first.get(key), second.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate a config hash for the config. It should be as type agnostic
     * as possible by converting all values into strings.
     *
     * @param config The config to hash.
     * @return The hash code value.
     */
    public static int hashCode(Config config) {
        Set<String> keys = config.keySet();
        int hash = Objects.hash(Config.class);
        for (String key : keys) {
            hash = Objects.hash(hash, config.getString(key));
        }
        return hash;
    }

    /**
     * Proper value equality checker.
     *
     * @param first The first value.
     * @param second The second value.
     * @return True if the two values are equal.
     */
    public static boolean equals(Object first, Object second) {
        try {
            if (first == second) {
                return true;
            } else if (first == null || second == null) {
                return false;
            }

            // if any of the two are *not* a char sequence, try to use that type
            // to compare.
            if (first instanceof Double || first instanceof Float ||
                second instanceof Double || second instanceof Float) {
                return asDouble(first) == asDouble(second);
            } else if (first instanceof Long || second instanceof Long) {
                return asLong(first) == asLong(second);
            } else if (first instanceof Number || second instanceof Number) {
                return asInteger(first) == asInteger(second);
            } else if (first instanceof Boolean || second instanceof Boolean) {
                return asBoolean(first) == asBoolean(second);
            } else if (first instanceof Set && second instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<Object> f = (Set) first;
                @SuppressWarnings("unchecked")
                Set<Object> s = (Set) second;
                // If we compare two sets, compare objects directly, as this time the
                // encoding class does matter.
                return f.size() == s.size() && f.containsAll(s);
            } else if (first instanceof Collection || second instanceof Collection) {
                Collection f = asCollection(first);
                Collection s = asCollection(second);

                if (f.size() != s.size()) {
                    return false;
                }

                Iterator fi = f.iterator();
                Iterator si = s.iterator();

                while (fi.hasNext() || si.hasNext()) {
                    if (!equals(fi.next(), si.next())) {
                        return false;
                    }
                }
                return !(fi.hasNext() || si.hasNext());
            } else if (first instanceof CharSequence || second instanceof CharSequence) {
                return asString(first).equals(asString(second));
            } else {
                return first.equals(second);
            }
        } catch (IncompatibleValueException e) {
            return false;
        }
    }

    /**
     * Make a proper toString value for the config.
     *
     * @param config The config to make toString for.
     * @return The toString value.
     */
    public static String toString(Config config) {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(config);
        for (String key : config.keySet()) {
            helper.add(key, config.get(key));
        }
        return helper.toString();
    }

    /**
     * Get the layer name based on the supplier.
     *
     * @param layer The layer to get the name of.
     * @return The layer name.
     */
    public static String getLayerName(Supplier<Config> layer) {
        String str = layer.toString();
        if (str.contains("$$Lambda$")) {
            return String.format("InMemorySupplier{%s}",
                                 layer.get().getClass().getSimpleName());
        }
        return str;
    }

    // -- defeat instantiation
    private ConfigUtil() {}
}
