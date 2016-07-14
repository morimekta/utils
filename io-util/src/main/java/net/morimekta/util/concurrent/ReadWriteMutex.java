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
package net.morimekta.util.concurrent;

import java.util.function.Supplier;

/**
 * Interface for wrapping a read-write muted into java-8 functional interface.
 */
public interface ReadWriteMutex {
    /**
     * Lock the config for a read operation that must be read atomic.
     *
     * @param callable The enclosed callable to be run inside read lock.
     * @param <V> The read return value.
     * @return The supplied value.
     */
    <V> V lockForReading(Supplier<V> callable);

    /**
     * Lock the config for a write operation that must be write atomic, and
     * could interfere with read operations.
     *
     * @param callable The callable operation.
     */
    void lockForWriting(Runnable callable);
}
