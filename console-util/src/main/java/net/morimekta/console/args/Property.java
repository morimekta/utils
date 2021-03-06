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
package net.morimekta.console.args;

/**
 * A property is an option where the value is a key-value pair, and applies
 * the key value onto a putter.
 */
public class Property extends BaseOption {
    /**
     * Basic interface for putting value onto a map, properties or config.
     */
    @FunctionalInterface
    public interface Putter {
        /**
         * Put the property into place.
         *
         * @param key The property key.
         * @param value The property value.
         */
        void put(String key, String value);
    }

    private final Putter properties;
    private final String metaKey;

    /**
     * Create a property argument with default key and value names.
     *
     * @param shortName The short name character.
     * @param usage The usage string.
     * @param properties The properties instance to populate.
     */
    public Property(char shortName, String usage, Putter properties) {
        this(shortName, null, null, usage, properties, false);
    }

    /**
     * Create a property argument with default key and value names.
     *
     * @param name The long option name.
     * @param shortName The short name character.
     * @param usage The usage string.
     * @param properties The properties instance to populate.
     */
    public Property(String name, char shortName, String usage, Putter properties) {
        this(name, shortName, null, null, usage, properties, false);
    }

    /**
     * Create a property argument.
     *
     * @param shortName The short name character.
     * @param metaKey The meta key name.
     * @param metaVar The meta value name.
     * @param usage The usage string.
     * @param properties The properties instance to populate.
     */
    public Property(char shortName, String metaKey, String metaVar, String usage, Putter properties) {
        this(shortName, metaKey, metaVar, usage, properties, false);
    }

    /**
     * Create a property argument.
     *
     * @param shortName The short name character.
     * @param metaKey The meta key name.
     * @param metaVar The meta value name.
     * @param usage The usage string.
     * @param properties The properties instance to populate.
     * @param hidden If the property argument should be hidden.
     */
    public Property(char shortName, String metaKey, String metaVar, String usage, Putter properties, boolean hidden) {
        this(null, shortName, metaKey, metaVar, usage, properties, hidden);
    }

    /**
     * Create a property argument.
     *
     * @param name The long option name.
     * @param shortName The short name character.
     * @param metaKey The meta key name.
     * @param metaVar The meta value name.
     * @param usage The usage string.
     * @param properties The properties instance to populate.
     * @param hidden If the property argument should be hidden.
     */
    public Property(String name, char shortName, String metaKey, String metaVar, String usage, Putter properties, boolean hidden) {
        super(name,   // long-option name
              new String(new char[]{shortName}),
              metaVar == null ? "val" : metaVar,
              usage,
              null,   // default value
              true,   // repeated
              false,  // required
              hidden);

        this.metaKey = metaKey == null ? "key" : metaKey;
        this.properties = properties;
    }

    /**
     * @return The meta key name.
     */
    public String getMetaKey() {
        return metaKey;
    }

    @Override
    public String getSingleLineUsage() {
        return "[-" +
               getShortNames() +
               metaKey +
               '=' +
               getMetaVar() +
               " ...]";
    }

    @Override
    public String getPrefix() {
        StringBuilder sb = new StringBuilder();

        sb.append('-')
          .append(getShortNames())
          .append(metaKey)
          .append('=')
          .append(getMetaVar());

        return sb.toString();
    }

    @Override
    public void validate() throws ArgumentException {
    }

    @Override
    public int applyShort(String opts, ArgumentList args) {
        if (opts.length() == 1) {
            return apply(args);
        }

        String[] parts = opts.substring(1).split("[=]", 2);
        if (parts.length != 2) {
            throw new ArgumentException("No key value sep for properties on " +
                                        nameOrShort() + ": \"-" + opts + "\"");
        }
        if (parts[0].length() == 0) {
            throw new ArgumentException("Empty property key on " +
                                        nameOrShort() + ": \"-" + opts + "\"");
        }
        properties.put(parts[0], parts[1]);
        return 1;
    }

    @Override
    public int apply(ArgumentList args) {
        if (args.remaining() < 2) {
            throw new ArgumentException("No value for " + nameOrShort());
        }
        String[] parts = args.get(1).split("[=]", 2);
        if (parts.length != 2) {
            throw new ArgumentException("No key value sep for properties on " +
                                        nameOrShort() + ": \"" + args.get(1) + "\"");
        }
        if (parts[0].length() == 0) {
            throw new ArgumentException("Empty property key on " +
                                        nameOrShort() + ": \"" + args.get(1) + "\"");
        }
        properties.put(parts[0], parts[1]);
        return 2;
    }
}
