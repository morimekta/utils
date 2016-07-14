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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A re-entrant read-write mutex wrapper.
 */
public class ReentrantReadWriteMutex implements ReadWriteMutex {
    private final Lock readLock;
    private final Lock writeLock;

    public ReentrantReadWriteMutex() {
        ReadWriteLock mutex = new ReentrantReadWriteLock();
        this.readLock = mutex.readLock();
        this.writeLock = mutex.writeLock();
    }

    @Override
    public void lockForReading(Runnable callable) {
        readLock.lock();
        try {
            callable.run();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <V> V lockForReading(Supplier<V> callable) {
        readLock.lock();
        try {
            return callable.get();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void lockForWriting(Runnable callable) {
        writeLock.lock();
        try {
            callable.run();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <V> V lockForWriting(Supplier<V> callable) {
        writeLock.lock();
        try {
            return callable.get();
        } finally {
            writeLock.unlock();
        }
    }
}
