package net.morimekta.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Immutable sequence of values of the same getType. And the sequence is statically
 * annotated with the getType of the values within the sequence.
 */
public class ImmutableSequence implements Sequence {
    private static final int characteristics =
            Spliterator.ORDERED |
            Spliterator.SIZED |
            Spliterator.IMMUTABLE |
            Spliterator.SUBSIZED;

    /**
     * Create an immutable sequence.
     * @param type The getType of elements. Note that a sequence cannot contain
     *             sequences.
     * @param content The contained collection.
     */
    @SuppressWarnings("unchecked")
    private ImmutableSequence(Value.Type type, Collection<?> content) {
        this.type = type;
        this.seq = new Object[content.size()];

        int i = 0;
        for (Object elem : content) {
            if (i < seq.length) {
                seq[i] = elem;
                ++i;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public Value.Type getType() {
        return type;
    }

    @Override
    public int size() {
        return seq.length;
    }

    @Override
    public Object get(int i) {
        checkRange(i);
        return seq[i];
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
        checkRange(i);
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
        for (Object o : seq) {
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

    @Override
    public Iterator<Object> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Stream<Object> stream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Spliterators.spliterator(seq, 0, seq.length, characteristics);
    }

    public static Collector<Object, Builder, ImmutableSequence> collect(final Value.Type type) {
        return Collector.of(() -> new Builder(type),
                            Builder::add,
                            (a, b) -> {
                                a.addAll(b.seq);
                                return a;
                            },
                            Builder::build);
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder(Value.Type type) {
        return new Builder(type);
    }

    public static Sequence create(String... values) {
        Builder builder = new Builder(Value.Type.STRING);
        for (String val : values) {
            builder.add(val);
        }
        return builder.build();
    }

    public static Sequence create(int... values) {
        Builder builder = new Builder(Value.Type.NUMBER);
        for (int val : values) {
            builder.add(val);
        }
        return builder.build();
    }

    public static Sequence create(long... values) {
        Builder builder = new Builder(Value.Type.NUMBER);
        for (long val : values) {
            builder.add(val);
        }
        return builder.build();
    }

    public static Sequence create(double... values) {
        Builder builder = new Builder(Value.Type.NUMBER);
        for (double val : values) {
            builder.add(val);
        }
        return builder.build();
    }

    public static Sequence create(boolean... values) {
        Builder builder = new Builder(Value.Type.BOOLEAN);
        for (boolean val : values) {
            builder.add(val);
        }
        return builder.build();
    }

    public static Sequence copyOf(Sequence sequence) {
        switch (sequence.getType()) {
            case SEQUENCE: {
                ImmutableSequence.Builder builder = ImmutableSequence.builder(Value.Type.SEQUENCE);
                for (Sequence subSequence : sequence.asSequenceArray()) {
                    builder.add(copyOf(subSequence));
                }
                return builder.build();
            }
            case CONFIG: {
                ImmutableSequence.Builder builder = ImmutableSequence.builder(Value.Type.CONFIG);
                for (Config config : sequence.asConfigArray()) {
                    builder.add(ImmutableConfig.copyOf(config));
                }
                return builder.build();
            }
            default:
                if (sequence instanceof ImmutableSequence) {
                    return sequence;
                }
                return ImmutableSequence.builder(sequence.getType())
                                        .addAll(sequence.stream()
                                                        .collect(Collectors.toList()))
                                        .build();
        }
    }

    private void checkRange(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        if (i >= seq.length) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + ", Size: " +
                    seq.length);
        }
    }

    public static class Builder {
        private final Value.Type        type;
        private final ArrayList<Object> seq;

        public Builder(Value.Type type) {
            this.type = type;
            this.seq = new ArrayList<>();
        }

        public Builder(Sequence base) {
            this(base.getType());
            base.stream().forEachOrdered(this::add);
        }

        public int size() {
            return seq.size();
        }

        public Value.Type getType() {
            return type;
        }

        private Object immutable(Object o) {
            if (o instanceof Value) {
                throw new IllegalArgumentException("Value passed as instance.");
            }

            switch (type) {
                case CONFIG:
                    if (!(o instanceof Config)) {
                        throw new ConfigException("Not a config type: %s", o.getClass().getSimpleName());
                    }
                    return ImmutableConfig.copyOf((Config) o);
                case SEQUENCE:
                    if (!(o instanceof Sequence)) {
                        throw new ConfigException("Not a sequence type: %s", o.getClass().getSimpleName());
                    }
                    return ImmutableSequence.copyOf((Sequence) o);
                default:
                    // All other base values are immutable.
                    return Value.fromObject(type, o);
            }
        }

        public Builder add(Object elem) {
            seq.add(immutable(elem));
            return this;
        }

        public Builder addValue(Value value) {
            seq.add(immutable(value.getValue()));
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(int i) {
            checkRange(i);
            return (T) seq.get(i);
        }

        public Value getValue(int i) {
            checkRange(i);
            return new ImmutableValue(type, seq.get(i));
        }

        public Builder add(int i, Object elem) {
            checkInsertRange(i);
            seq.add(i, immutable(elem));
            return this;
        }

        public Builder addValue(int i, Value value) {
            checkInsertRange(i);
            seq.add(i, immutable(value));
            return this;
        }

        public Builder set(int i, Object value) {
            checkRange(i);
            seq.set(i, immutable(value));
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder setValue(int i, Value value) {
            checkRange(i);
            seq.set(i, immutable(value.getValue()));
            return this;
        }

        public Builder remove(int i) {
            checkRange(i);
            seq.remove(i);
            return this;
        }

        public Builder removeLast() {
            if (seq.size() == 0) {
                throw new IllegalStateException("Unable to remove last of empty sequence");
            }
            seq.remove(seq.size() - 1);
            return this;
        }

        public Builder addAll(Iterable<?> iter) {
            iter.forEach(this::add);
            return this;
        }

        public Builder addAll(int i, Iterable<?> iter) {
            checkInsertRange(i);
            for (Object o : iter) {
                add(i++, o);
            }
            return this;
        }

        public Builder addAll(Object... items) {
            for (Object o : items) {
                add(o);
            }
            return this;
        }

        public ImmutableSequence build() {
            return new ImmutableSequence(type, seq);
        }

        private void checkRange(int i) {
            if (i < 0) {
                throw new IndexOutOfBoundsException(Integer.toString(i));
            }
            if (i >= seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Index: " + i + ", Size: " +
                        seq.size());
            }
        }

        private void checkInsertRange(int i) {
            if (i < 0) {
                throw new IndexOutOfBoundsException(Integer.toString(i));
            }
            if (i > seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Index: " + i + ", Size: " +
                        seq.size());
            }
        }
    }

    // --- INTERNAL ---

    private final Object[]   seq;
    private final Value.Type type;
}
