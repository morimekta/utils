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
import net.morimekta.config.impl.ImmutableConfig;
import net.morimekta.config.impl.SimpleConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Format config into properties objects or files.
 */
public class PropertiesConfigParser implements ConfigParser {
    @Override
    public Config parse(InputStream in) {
        try {
            Properties properties = new Properties();
            properties.load(in);
            return parse(properties);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    /**
     * Parse the properties instance into a config.
     *
     * @param properties The properties to parse.
     * @return The config instance.
     */
    public static Config parse(Properties properties) {
        ConfigBuilder config = new SimpleConfig();
        for (Object o : new TreeSet<>(properties.keySet())) {
            String key = o.toString();
            if (isPositional(key)) {
                String entryKey = entryKey(key);
                if (config.containsKey(entryKey)) {
                    config.getCollection(entryKey).add(properties.getProperty(key));
                } else {
                    LinkedList<Object> sequence = new LinkedList<>();
                    sequence.add(properties.getProperty(key));
                    config.putCollection(entryKey, sequence);
                }
            } else {
                config.put(key, properties.getProperty(key));
            }
        }
        return ImmutableConfig.copyOf(config);
    }

    private static boolean isPositional(String key) {
        try {
            String[] parts = key.split("[.]");
            String last = parts[parts.length - 1];
            if (Integer.parseInt(last) >= 0) {
                return true;
            }
        } catch (NumberFormatException e) {
            // Ignore.
        }
        return false;
    }

    private static String entryKey(String positional) {
        return positional.substring(0, positional.lastIndexOf("."));
    }
}
