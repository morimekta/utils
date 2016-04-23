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
 * IO-Optimized binary reader.
 */
public class BigEndianBinaryReader extends BinaryReader {
    public BigEndianBinaryReader(InputStream in) {
        super(in);
    }

    @Override
    protected int unshift2bytes(int b1, int b2) {
        return (b2 | b1 << 8);
    }

    @Override
    protected int unshift3bytes(int b1, int b2, int b3) {
        return (b3 | b2 << 8 | b1 << 16);
    }

    @Override
    protected int unshift4bytes(int b1, int b2, int b3, int b4) {
        return (b4 | b3 << 8 | b2 << 16 | b1 << 24);
    }

    @Override
    protected long unshift8bytes(long b1, long b2, long b3, long b4, long b5, long b6, long b7, long b8) {
        return (b8 | b7 << 8 | b6 << 16 | b5 << 24 | b4 << 32 | b3 << 40 | b2 << 48 | b1 << 56);
    }
}
