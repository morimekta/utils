package net.morimekta.config;

import java.util.stream.Stream;

/**
 * A sequence of values with type safe getter methods.
 */
public interface Sequence extends Iterable<Object> {
    /**
     * Get the types of values stored in the sequence. A sequence should
     * only contain one type of values to be type consistent.
     *
     * @return The value item type.
     */
    Value.Type type();

    /**
     * The number of entries in the sequence.
     *
     * @return Sequence size.
     */
    int size();

    /**
     * Get an item from the sequence.
     *
     * @param i The item index.
     * @param <T> The item class.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    <T> T get(int i);

    /**
     * Get value at index as a boolean.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    boolean getBoolean(int i);

    /**
     * Get the sequence as a boolean array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    boolean[] asBooleanArray();

    /**
     * Get value at index as a integer.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    int getInteger(int i);

    /**
     * Get the sequence as an integer array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    int[] asIntegerArray();

    /**
     * Get value at index as a long.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    long getLong(int i);

    /**
     * Get the sequence as a long array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    long[] asLongArray();

    /**
     * Get value at index as a double.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    double getDouble(int i);

    /**
     * Get the sequence as a double array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    double[] asDoubleArray();

    /**
     * Get value at index as a string.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    String getString(int i);

    /**
     * Get the sequence as a string array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    String[] asStringArray();

    /**
     * Get value at index as a sequence.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    Sequence getSequence(int i);

    /**
     * Get the sequence as a sequence array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    Sequence[] asSequenceArray();

    /**
     * Get value at index as a config.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    Config getConfig(int i);

    /**
     * Get the sequence as a config array.
     *
     * @return The boolean array.
     * @throws IncompatibleValueException If any value could not be converted.
     */
    Config[] asConfigArray();

    /**
     * Get value at index.
     * @param i The item index.
     * @return The item.
     * @throws IndexOutOfBoundsException If index is outside the sequence size.
     */
    Value getValue(int i);

    /**
     * Get the sequence as a value array.
     *
     * @return The boolean array.
     */
    Value[] asValueArray();

    /**
     * Get the sequence as a stream.
     *
     * @return The stream.
     */
    Stream<Object> stream();
}
