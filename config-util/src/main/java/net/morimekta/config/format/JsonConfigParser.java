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
import net.morimekta.config.ConfigBuilder;
import net.morimekta.config.ConfigException;
import net.morimekta.config.IncompatibleValueException;
import net.morimekta.config.impl.ImmutableConfig;
import net.morimekta.config.impl.SimpleConfig;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonToken;
import net.morimekta.util.json.JsonTokenizer;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Config parser for JSON object syntax.
 */
public class JsonConfigParser implements ConfigParser {
    @Override
    public Config parse(InputStream in) {
        try {
            JsonTokenizer tokenizer = new JsonTokenizer(in);
            JsonToken token = tokenizer.expect("config start");
            if (!token.isSymbol(JsonToken.kMapStart)) {
                throw new ConfigException("Illegal json start token: %s", token);
            }
            return parseConfig(tokenizer);
        } catch (JsonException | IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    private Config parseConfig(JsonTokenizer tokenizer)
            throws ConfigException, IOException, JsonException {
        ConfigBuilder config = new SimpleConfig();
        JsonToken token = tokenizer.peek("for empty map");
        if (token.isSymbol(JsonToken.kMapEnd)) {
            tokenizer.next();
            return ImmutableConfig.copyOf(config);
        }

        char sep = token.charAt(0);
        while (sep != JsonToken.kMapEnd) {
            JsonToken jkey = tokenizer.expect("map key");
            // No need to decode the key.
            String key = jkey.substring(1, -1).asString();
            tokenizer.expectSymbol("", JsonToken.kKeyValSep);

            token = tokenizer.expect("Map value.");
            switch (token.type) {
                case SYMBOL:
                    switch (token.charAt(0)) {
                        case JsonToken.kListStart:
                            config.putCollection(key, parseCollection(tokenizer));
                            break;
                        default:
                            throw new IncompatibleValueException("No supported value type for " + token);
                    }
                    break;
                case LITERAL:
                    config.putString(key, token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (token.isInteger()) {
                        config.putLong(key, token.longValue());
                    } else {
                        config.putDouble(key, token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    config.putBoolean(key, token.booleanValue());
                    break;
            }

            sep = tokenizer.expectSymbol("", JsonToken.kMapEnd, JsonToken.kListSep);
        }

        return ImmutableConfig.copyOf(config);
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> parseCollection(JsonTokenizer tokenizer)
            throws ConfigException, IOException, JsonException {
        JsonToken token = tokenizer.peek("for empty list");
        if (token.isSymbol(JsonToken.kListEnd)) {
            tokenizer.next();
            return ImmutableList.of();
        }

        ImmutableList.Builder<T> builder = ImmutableList.builder();
        char sep = token.charAt(0);
        while (sep != JsonToken.kListEnd) {
            token = tokenizer.expect("array value.");
            switch (token.type) {
                case LITERAL:
                    builder.add((T) token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (token.isInteger()) {
                        builder.add((T) (Object) token.longValue());
                    } else {
                        builder.add((T) (Object) token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    builder.add((T) (Object) token.booleanValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled JSON value token: " + token);
            }

            sep = tokenizer.expectSymbol("list sep or end", JsonToken.kListEnd, JsonToken.kListSep);
        }

        return builder.build();
    }
}
