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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class ResourceConfigSupplier implements Supplier<Config> {
    private final Config config;
    private final String resource;

    public ResourceConfigSupplier(String resource, ConfigParser parser) {
        this.resource = resource;
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            this.config = parser.parse(in);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("resource", resource)
                          .toString();
    }

    @Override
    public Config get() {
        return config;
    }
}
