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
package net.morimekta.config;

import net.morimekta.util.Numeric;
import net.morimekta.util.Strings;

import java.util.Collection;
import java.util.Objects;

/**
 * Config value holder class. This is primarily an internal class, but may be
 * exposed so various iterators can iterate with both getType and value from the
 * config entry.
 */
public abstract class Value {
    public static boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Double) {
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

    public static int asInteger(Object value) {
        if (value instanceof Numeric) {
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

    public static String asString(Object value) {
        return Objects.toString(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> asCollection(Object value) {
        if (value instanceof Collection) {
            return (Collection) value;
        }
        throw new IncompatibleValueException(
                "Unable to convert " + value.getClass().getSimpleName() + " to a collection");
    }
}
