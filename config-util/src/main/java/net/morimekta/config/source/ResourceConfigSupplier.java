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

import com.google.common.base.MoreObjects;
import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigParser;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import static net.morimekta.config.util.ConfigUtil.getParserForName;

/**
 * File source for config objects.
 */
public class ResourceConfigSupplier implements Supplier<Config> {
    private final Config config;
    private final String resource;

    public ResourceConfigSupplier(@Nonnull String resource) {
        this(resource, getParserForName(resource));
    }

    public ResourceConfigSupplier(@Nonnull String resource,
                                  @Nonnull ConfigParser parser) {
        this(resource, parser, getResourceAsStream(resource));
    }

    public ResourceConfigSupplier(@Nonnull String resource,
                                  @Nonnull ConfigParser parser,
                                  @WillClose InputStream in) {
        this.resource = resource;
        try (InputStream ignore = in){
            this.config = parser.parse(in);
        } catch (IOException e) {
            throw new ConfigException("Unable to close stream", e);
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

    private static InputStream getResourceAsStream(String resource) {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
        if (in == null) {
            in = ResourceConfigSupplier.class.getResourceAsStream(resource);
        }
        if (in == null) {
            throw new ConfigException("No such config resource: " + resource);
        }
        return in;
    }
}
