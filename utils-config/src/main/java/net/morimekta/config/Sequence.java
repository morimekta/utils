package net.morimekta.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Immutable sequence of values of the same type. And the sequence is statically
 * annotated with the type of the values within the sequence.
 */
public class Sequence implements Iterable {
    private static final int characteristics =
            Spliterator.ORDERED |
            Spliterator.SIZED |
            Spliterator.IMMUTABLE |
            Spliterator.SUBSIZED;

    /**
     * Create an immutable sequence.
     * @param type The type of elements. Note that a sequence cannot contain
     *             sequences.
     * @param content The contained collection.
     */
    @SuppressWarnings("unchecked")
    private Sequence(Value.Type type, Collection<?> content) {
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

    public Value.Type type() {
        return type;
    }

    public int size() {
        return seq.length;
    }

    public Object get(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Invalid index " + i);
        }
        if (i >= seq.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + i + " outside range of sequence length " +
                    seq.length);
        }
        return seq[i];
    }

    public boolean getBoolean(int i) throws ConfigException {
        return getValue(i).asBoolean();
    }

    public int getInteger(int i) throws ConfigException {
        return getValue(i).asInteger();
    }

    public long getLong(int i) throws ConfigException {
        return getValue(i).asLong();
    }

    public double getDouble(int i) throws ConfigException {
        return getValue(i).asDouble();
    }

    public String getString(int i) throws ConfigException {
        return getValue(i).asString();
    }

    public Sequence getSequence(int i) throws ConfigException {
        return getValue(i).asSequence();
    }

    public Config getConfig(int i) throws ConfigException {
        return getValue(i).asConfig();
    }

    public Value getValue(int i) {
        return new Value(type, get(i));
    }

    public Value getLastValue() {
        if (seq.length == 0) {
            throw new IllegalStateException("Unable get last value of empty sequence");
        }
        return getValue(seq.length - 1);
    }

    public List<Value> values() {
        // Make a copy, so it cannot be modified with side effects.
        return stream().map(v -> new Value(type, v))
                       .collect(Collectors.toList());
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
        for (Value v : values()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(v.value.toString());
        }

        builder.append(']')
               .append(')');
        return builder.toString();
    }

    public Iterator<?> iterator() {
        return Spliterators.iterator(spliterator());
    }

    public Stream<?> stream() {
        return StreamSupport.stream(spliterator(), true);
    }

    public Spliterator<?> spliterator() {
        return Spliterators.spliterator(seq, 0, seq.length, characteristics);
    }

    public static Collector<Object, Builder, Sequence> collect(final Value.Type type) {
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

    public static class Builder {
        private final Value.Type   type;
        private final List<Object> seq;

        public Builder(Value.Type type) {
            this.type = type;
            this.seq = new ArrayList<>();
        }

        public Builder(Sequence base) {
            this(base.type);
            Collections.addAll(seq, base.seq);
        }

        public int size() {
            return seq.size();
        }

        public Value.Type type() {
            return type;
        }

        public Builder add(Object elem) {
            switch (type) {
                case STRING:
                    if ((elem instanceof Sequence) ||
                        (elem instanceof Config)) {
                        throw new IllegalArgumentException("Not a string value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    // Cast everything into string.
                    seq.add(elem.toString());
                    break;
                case BOOLEAN:
                    if (!(elem instanceof Boolean)) {
                        throw new IllegalArgumentException("Not a boolean value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(elem);
                    break;
                case NUMBER:
                    if (!(Number.class.isAssignableFrom(elem.getClass()))) {
                        throw new IllegalArgumentException("Not a number value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(elem);
                    break;
                case SEQUENCE:
                    if (!(elem instanceof Sequence)) {
                        throw new IllegalArgumentException("Not a sequence type: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(elem);
                    break;
                case CONFIG:
                    if (!(elem instanceof Config)) {
                        throw new IllegalArgumentException("Not a config type: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(elem);
                    break;
                default:
                    // TODO: Maybe support more element types in sequences?
                    throw new IllegalArgumentException("Not supported sequence value type: " + type);
            }
            return this;
        }

        public Builder addValue(Value value) {
            return add(value.value);
        }

        public Value getValue(int i) {
            if (i < 0) {
                throw new IllegalArgumentException("Invalid index " + i);
            }
            if (i >= seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Index " + i + " outside range of sequence length " +
                        seq.size());
            }
            return new Value(type, seq.get(i));
        }

        public Builder insert(int i, Object elem) {
            if (i < 0) {
                throw new IllegalArgumentException("Illegal insert index " + i);
            }
            if (i > seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Insert index " + i + " outside range of sequence length " +
                        seq.size());
            }
            switch (type) {
                case STRING:
                    if ((elem instanceof Sequence) ||
                        (elem instanceof Config)) {
                        throw new IllegalArgumentException("Not a string value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    // Cast everything into string.
                    seq.add(i, elem.toString());
                    break;
                case BOOLEAN:
                    if (!(elem instanceof Boolean)) {
                        throw new IllegalArgumentException("Not a boolean value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(i, elem);
                    break;
                case NUMBER:
                    if (!(Number.class.isAssignableFrom(elem.getClass()))) {
                        throw new IllegalArgumentException("Not a number value: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(i, elem);
                    break;
                case SEQUENCE:
                    if (!(elem instanceof Sequence)) {
                        throw new IllegalArgumentException("Not a sequence type: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(i, elem);
                    break;
                case CONFIG:
                    if (!(elem instanceof Config)) {
                        throw new IllegalArgumentException("Not a config type: " +
                                                           elem.getClass().getSimpleName());
                    }
                    seq.add(i, elem);
                    break;
                default:
                    throw new IllegalArgumentException("Not supported sequence value type: " + type);
            }
            return this;
        }

        public Builder insertValue(int i, Value value) {
            return insert(i, value.value);
        }

        public Builder replace(int i, Object value) {
            if (i < 0) {
                throw new IllegalArgumentException("Illegal replace index " + i);
            }
            if (i >= seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Replace index " + i + " outside range of sequence length " +
                        seq.size());
            }

            seq.remove(i);
            insert(i, value);

            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder replaceValue(int i, Value value) {
            return replace(i, value.value);
        }

        public Builder remove(int i) {
            if (i < 0) {
                throw new IllegalArgumentException("Illegal remove index " + i);
            }
            if (i >= seq.size()) {
                throw new IndexOutOfBoundsException(
                        "Remove index " + i + " outside range of sequence length " +
                        seq.size());
            }

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

        public Builder addAll(Collection<?> coll) {
            coll.forEach(this::add);
            return this;
        }

        public Builder addAll(Object... coll) {
            for (Object o : coll) {
                add(o);
            }
            return this;
        }

        public Sequence build() {
            return new Sequence(type, seq);
        }
    }

    // --- INTERNAL ---

    private final Object[]   seq;
    private final Value.Type type;
}
