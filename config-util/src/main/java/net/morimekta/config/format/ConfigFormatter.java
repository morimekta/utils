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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Config formatter interface.
 */
@FunctionalInterface
public interface ConfigFormatter {
    /**
     * Format config and write to output stream.
     *
     * @param config The config to format.
     * @param out The output stream to write to.
     */
    void format(Config config, OutputStream out);

    /**
     * Format the config to string.
     *
     * @param config The config to format.
     * @return The string representation of the config with this formatter.
     */
    default String formatToString(Config config) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        format(config, baos);
        return new String(baos.toByteArray(), UTF_8);
    }
}
