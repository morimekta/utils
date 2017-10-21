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
package net.morimekta.util.json;

import net.morimekta.util.Binary;
import net.morimekta.util.io.Utf8StreamWriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Stack;

import static java.lang.Character.isSurrogate;
import static net.morimekta.util.Strings.isConsolePrintable;

/**
 * IO-optimized JSON writer.
 */
public class JsonWriter {
    public static final String kNull  = "null";
    public static final String kTrue  = "true";
    public static final String kFalse = "false";

    private final PrintWriter        writer;
    private final Stack<JsonContext> stack;

    protected JsonContext context;

    public JsonWriter(OutputStream out) {
        this(new PrintWriter(new Utf8StreamWriter(out)));
    }

    public JsonWriter(PrintWriter writer) {
        this.writer = writer;
        this.stack = new Stack<>();

        context = new JsonContext(JsonContext.Mode.VALUE);
    }

    /**
     * Reset the state of the writer and flush already written content.
     */
    protected void reset() {
        writer.flush();
        stack.clear();
        context = new JsonContext(JsonContext.Mode.VALUE);
    }

    /**
     * Flush the internal writer.
     */
    public void flush() {
        writer.flush();
    }

    /**
     * Start an object value.
     *
     * @return The JSON Writer.
     */
    public JsonWriter object() {
        startValue();

        stack.push(context);
        context = new JsonContext(JsonContext.Mode.MAP);
        writer.write('{');

        return this;
    }

    /**
     * Start an array value.
     *
     * @return The JSON Writer.
     */
    public JsonWriter array() {
        startValue();

        stack.push(context);
        context = new JsonContext(JsonContext.Mode.LIST);
        writer.write('[');

        return this;
    }

    /**
     * The the ongoing object.
     *
     * @return The JSON Writer.
     */
    public JsonWriter endObject() {
        if (!context.map()) {
            throw new IllegalStateException("Unexpected end, not in object.");
        }
        if (context.value()) {
            throw new IllegalStateException("Expected map value but got end.");
        }
        writer.write('}');
        context = stack.pop();
        return this;
    }

    /**
     * End the ongoing array.
     *
     * @return The JSON Writer.
     */
    public JsonWriter endArray() {
        if (!context.list()) {
            throw new IllegalStateException("Unexpected end, not in list.");
        }
        writer.write(']');
        context = stack.pop();
        return this;
    }

    /**
     * Write the boolean as object key.
     *
     * @param key The boolean key.
     * @return The JSON Writer.
     */
    public JsonWriter key(boolean key) {
        startKey();

        writer.write(key ? "\"true\":" : "\"false\":");
        return this;
    }

