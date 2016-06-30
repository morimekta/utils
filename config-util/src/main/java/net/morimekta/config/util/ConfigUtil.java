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
import net.morimekta.config.IncompatibleValueException;
import net.morimekta.util.Strings;

import com.google.common.base.MoreObjects;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config value utility.
 */
public class ConfigUtil {
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
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        } else if (value instanceof CharSequence) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException nfe) {
                throw new IncompatibleValueException("Unable to parse string \"" + Strings.escape(value.toString()) +
                                                     "\" to a long", nfe);
            }
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
        }
        return Objects.toString(value);
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
            } else if (first instanceof Number || second instanceof Number) {
                return asLong(first) == asLong(second);
            } else if (first instanceof Boolean || second instanceof Boolean) {
                return asBoolean(first) == asBoolean(second);
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
            } else {
                return asString(first).equals(asString(second));
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

    // -- defeat instantiation
    private ConfigUtil() {}
}
