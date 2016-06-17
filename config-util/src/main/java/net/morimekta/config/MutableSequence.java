package net.morimekta.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.morimekta.config.Value.fromObject;
import static net.morimekta.config.Value.fromValue;

/**
 * A mutable sequence of values of the same getType. And the sequence is statically
 * annotated with the getType of the values within the sequence.
 */
public class MutableSequence implements Sequence {
    /**
     * Create an immutable sequence.
     * @param type The getType of elements. Note that a sequence cannot contain
     *             sequences.
     */
    public MutableSequence(Value.Type type) {
        this.type = type;
        this.arr = new ArrayList<>();
    }

    /**
     * Create an immutable sequence.
     * @param type The getType of elements. Note that a sequence cannot contain
     *             sequences.
     * @param content The contained collection.
     */
    public MutableSequence(Value.Type type, Iterable<?> content) {
        this(type);
        addAll(content);
    }

    public MutableSequence(Value.Type type, Object... values) {
        this(type);
        addAll(values);
    }

    public Value.Type getType() {
        return type;
    }

    @Override
    public int size() {
        return arr.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(int i) {
        return (T) arr.get(i);
    }

    @Override
    public boolean getBoolean(int i) throws ConfigException {
        return getValue(i).asBoolean();
    }

    @Override
    public boolean[] asBooleanArray() throws ConfigException {
        boolean[] out = new boolean[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getBoolean(i);
        }
        return out;
    }

    @Override
    public int getInteger(int i) throws ConfigException {
        return getValue(i).asInteger();
    }

    @Override
    public int[] asIntegerArray() throws ConfigException {
        int[] out = new int[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getInteger(i);
        }
        return out;
    }

    @Override
    public long getLong(int i) throws ConfigException {
        return getValue(i).asLong();
    }

    @Override
    public long[] asLongArray() throws ConfigException {
        long[] out = new long[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getLong(i);
        }
        return out;
    }

    @Override
    public double getDouble(int i) throws ConfigException {
        return getValue(i).asDouble();
    }

    @Override
    public double[] asDoubleArray() throws ConfigException {
        double[] out = new double[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getDouble(i);
        }
        return out;
    }

    @Override
    public String getString(int i) throws ConfigException {
        return getValue(i).asString();
    }

    @Override
    public String[] asStringArray() throws ConfigException {
        String[] out = new String[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getString(i);
        }
        return out;
    }

    @Override
    public Sequence getSequence(int i) throws ConfigException {
        return getValue(i).asSequence();
    }

    @Override
    public Sequence[] asSequenceArray() throws ConfigException {
        Sequence[] out = new Sequence[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getSequence(i);
        }
        return out;
    }

    @Override
    public Config getConfig(int i) throws ConfigException {
        return getValue(i).asConfig();
    }

    @Override
    public Config[] asConfigArray() throws ConfigException {
        Config[] out = new Config[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getConfig(i);
        }
        return out;
    }

    @Override
    public Value getValue(int i) {
        return new ImmutableValue(type, get(i));
    }

    @Override
    public Value[] asValueArray() {
        Value[] out = new Value[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getValue(i);
        }
        return out;
    }

    @Override
    public Iterator<Object> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Object> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Spliterators.spliterator(arr, Spliterator.NONNULL |
                                             Spliterator.ORDERED |
                                             Spliterator.SIZED);
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
        if (other.getType() != type || other.size() != size()) {
            return false;
        }

        if (type == Value.Type.NUMBER) {
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
               .append(type.toString().toLowerCase())
               .append(':')
               .append('[');

        boolean first = true;
        for (Object o : this.arr) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(o.toString());
        }

        builder.append(']')
               .append(')');
        return builder.toString();
    }

    /**
     * Collector for collecting into a mutable sequence.
     *
     * @param type The value tyoe to collect as.
     * @return The sequence collector.
     */
    public static Collector<Object, MutableSequence, MutableSequence> collect(final Value.Type type) {
        return Collector.of(() -> new MutableSequence(type),
                            MutableSequence::add,
                            (a, b) -> {
                                a.addAll(b);
                                return a;
                            },
                            i -> i);
    }

    /**
     * Create a string sequence with given values.
     *
     * @param values The values to add to the sequence.
     * @return The mutable sequence.
     */
    public static MutableSequence create(String... values) {
        MutableSequence builder = new MutableSequence(Value.Type.STRING);
        for (String val : values) {
            builder.add(val);
        }
        return builder;
    }

    /**
     * Create an integer sequence with given values.
     *
     * @param values The values to add to the sequence.
     * @return The mutable sequence.
     */
    public static MutableSequence create(int... values) {
        MutableSequence builder = new MutableSequence(Value.Type.NUMBER);
        for (int val : values) {
            builder.add(val);
        }
        return builder;
    }

    /**
     * Create a long sequence with given values.
     *
     * @param values The values to add to the sequence.
     * @return The mutable sequence.
     */
    public static MutableSequence create(long... values) {
        MutableSequence builder = new MutableSequence(Value.Type.NUMBER);
        for (long val : values) {
            builder.add(val);
        }
        return builder;
    }

    /**
     * Create a double sequence with given values.
     *
     * @param values The values to add to the sequence.
     * @return The mutable sequence.
     */
    public static MutableSequence create(double... values) {
        MutableSequence builder = new MutableSequence(Value.Type.NUMBER);
        for (double val : values) {
            builder.add(val);
        }
        return builder;
    }

    /**
     * Create a boolean sequence with given values.
     *
     * @param values The values to add to the sequence.
     * @return The mutable sequence.
     */
    public static MutableSequence create(boolean... values) {
        MutableSequence builder = new MutableSequence(Value.Type.BOOLEAN);
        for (boolean val : values) {
            builder.add(val);
        }
        return builder;
    }

    /**
     * Set the value at the given index.
     * @param i The index to set.
     * @param elem The value to set.
     * @return The sequence.
     */
    public MutableSequence set(int i, Object elem) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i >= arr.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    arr.size());
        }
        arr.set(i, fromObject(type, elem));
        return this;
    }

    public MutableSequence add(Object elem) {
        arr.add(fromObject(type, elem));
        return this;
    }

    public MutableSequence add(int i, Object elem) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i > arr.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    arr.size());
        }
        arr.add(i, fromObject(type, elem));
        return this;
    }

    public MutableSequence setValue(int i, Value value) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i >= arr.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    arr.size());
        }
        arr.set(i, fromValue(type, value));
        return this;
    }

    public MutableSequence addValue(Value value) {
        arr.add(fromValue(type, value));
        return this;
    }

    public MutableSequence addValue(int i, Value value) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i > arr.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    arr.size());
        }
        arr.add(i, fromValue(type, value));
        return this;
    }

    public MutableSequence remove(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i >= arr.size()) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    arr.size());
        }
        arr.remove(i);
        return this;
    }

    public MutableSequence removeLast() {
        if (size() == 0) {
            throw new IllegalStateException("Unable to remove last of empty sequence");
        }
        arr.remove(size() - 1);
        return this;
    }

    public MutableSequence addAll(Iterable<?> iter) {
        iter.forEach(this::add);
        return this;
    }

    public MutableSequence addAll(int pos, Iterable<?> iter) {
        for (Object o : iter) {
            add(pos++, o);
        }
        return this;
    }

    public MutableSequence addAll(Object... items) {
        for (Object o : items) {
            add(o);
        }
        return this;
    }

    // --- INTERNAL ---

    private final Value.Type type;
    private final ArrayList<Object> arr;
}
