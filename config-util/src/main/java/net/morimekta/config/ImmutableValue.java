/*
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
package net.morimekta.config;

/**
 * Immutable value that can only contain the true config types.
 */
public final class ImmutableValue extends Value {
    private final Type   type;
    private final Object value;

    public ImmutableValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Value.Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public static ImmutableValue copyOf(Value value) {
        if (value == null) {
            throw new ConfigException("Cannot copy null");
        }
        if (value instanceof ImmutableValue) {
            return (ImmutableValue) value;
        }
        return new ImmutableValue(value.getType(), value.getValue());
    }

    public static ImmutableValue create(boolean value) {
        return new ImmutableValue(Type.BOOLEAN, value);
    }

    public static ImmutableValue create(int value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(long value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(double value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(String value) {
        return new ImmutableValue(Type.STRING, value);
    }

    public static ImmutableValue create(Config value) {
        return new ImmutableValue(Type.CONFIG, value);
    }

    public static ImmutableValue create(Sequence value) {
        return new ImmutableValue(Type.SEQUENCE, value);
    }
}
