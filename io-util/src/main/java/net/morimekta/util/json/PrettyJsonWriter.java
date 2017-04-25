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
import net.morimekta.util.io.IndentedPrintWriter;

import java.io.OutputStream;

/**
 * JSON Writer that automatically prints prettified JSON based on the
 * inherent structure of the data.
 *
 * This is deliberately kept separate from the default JsonWriter to
 * keep the indention logic away from the default JsonWriter (since it is
 * pretty latency sensitive).
 *
 * Can be used in place of the default JsonWriter, and will print pretty-
 * printed JSON out of the box. Default indentation is from the indented
 * print writer, which is 4 spaces,
 */
public class PrettyJsonWriter extends JsonWriter {
    private static final int SPACE = ' ';

    private final IndentedPrintWriter writer;

    /**
     * Create a Prettified JSON writer that writes to the given output stream.
     *
     * @param out The stream to write to.
     */
    public PrettyJsonWriter(OutputStream out) {
        this(new IndentedPrintWriter(out));
    }

    /**
     * Create a Prettified JSON writer that writes to the given indented print writer.
     *
     * @param writer The writer to write to.
     */
    public PrettyJsonWriter(IndentedPrintWriter writer) {
        super(writer);
        this.writer = writer;
    }

    @Override
    public PrettyJsonWriter object() {
        super.object();
        writer.begin();
        return this;
    }

    @Override
    public PrettyJsonWriter array() {
        super.array();
        writer.begin();
        return this;
    }

    @Override
    public PrettyJsonWriter endObject() {
        writer.end();
        if (context.num != 0) {
            writer.appendln();
        }
        super.endObject();
        return this;
    }

    @Override
    public PrettyJsonWriter endArray() {
        writer.end();
        if (context.num != 0) {
            writer.appendln();
        }
        super.endArray();
        return this;
    }

    @Override
    public PrettyJsonWriter key(boolean key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(byte key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(short key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(int key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(long key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(double key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(CharSequence key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(Binary key) {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter keyLiteral(CharSequence key) {
        super.keyLiteral(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter value(boolean value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(byte value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(short value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(int value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(long value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(double value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(CharSequence value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(Binary value) {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter valueLiteral(CharSequence value) {
        super.valueLiteral(value);
        return this;
    }

    @Override
    protected void startKey() {
        super.startKey();
        writer.appendln();
    }

    @Override
    protected boolean startValue() {
        if (super.startValue()) {
            writer.appendln();
            return true;
        }
        return false;
    }
}
