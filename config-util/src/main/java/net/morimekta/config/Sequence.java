package net.morimekta.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;

import static net.morimekta.config.Value.fromObject;
import static net.morimekta.config.Value.fromValue;

/**
 * Immutable sequence of values of the same type. And the sequence is statically
 * annotated with the type of the values within the sequence.
 */
public class Sequence extends ArrayList<Object> {
    /**
     * Create an immutable sequence.
     * @param type The type of elements. Note that a sequence cannot contain
     *             sequences.
     */
    public Sequence(Value.Type type) {
        super();
        this.type = type;
    }

    /**
     * Create an immutable sequence.
     * @param type The type of elements. Note that a sequence cannot contain
     *             sequences.
     * @param content The contained collection.
     */
    @SuppressWarnings("unchecked")
    public Sequence(Value.Type type, Collection<?> content) {
        super();
        this.type = type;
        addAll(content);
    }

    public Sequence(Value.Type type, Object... values) {
        super();
        this.type = type;
        addAll(values);
    }

    public Value.Type type() {
        return type;
    }

    public boolean getBoolean(int i) throws ConfigException {
        return getValue(i).asBoolean();
    }
    
    public boolean[] asBooleanArray() throws ConfigException {
        boolean[] out = new boolean[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getBoolean(i);
        }
        return out;
    }

    public int getInteger(int i) throws ConfigException {
        return getValue(i).asInteger();
    }
    
    public int[] asIntegerArray() throws ConfigException {
        int[] out = new int[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getInteger(i);
        }
        return out;
    }

    public long getLong(int i) throws ConfigException {
        return getValue(i).asLong();
    }

    public long[] asLongArray() throws ConfigException {
        long[] out = new long[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getLong(i);
        }
        return out;
    }

    public double getDouble(int i) throws ConfigException {
        return getValue(i).asDouble();
    }

    public double[] asDoubleArray() throws ConfigException {
        double[] out = new double[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getDouble(i);
        }
        return out;
    }

    public String getString(int i) throws ConfigException {
        return getValue(i).asString();
    }

    public String[] asStringArray() throws ConfigException {
        String[] out = new String[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getString(i);
        }
        return out;
    }

    public Sequence getSequence(int i) throws ConfigException {
        return getValue(i).asSequence();
    }

    public Sequence[] asSequenceArray() throws ConfigException {
        Sequence[] out = new Sequence[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getSequence(i);
        }
        return out;
    }

    public Config getConfig(int i) throws ConfigException {
        return getValue(i).asConfig();
    }

    public Config[] asConfigArray() throws ConfigException {
        Config[] out = new Config[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getConfig(i);
        }
        return out;
    }

    public Value getValue(int i) {
        return new Value(type, get(i));
    }

    public Value[] asValueArray() {
        Value[] out = new Value[size()];
        for (int i = 0; i < size(); ++i) {
            out[i] = getValue(i);
        }
        return out;
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
        if (other.type() != type || other.size() != size()) {
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
        for (Object o : this) {
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

    public static Collector<Object, Sequence, Sequence> collect(final Value.Type type) {
        return Collector.of(() -> new Sequence(type),
                            Sequence::add,
                            (a, b) -> {
                                a.addAll(b);
                                return a;
                            },
                            i -> i);
    }

    public static Sequence create(String... values) {
        Sequence builder = new Sequence(Value.Type.STRING);
        for (String val : values) {
            builder.add(val);
        }
        return builder;
    }

    public static Sequence create(int... values) {
        Sequence builder = new Sequence(Value.Type.NUMBER);
        for (int val : values) {
            builder.add(val);
        }
        return builder;
    }

    public static Sequence create(long... values) {
        Sequence builder = new Sequence(Value.Type.NUMBER);
        for (long val : values) {
            builder.add(val);
        }
        return builder;
    }

    public static Sequence create(double... values) {
        Sequence builder = new Sequence(Value.Type.NUMBER);
        for (double val : values) {
            builder.add(val);
        }
        return builder;
    }

    public static Sequence create(boolean... values) {
        Sequence builder = new Sequence(Value.Type.BOOLEAN);
        for (boolean val : values) {
            builder.add(val);
        }
        return builder;
    }

    public Object set(int i, Object elem) {
        return super.set(i, fromObject(type, elem));
    }

    public boolean add(Object elem) {
        return super.add(fromObject(type, elem));
    }

    public void add(int pos, Object elem) {
        super.add(pos, fromObject(type, elem));
    }

    public Sequence setValue(int i, Value value) {
        super.set(i, fromValue(type, value));
        return this;
    }

    public Sequence addValue(Value value) {
        super.add(fromValue(type, value));
        return this;
    }

    public Sequence addValue(int i, Value value) {
        super.add(i, fromValue(type, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    public Sequence replaceValue(int i, Value value) {
        set(i, fromValue(type, value));
        return this;
    }

    public Sequence removeLast() {
        if (size() == 0) {
            throw new IllegalStateException("Unable to remove last of empty sequence");
        }
        remove(size() - 1);
        return this;
    }

    public boolean addAll(Collection<?> coll) {
        boolean ret = false;
        for (Object o : coll) {
            ret |= add(o);
        }
        return ret;
    }

    public boolean addAll(int pos, Collection<?> coll) {
        boolean ret = false;
        for (Object o : coll) {
            add(pos++, o);
            ret = true;
        }
        return ret;
    }

    public boolean addAll(Object... coll) {
        boolean ret = false;
        for (Object o : coll) {
            ret |= add(o);
        }
        return ret;
    }

    // --- INTERNAL ---

    private final Value.Type type;
}