    /**
     * Write the byte as object key.
     *
     * @param key The byte key.
     * @return The JSON Writer.
     */
    public JsonWriter key(byte key) {
        startKey();

        writer.write('\"');
        writer.print((int) key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the short as object key.
     *
     * @param key The short key.
     * @return The JSON Writer.
     */
    public JsonWriter key(short key) {
        startKey();

        writer.write('\"');
        writer.print((int) key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the int as object key.
     *
     * @param key The int key.
     * @return The JSON Writer.
     */
    public JsonWriter key(int key) {
        startKey();

        writer.write('\"');
        writer.print(key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the long as object key.
     *
     * @param key The long key.
     * @return The JSON Writer.
     */
    public JsonWriter key(long key) {
        startKey();

        writer.write('\"');
        writer.print(key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the double as object key.
     *
     * @param key The double key.
     * @return The JSON Writer.
     */
    public JsonWriter key(double key) {
        startKey();

        writer.write('\"');
        final long i = (long) key;
        if (key == (double) i) {
            writer.print(i);
        } else {
            writer.print(key);
        }
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the string as object key.
     *
     * @param key The string key.
     * @return The JSON Writer.
     */
    public JsonWriter key(CharSequence key) {
        startKey();

        if (key == null) {
            throw new IllegalArgumentException("Expected map key, but got null.");
        }

        writeQuotedAndEscaped(key);
        writer.write(':');
        return this;
    }

    /**
     * Write the string as object key without escaping.
     *
     * @param key The string key.
     * @return The JSON Writer.
     */
    public JsonWriter keyUnescaped(CharSequence key) {
        startKey();

        if (key == null) {
            throw new IllegalArgumentException("Expected map key, but got null.");
        }

        writer.write('\"');
        writer.write(key.toString());
        writer.write('\"');
        writer.write(':');
        return this;

    }

    /**
     * Write the binary as object key.
     *
     * @param key The binary key.
     * @return The JSON Writer.
     */
    public JsonWriter key(Binary key) {
        startKey();

        if (key == null) {
            throw new IllegalArgumentException("Expected map key, but got null.");
        }

        writer.write('\"');
        writer.write(key.toBase64());
        writer.write('\"');
        writer.write(':');
        return this;
    }

    /**
     * Write the string key without quoting or escaping.
     *
     * @param key The raw string key.
     * @return The JSON Writer.
     */
    public JsonWriter keyLiteral(CharSequence key) {
        startKey();

        if (key == null) {
            throw new IllegalArgumentException("Expected map key, but got null.");
        }

        writer.write(key.toString());
        writer.write(':');
        return this;
    }

    /**
     * Write boolean value.
     *
     * @param value The boolean value.
     * @return The JSON Writer.
     */
    public JsonWriter value(boolean value) {
        startValue();

        writer.write(value ? kTrue : kFalse);
        return this;
    }

    /**
     * Write byte value.
     *
     * @param value The byte value.
     * @return The JSON Writer.
     */
    public JsonWriter value(byte value) {
        startValue();

        writer.print((int) value);
        return this;
    }

    /**
     * Write short value.
     *
     * @param value The short value.
     * @return The JSON Writer.
     */
    public JsonWriter value(short value) {
        startValue();

        writer.print((int) value);
        return this;
    }

    /**
     * Write int value.
     *
     * @param value The int value.
     * @return The JSON Writer.
     */
    public JsonWriter value(int value) {
        startValue();

        writer.print(value);
        return this;
    }

    /**
     * Write long value.
     *
     * @param value The long value.
     * @return The JSON Writer.
     */
    public JsonWriter value(long value) {
        startValue();

        writer.print(value);
        return this;
    }

    /**
     * Write double value.
     *
     * @param value The double value.
     * @return The JSON Writer.
     */
    public JsonWriter value(double value) {
        startValue();

        final long i = (long) value;
        if (value == (double) i) {
            writer.print(i);
        } else {
            writer.print(value);
        }
        return this;
    }

    /**
     * Write unicode string value.
     *
     * @param value The string value.
     * @return The JSON Writer.
     */
    public JsonWriter value(CharSequence value) {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writeQuotedAndEscaped(value);
        }
        return this;
    }

    /**
     * Write a string unescaped value.
     *
     * @param value The string value.
     * @return The JSON Writer.
     */
    public JsonWriter valueUnescaped(CharSequence value) {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writer.write('\"');
            writer.write(value.toString());
            writer.write('\"');
        }
        return this;
    }

    /**
     * Write binary value.
     *
     * @param value The binary value.
     * @return The JSON Writer.
     */
    public JsonWriter value(Binary value) {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writer.write('\"');
            writer.write(value.toBase64());
            writer.write('\"');
        }
        return this;
    }

    /**
     * Write a literal string as value. Not quoted and not escaped.
     *
     * @param value The raw string value.
     * @return The JSON Writer.
     */
    public JsonWriter valueLiteral(CharSequence value) {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writer.write(value.toString());
        }
        return this;
    }

    protected void startKey() {
        if (!context.map()) {
            throw new IllegalStateException("Unexpected map key outside map.");
        }
        if (!context.key()) {
            throw new IllegalStateException("Unexpected map key, expected value or end.");
        }

        if (context.num > 0) {
            writer.write(',');
        }

        ++context.num;
        context.expect = JsonContext.Expect.VALUE;
    }

    protected boolean startValue() {
        if (!context.value()) {
            if (context.expect == JsonContext.Expect.VALUE) {
                throw new IllegalStateException("Value already written, and not in container.");
            } else {
                throw new IllegalStateException("Expected map key, but got value.");
            }
        }
        if (context.list()) {
            if (context.num > 0) {
                writer.write(',');
            }
            ++context.num;
            return true;
        } else if (context.map()) {
            context.expect = JsonContext.Expect.KEY;
        } else {
            ++context.num;
        }
        return false;
    }

    // Ported from org.json JSONObject.quote and modified for local use.
    private void writeQuotedAndEscaped(CharSequence string) {
        if (string != null && string.length() != 0) {
            int len = string.length();
            writer.write('\"');

            for (int i = 0; i < len; ++i) {
                char cp = string.charAt(i);
                if ((cp < 0x7f &&
                     cp >= 0x20 &&
                     cp != '\"' &&
                     cp != '\\') ||
                    (cp > 0x7f &&
                     isConsolePrintable(cp) &&
                     !isSurrogate(cp))) {
                    // quick bypass for direct printable chars.
                    writer.write(cp);
                } else {
                    switch (cp) {
                        case '\b':
                            writer.write("\\b");
                            break;
                        case '\t':
                            writer.write("\\t");
                            break;
                        case '\n':
                            writer.write("\\n");
                            break;
                        case '\f':
                            writer.write("\\f");
                            break;
                        case '\r':
                            writer.write("\\r");
                            break;
                        case '\"':
                        case '\\':
                            writer.write('\\');
                            writer.write(cp);
                            break;
                        default:
                            if (isSurrogate(cp) && (i + 1) < string.length()) {
                                char c2 = (i + 1) < string.length() ? string.charAt(i + 1) : 0;
                                writer.format("\\u%04x", (int) cp);
                                writer.format("\\u%04x", (int) c2);
                                ++i;
                            } else {
                                writer.format("\\u%04x", (int) cp);
                            }
                            break;
                    }
                }
            }

            writer.write('\"');
        } else {
            writer.write("\"\"");
        }
    }
}
