package net.morimekta.util;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Base class for holding tuples.
 * <p>
 * <b>A note on tuples in java 8:</b><br>
 * This class is mainly meant for holding sets of values passed along in a
 * stream, not as a general holder class. It can be used as a mean of
 * returning tuples of values from methods, though the latter I would really
 * discourage, as it makes the code usually <b>less</b> readable. This would
 * be a moot problem if Java had native tuple handling, but alas.
 * <p>
 * Example code:
 * <pre>{@code
 * list.stream()
 *     .map(i -> tuple(i, ((Integer) i).hashCode())
 *     .sort(Comparators.comparing(Tuple.Tuple2::second))
 *     .findFirst()
 * }</pre>
 */
@Immutable
public class Tuple implements Iterable<Object> {
    public static <T> Tuple1<T> tuple(T value) {
        return new Tuple1<>(value);
    }
    public static <T1, T2> Tuple2<T1,T2> tuple(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    public static <T1, T2, T3> Tuple3<T1,T2,T3> tuple(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1,T2,T3,T4> tuple(T1 v1, T2 v2, T3 v3, T4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1,T2,T3,T4,T5> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return new Tuple6<>(v1, v2, v3, v4, v5, v6);
    }

    private final Object[] values;

    public Tuple(Object... values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Empty tuple");
        }
        this.values = values;
    }

    @Nonnull
    @Override
    public Iterator<Object> iterator() {
        return Arrays.asList(values).iterator();
    }

    public Object[] array() {
        return Arrays.copyOf(values, values.length);
    }

    public int size() {
        return values.length;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int i) {
        return (T) values[i];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Tuple(");
        for (int i = 0; i < values.length; ++i) {
            if (i > 0) builder.append(", ");
            builder.append(Strings.asString(values[i]));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        Tuple other = (Tuple) o;
        return Arrays.deepEquals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), Arrays.deepHashCode(values));
    }

    /**
     * A tuple class with a single value.
     *
     * @param <T> The tuple value type.
     */
    public static class Tuple1<T> extends Tuple {
        /**
         * Create a single value tuple.
         *
         * @param value The first value.
         */
        public Tuple1(T value) {
            super(value);
        }

        /**
         * @return The first tuple value.
         */
        public T first() {
            return get(0);
        }

        /** Pass-along constructor */
        Tuple1(@Nonnull Object[] values) {
            super(values);
        }
    }

    public static class Tuple2<T1, T2> extends Tuple1<T1> {
        public Tuple2(T1 v1, T2 v2) {
            super(new Object[]{v1, v2});
        }

        /**
         * @return The second tuple value.
         */
        public T2 second() {
            return get(1);
        }

        /** Pass-along constructor */
        Tuple2(@Nonnull Object[] values) {
            super(values);
        }
    }

    public static class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
        public Tuple3(T1 v1, T2 v2, T3 v3) {
            super(new Object[]{v1, v2, v3});
        }

        public T3 third() {
            return get(2);
        }

        /** Pass-along constructor */
        Tuple3(@Nonnull Object[] values) {
            super(values);
        }
    }

    public static class Tuple4<T1, T2, T3, T4> extends Tuple3<T1, T2, T3> {
        public Tuple4(T1 v1, T2 v2, T3 v3, T4 v4) {
            super(new Object[]{v1, v2, v3, v4});
        }

        public T4 fourth() {
            return get(3);
        }

        /** Pass-along constructor */
        Tuple4(@Nonnull Object[] values) {
            super(values);
        }
    }

    public static class Tuple5<T1, T2, T3, T4, T5> extends Tuple4<T1, T2, T3, T4> {
        public Tuple5(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
            super(new Object[]{v1, v2, v3, v4, v5});
        }

        public T5 fifth() {
            return get(4);
        }

        /** Pass-along constructor */
        Tuple5(@Nonnull Object[] values) {
            super(values);
        }
    }

    public static class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple5<T1, T2, T3, T4, T5> {
        public Tuple6(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
            super(new Object[]{v1, v2, v3, v4, v5, v6});
        }

        public T6 sixth() {
            return get(5);
        }
    }
}
