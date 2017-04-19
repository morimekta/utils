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

import com.google.common.collect.ImmutableList;
import net.morimekta.config.Config;
import net.morimekta.config.ConfigBuilder;
import net.morimekta.config.ConfigException;
import net.morimekta.config.impl.ImmutableConfig;
import net.morimekta.config.impl.SimpleConfig;
import net.morimekta.util.Strings;
import net.morimekta.util.io.IOUtils;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonToken;
import net.morimekta.util.json.JsonTokenizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Config formatting for parsing (and formatting) .INI style config files.
 * It follows more or less the <a href="https://github.com/toml-lang/toml">TOML-lang</a>
 * syntax with some exceptions.
 *
 * The syntax is somewhat simpler than the TOML
 *
 * <ul>
 *     <li>The Structure is completely 2-tier, section and property.</li>
 *     <li>No support for multi-line basic strings (triple-quoted).</li>
 *     <li>No support for literal strings (quoted with "'").</li>
 *     <li>Integers follow JSON syntax only, so no '_' separators, no hex.</li>
 *
 *     <li>Property keys are <b>never</b> quoted, but supports containing
 *         '.' regardless.</li>
 *     <li>The end result is a flat map of
 *         <code>section-key + '.' + property-key = value</code>. Or
 *         <code>property-key = value</code> for properties before the first section.
 *         </li>
 * </ul>
 *
 * <a href="https://plugins.jetbrains.com/plugin/8195?pr=idea">TOML IntelliJ plugin</a>
 *
 * Example .toml file.
 *
 * <code>
 * # comment
 * key.outside.section = 5
 *
 * [section]
 * key.string = "value with\n\t - escaping"
 * key.sequence = [ "sequence", "follows", "JSON", "syntax" ]
 *
 * # comments can be anywhere, as long as it's on it's own line.
 * key.int = 1234567890
 * </code>
 *
 * The keys inside a section is prepended with the section name and '.'. This
 * way the real key of the 'key.string' property is going to be
 * 'section.key.string'. The structure is completely flattened, no section
 * structure is kept.
 */
public class TomlConfigParser implements ConfigParser {
    @Override
    public Config parse(InputStream in) {
        try {
            ConfigBuilder config = new SimpleConfig();

            // Part 1: Strip comments.
            String all = IOUtils.readString(in);
            String[] lines = all.split("[\\n]");
            for (int i = 0; i < lines.length; ++i) {
                lines[i] = stripComment(lines[i]);
            }
            // Insert double newlines, to make it easier to detect garbage.
            all = String.join("\n\n", (CharSequence[]) lines);

            // Part 2: Parse remaining data.
            ByteArrayInputStream bais = new ByteArrayInputStream(all.getBytes(UTF_8));
            JsonTokenizer tokenizer = new JsonTokenizer(bais);
            String currentSection = null;
            JsonToken token = tokenizer.next();
            while (token != null) {
                if (token.isSymbol('[')) {
                    currentSection = tokenizer.expect("section name").asString();
                    tokenizer.expectSymbol("", ']');
                    String rest = IOUtils.readString(bais, "\n").trim();
                    if (rest.length() > 0) {
                        throw new ConfigException("Garbage after section: " + Strings.escape(rest));
                    }
                    token = tokenizer.next();
                    continue;
                }

                String key = entryKey(currentSection, token.asString());
                tokenizer.expectSymbol("key/value separator", ':', '=');
                try {
                    config.put(key, parseValue(tokenizer));
                    String rest = IOUtils.readString(bais, "\n").trim();
                    if (rest.length() > 0) {
                        throw new ConfigException("Garbage after value: " + Strings.escape(rest));
                    }
                } catch (JsonException je) {
                    // TOML dates are invalid JSON, but if we pick up a
                    // JsonException which has detected the year-mm-dd
                    // separator char, we can manage to parse this
                    // correctly.
                    if (je.getMessage().endsWith("Wrongly terminated JSON number: -.")) {
                        IOUtils.readString(bais, "\n");
                        // Since the tokenizer is in a bad state (unconsumed
                        // token char that should be ignored) it has to be
                        // reset.
                        tokenizer = new JsonTokenizer(bais);

                        // Hack out the value part of the line.
                        String date = je.getLine().split("[=]", 2)[1].trim();
                        try {
                            LocalDateTime time;
                            if (date.endsWith("Z")) {
                                time = LocalDateTime.parse(date,
                                                           DateTimeFormatter.ISO_INSTANT.withZone(Clock.systemUTC()
                                                                                                       .getZone()));
                            } else {
                                time = LocalDateTime.parse(date,
                                                           DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(Clock.systemUTC()
                                                                                                                .getZone()));
                            }
                            config.put(key,
                                       new Date(time.atZone(Clock.systemUTC().getZone())
                                                    .toInstant()
                                                    .toEpochMilli()));
                        } catch (RuntimeException e) {
                            throw new ConfigException("Value type not recognized: " + Strings.escape(date));
                        }
                    } else {
                        throw new ConfigException(je, je.getMessage());
                    }
                }
                token = tokenizer.next();
            }

            return ImmutableConfig.copyOf(config);
        } catch (JsonException | IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    private Object parseValue(JsonTokenizer tokenizer) throws IOException, JsonException {
        JsonToken token = tokenizer.expect("TOML value");
        if (token.isSymbol('[')) {
            ImmutableList.Builder<Object> list = ImmutableList.builder();
            if (tokenizer.peek("").isSymbol(']')) {
                tokenizer.next();
                return list.build();
            }

            char sep = '[';
            while (sep != ']') {
                list.add(parseValue(tokenizer));
                sep = tokenizer.expectSymbol("list separator", ',', ']');
            }
            return list.build();
        } else if (token.isBoolean()) {
            return token.booleanValue();
        } else if (token.isDouble()) {
            return token.doubleValue();
        } else if (token.isInteger()) {
            return token.longValue();
        } else if (token.isLiteral()) {
            return token.decodeJsonLiteral();
        } else {
            throw new ConfigException("Unknown value token " + Strings.escape(token.asString()));
        }
    }

    private String entryKey(String section, String key) {
        if (section != null) {
            return section + "." + key;
        }
        return key;
    }

    private String stripComment(String line) {
        boolean quoted = false;
        boolean escaped = false;
        int i = 0;
        for (char c : line.toCharArray()) {
            if (quoted) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '\"') {
                    quoted = false;
                }
            } else if (c == '\"') {
                quoted = true;
            } else if (c == '#') {
                return line.substring(0, i);
            }

            ++i;
        }
        return line;
    }
}
