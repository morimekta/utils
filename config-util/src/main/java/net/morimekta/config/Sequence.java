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

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A sequence of values with getType safe getter methods.
 */
public abstract class Sequence implements Iterable<Object> {
    /**
     * Get the types of values stored in the sequence. A sequence should
     * only contain one getType of values to be getType consistent.
     *
     * @return The value item getType.
     */
    public abstract Value.Type getType();

    /**
     * The number of entries in the sequence.
     *
     * @return Sequence size.
     */
    public abstract int size();

    /**
     * Get an item from the sequence.
     *
     * @param i The item index.
     * @param <T> The item type.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    public abstract <T> T get(int i);

    /**
     * Get value at index as a boolean.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    public boolean getBoolean(int i) {
        return getValue(i).asBoolean();
    }

    /**
     * Get the sequence as a boolean array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public boolean[] asBooleanArray() {
        boolean[] out = new boolean[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getBoolean(i);
        }
        return out;
    }

    /**
     * Get value at index as a integer.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public int getInteger(int i) {
        return getValue(i).asInteger();
    }

    /**
     * Get the sequence as an integer array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public int[] asIntegerArray() {
        int[] out = new int[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getInteger(i);
        }
        return out;
    }

    /**
     * Get value at index as a long.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public long getLong(int i) {
        return getValue(i).asLong();
    }

    /**
     * Get the sequence as a long array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public long[] asLongArray() {
        long[] out = new long[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getLong(i);
        }
        return out;
    }

    /**
     * Get value at index as a double.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public double getDouble(int i) {
        return getValue(i).asDouble();
    }

    /**
     * Get the sequence as a double array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public double[] asDoubleArray() {
        double[] out = new double[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getDouble(i);
        }
        return out;
    }

    /**
     * Get value at index as a string.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public String getString(int i) {
        return getValue(i).asString();
    }

    /**
     * Get the sequence as a string array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public String[] asStringArray() {
        String[] out = new String[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getString(i);
        }
        return out;
    }

    /**
     * Get value at index as a sequence.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public Sequence getSequence(int i) {
        return getValue(i).asSequence();
    }

    /**
     * Get the sequence as a sequence array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public Sequence[] asSequenceArray() {
        Sequence[] out = new Sequence[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getSequence(i);
        }
        return out;
    }

    /**
     * Get value at index as a config.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public Config getConfig(int i) {
        return getValue(i).asConfig();
    }

    /**
     * Get the sequence as a config array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    public Config[] asConfigArray() {
        Config[] out = new Config[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getConfig(i);
        }
        return out;
    }

    /**
     * Get value at index.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    public Value getValue(int i) {
        return new ImmutableValue(getType(), get(i));
    }

    /**
     * Get the sequence as a value array.
     *
     * @return The boolean array.
     */
    public Value[] asValueArray() {
        Value[] out = new Value[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getValue(i);
        }
        return out;
    }

    /**
     * Get the sequence as a stream.
     *
     * @return The stream.
     */
    public Stream<Object> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<Object> iterator() {
        return stream().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Sequence)) {
            return false;
        }
        Sequence other = (Sequence) o;
        if (other.getType() != getType() || other.size() != size()) {
            return false;
        }

        if (getType() == Value.Type.NUMBER) {
            for (int i = 0; i < size(); ++i) {
                if (((Number) get(i)).doubleValue() != ((Number) other.get(i)).doubleValue()) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < size(); ++i) {
                if (!get(i).equals(other.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append('(')
               .append(getType().toString().toLowerCase())
               .append(':')
               .append('[');

        boolean first = true;
        for (Value o : asValueArray()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(o.getValue().toString());
        }

        builder.append(']')
               .append(')');
        return builder.toString();
    }

    /**
     * Check if the index is valid got item lookup.
     * @param i The index.
     * @throws IndexOutOfBoundsException If the index is not in range.
     */
    void checkRange(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i >= size()) {
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size());
        }
    }
}
