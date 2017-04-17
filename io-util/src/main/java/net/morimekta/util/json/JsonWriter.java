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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Stack;

import static java.lang.Character.isSurrogatePair;
import static java.nio.charset.StandardCharsets.UTF_8;
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
        this(new PrintWriter(new OutputStreamWriter(out, UTF_8)));
    }

    public JsonWriter(PrintWriter writer) {
        this.writer = writer;
        this.stack = new Stack<>();

        context = new JsonContext(JsonContext.Mode.VALUE);
    }

    protected void reset() {
        writer.flush();
        stack.clear();
        context = new JsonContext(JsonContext.Mode.VALUE);
    }

    public void flush() {
        writer.flush();
    }

    public JsonWriter object() throws JsonException {
        startValue();

        stack.push(context);
        context = new JsonContext(JsonContext.Mode.MAP);
        writer.write('{');

        return this;
    }

    public JsonWriter array() throws JsonException {
        startValue();

        stack.push(context);
        context = new JsonContext(JsonContext.Mode.LIST);
        writer.write('[');

        return this;
    }

    public JsonWriter endObject() throws JsonException {
        if (!context.map()) {
            throw new JsonException("Unexpected end, not in object.");
        }
        if (context.value()) {
            throw new JsonException("Expected map value but got end.");
        }
        writer.write('}');
        context = stack.pop();
        return this;
    }

    public JsonWriter endArray() throws JsonException {
        if (!context.list()) {
            throw new JsonException("Unexpected end, not in list.");
        }
        writer.write(']');
        context = stack.pop();
        return this;
    }

    public JsonWriter key(boolean key) throws JsonException {
        startKey();

        writer.write(key ? "\"true\":" : "\"false\":");
        return this;
    }

    public JsonWriter key(byte key) throws JsonException {
        startKey();

        writer.write('\"');
        writer.print((int) key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    public JsonWriter key(short key) throws JsonException {
        startKey();

        writer.write('\"');
        writer.print((int) key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    public JsonWriter key(int key) throws JsonException {
        startKey();

        writer.write('\"');
        writer.print(key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    public JsonWriter key(long key) throws JsonException {
        startKey();

        writer.write('\"');
        writer.print(key);
        writer.write('\"');
        writer.write(':');
        return this;
    }

    public JsonWriter key(double key) throws JsonException {
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

    public JsonWriter key(CharSequence key) throws JsonException {
        startKey();

        if (key == null) {
            throw new JsonException("Expected map key, but got null.");
        }

        writeQuoted(key);
        writer.write(':');
        return this;
    }

    public JsonWriter key(Binary key) throws JsonException {
        startKey();

        if (key == null) {
            throw new JsonException("Expected map key, but got null.");
        }

        writer.write('\"');
        writer.write(key.toBase64());
        writer.write('\"');
        writer.write(':');
        return this;
    }

    public JsonWriter keyLiteral(CharSequence key) throws JsonException {
        startKey();

        if (key == null) {
            throw new JsonException("Expected map key, but got null.");
        }

        writer.write(key.toString());
        writer.write(':');
        return this;
    }

    public JsonWriter value(boolean value) throws JsonException {
        startValue();

        writer.write(value ? kTrue : kFalse);
        return this;
    }

    public JsonWriter value(byte value) throws JsonException {
        startValue();

        writer.print((int) value);
        return this;
    }

    public JsonWriter value(short value) throws JsonException {
        startValue();

        writer.print((int) value);
        return this;
    }

    public JsonWriter value(int value) throws JsonException {
        startValue();

        writer.print(value);
        return this;
    }

    public JsonWriter value(long number) throws JsonException {
        startValue();

        writer.print(number);
        return this;
    }

    public JsonWriter value(double number) throws JsonException {
        startValue();

        final long i = (long) number;
        if (number == (double) i) {
            writer.print(i);
        } else {
            writer.print(number);
        }
        return this;
    }

    public JsonWriter value(CharSequence value) throws JsonException {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writeQuoted(value);
        }
        return this;
    }

    public JsonWriter value(Binary value) throws JsonException {
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

    public JsonWriter valueLiteral(CharSequence value) throws JsonException {
        startValue();

        if (value == null) {
            writer.write(kNull);
        } else {
            writer.write(value.toString());
        }
        return this;
    }

    protected void startKey() throws JsonException {
        if (!context.map()) {
            throw new JsonException("Unexpected map key outside map.");
        }
        if (!context.key()) {
            throw new JsonException("Unexpected map key, expected value or end.");
        }

        if (context.num > 0) {
            writer.write(',');
        }

        ++context.num;
        context.expect = JsonContext.Expect.VALUE;
    }

    protected boolean startValue() throws JsonException {
        if (!context.value()) {
            if (context.expect == JsonContext.Expect.VALUE) {
                throw new JsonException("Value already written, and not in container.");
            } else {
                throw new JsonException("Expected map key, but got value.");
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
    private void writeQuoted(CharSequence string) {
        if (string != null && string.length() != 0) {
            int len = string.length();
            writer.write('\"');

            for (int i = 0; i < len; ++i) {
                char cp = string.charAt(i);
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
                        char c2 = (i + 1) < string.length() ? string.charAt(i + 1) : 0;
                        if (((i + 1) < string.length()) && isSurrogatePair(cp, c2)) {
                            writer.format("\\u%04x", (int) cp);
                            writer.format("\\u%04x", (int) c2);
                            ++i;
                        } else if (isConsolePrintable(cp)) {
                            writer.write(cp);
                        } else {
                            writer.format("\\u%04x", (int) cp);
                        }
                        break;
                }
            }

            writer.write('\"');
        } else {
            writer.write("\"\"");
        }
    }
}
