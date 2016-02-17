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
    public PrettyJsonWriter object() throws JsonException {
        super.object();
        writer.begin();
        return this;
    }

    @Override
    public PrettyJsonWriter array() throws JsonException {
        super.array();
        writer.begin();
        return this;
    }

    @Override
    public PrettyJsonWriter endObject() throws JsonException {
        writer.end();
        if (context.num != 0) {
            writer.appendln();
        }
        super.endObject();
        return this;
    }

    @Override
    public PrettyJsonWriter endArray() throws JsonException {
        writer.end();
        if (context.num != 0) {
            writer.appendln();
        }
        super.endArray();
        return this;
    }

    @Override
    public PrettyJsonWriter key(boolean key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(byte key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(short key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(int key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(long key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(double key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(CharSequence key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter key(Binary key) throws JsonException {
        super.key(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter keyLiteral(CharSequence key) throws JsonException {
        super.keyLiteral(key);
        writer.write(SPACE);
        return this;
    }

    @Override
    public PrettyJsonWriter value(boolean value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(byte value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(short value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(int value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(long value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(double value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(CharSequence value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter value(Binary value) throws JsonException {
        super.value(value);
        return this;
    }

    @Override
    public PrettyJsonWriter valueLiteral(CharSequence value) throws JsonException {
        super.valueLiteral(value);
        return this;
    }

    @Override
    protected void startKey() throws JsonException {
        super.startKey();
        writer.appendln();
    }

    @Override
    protected boolean startValue() throws JsonException {
        if (super.startValue()) {
            writer.appendln();
            return true;
        }
        return false;
    }
}
