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

import net.morimekta.config.format.ConfigParser;
import net.morimekta.util.FileWatcher;

import java.io.File;

import static net.morimekta.config.util.ConfigUtil.getParserForName;

/**
 * File source for config objects that is updated with help from a file
 * watcher. If you want to have separate watcher that should react to the same
 * file, but trigger <b>after</b> the config is updated, then you need to
 * register that watcher <b>after</b> the supplier is created.
 *
 * <code>
 *     FileWatcher watcher = new FileWatcher();
 *     WatchedFileConfigSupplier supplier = new WatchedFileConfigSupplier(watcher, file);
 *     watcher.addWatcher(this::onConfigUpdate);
 * </code>
 *
 * Most likely you would want to register the watcher after your entire app has
 * been initialized, but that is another topic.
 */
public class WatchedFileConfigSupplier extends FileConfigSupplier {
    public WatchedFileConfigSupplier(FileWatcher watcher, File configFile) {
        this(watcher, configFile, getParserForName(configFile.getName()));
    }

    public WatchedFileConfigSupplier(FileWatcher watcher, File configFile, ConfigParser format) {
        super(configFile, format);
        watcher.addWatcher(configFile.toPath(), file -> reload());
    }
}
