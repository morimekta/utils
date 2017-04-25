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
package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.util.Numeric;
import net.morimekta.util.Stringable;
import net.morimekta.util.json.JsonWriter;
import net.morimekta.util.json.PrettyJsonWriter;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import static net.morimekta.config.util.ConfigUtil.asString;

/**
 * Config formatter for JSON object syntax.
 */
public class JsonConfigFormatter implements ConfigFormatter {
    private final boolean pretty;

    public JsonConfigFormatter() {
        this(false);
    }

    public JsonConfigFormatter(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public void format(Config config, OutputStream out) {
        try {
            JsonWriter writer = pretty ? new PrettyJsonWriter(out) : new JsonWriter(out);
            writer.object();
            // Make sure entries are ordered (makes the output consistent).
            for (String key : new TreeSet<>(config.keySet())) {
                writer.key(key);
                writeValue(writer, config.get(key));
            }
            writer.endObject();
            writer.flush();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    private void writeValue(JsonWriter writer, Object value)
            throws ConfigException {
        if (value instanceof Boolean) {
            writer.value((Boolean) value);
        } else if (value instanceof Double) {
            writer.value((Double) value);
        } else if (value instanceof Number) {
            writer.value(((Number) value).longValue());
        } else if (value instanceof CharSequence || value instanceof Date) {
            writer.value(asString(value));
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            writer.array();
            for (Object o : collection) {
                writeValue(writer, o);
            }
            writer.endArray();
        } else if (value instanceof Numeric) {
            writer.value(((Numeric) value).asInteger());
        } else if (value instanceof Stringable) {
            writer.value(((Stringable) value).asString());
        } else {
            throw new ConfigException("Unknown value class: " + value.getClass().getSimpleName());
        }
    }
}
