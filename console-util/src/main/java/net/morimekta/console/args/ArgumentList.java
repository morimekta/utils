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
 * List of arguments with various tools.
 */
public class ArgumentList {
    private final String[] args;
    private int            offset;

    public ArgumentList(ArgumentList copy) {
        this.args = copy.args;
        this.offset = copy.offset;
    }

    public ArgumentList(String[] args) {
        this.args = args;
        this.offset = 0;
    }

    public int remaining() {
        return args.length - offset;
    }

    public String get(int i) {
        if (i >= (args.length - offset)) {
            throw new IllegalArgumentException("Index: " + i + ", Offset: " + offset + ", Size: " + args.length);
        }
        return args[offset + i];
    }

    public void consume(int offset) {
        this.offset += offset;
    }
}
