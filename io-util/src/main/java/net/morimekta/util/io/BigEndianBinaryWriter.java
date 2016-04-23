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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Variant of the binary writer that writes all numbers big-endian.
 */
public class BigEndianBinaryWriter extends BinaryWriter {
    public BigEndianBinaryWriter(OutputStream out) {
        super(out);
    }

    @Override
    public int writeShort(short integer) throws IOException {
        out.write(integer >>> 8);
        out.write(integer);
        return 2;
    }

    @Override
    public int writeInt(int integer) throws IOException {
        out.write(integer >>> 24);
        out.write(integer >>> 16);
        out.write(integer >>> 8);
        out.write(integer);
        return 4;
    }

    @Override
    public int writeLong(long integer) throws IOException {
        out.write((int) (integer >>> 56));
        out.write((int) (integer >>> 48));
        out.write((int) (integer >>> 40));
        out.write((int) (integer >>> 32));
        out.write((int) (integer >>> 24));
        out.write((int) (integer >>> 16));
        out.write((int) (integer >>> 8));
        out.write((int) (integer));
        return 8;
    }

    @Override
    public int writeUInt16(int number) throws IOException {
        out.write(number >>> 8);
        out.write(number);
        return 2;
    }

    @Override
    public int writeUInt24(int number) throws IOException {
        out.write(number >>> 16);
        out.write(number >>> 8);
        out.write(number);
        return 3;
    }

    @Override
    public int writeUInt32(int number) throws IOException {
        out.write(number >>> 24);
        out.write(number >>> 16);
        out.write(number >>> 8);
        out.write(number);
        return 4;
    }
}
