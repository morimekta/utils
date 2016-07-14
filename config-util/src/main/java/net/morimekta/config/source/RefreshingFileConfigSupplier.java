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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Clock;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class RefreshingFileConfigSupplier implements Supplier<Config> {
    private static final long UPDATE_CHECK_INTERVAL_MS = 1000;

    private final File         configFile;
    private final ConfigParser parser;
    private final Clock        clock;

    private long lastCheckTimestamp;
    private long lastModified;

    private Config config;

    public RefreshingFileConfigSupplier(File configFile, ConfigParser format) {
        this(configFile, format, Clock.systemUTC());
    }

    public RefreshingFileConfigSupplier(File configFile, ConfigParser format, Clock clock) {
        this.clock = clock;
        this.configFile = configFile;
        this.parser = format;
    }

    @Override
    public synchronized Config get() {
        try {
            long now = clock.millis();
            if (config == null) {
                loadInternal();
                this.lastModified = Files.getLastModifiedTime(configFile.toPath())
                                         .toMillis();
                this.lastCheckTimestamp = now;
            } else if (now > (lastCheckTimestamp + UPDATE_CHECK_INTERVAL_MS)) {
                long modified = Files.getLastModifiedTime(configFile.toPath())
                                     .toMillis();
                if (modified > this.lastModified) {
                    loadInternal();
                    this.lastModified = modified;
                }
                this.lastCheckTimestamp = now;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return config;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("file", configFile)
                          .toString();
    }

    private void loadInternal() throws IOException, ConfigException {
        try (FileInputStream fis = new FileInputStream(configFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            this.config = parser.parse(bis);
        }
    }
}
