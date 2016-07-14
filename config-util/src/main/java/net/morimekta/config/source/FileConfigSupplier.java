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
package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigParser;

import com.google.common.base.MoreObjects;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class FileConfigSupplier implements Supplier<Config> {
    private final File         configFile;
    private final ConfigParser parser;
    private final AtomicReference<Config> config;

    public FileConfigSupplier(File configFile, ConfigParser format) {
        this.configFile = configFile;
        this.parser = format;
        this.config = new AtomicReference<>(loadInternal());
    }

    @Override
    public Config get() {
        return config.get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("file", configFile)
                          .toString();
    }

    /**
     * Reload the config value.
     */
    public void reload() {
        this.config.set(loadInternal());
    }

    private Config loadInternal() {
        try (FileInputStream fis = new FileInputStream(configFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            return parser.parse(bis);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }
}
