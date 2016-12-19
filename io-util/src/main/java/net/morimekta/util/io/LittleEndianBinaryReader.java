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
package net.morimekta.util.io;

import java.io.InputStream;

/**
 * IO-Optimized binary reader using little-endian integer encoding.
 */
public class LittleEndianBinaryReader extends BinaryReader {
    public LittleEndianBinaryReader(InputStream in) {
        super(in);
    }

    @Override
    protected int unshift2bytes(int b1, int b2) {
        return (b1 | b2 << 8);
    }

    @Override
    protected int unshift3bytes(int b1, int b2, int b3) {
        return (b1 | b2 << 8 | b3 << 16);
    }

    @Override
    protected int unshift4bytes(int b1, int b2, int b3, int b4) {
        return (b1 | b2 << 8 | b3 << 16 | b4 << 24);
    }

    @Override
    protected long unshift8bytes(long b1, long b2, long b3, long b4, long b5, long b6, long b7, long b8) {
        return (b1 | b2 << 8 | b3 << 16 | b4 << 24 | b5 << 32 | b6 << 40 | b7 << 48 | b8 << 56);
    }
}
