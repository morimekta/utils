package android.util;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object
 * provides a sensible implementation of equals(), returning true if equals()
 * is true on each of the contained objects.
 */
public final class Pair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a Pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pair))
            return false;
        Pair<?, ?> other = (Pair<?, ?>) o;

        return (Objects.equals(first, other.first) &&
                Objects.equals(second, other.second));
    }

    @Override
    public int hashCode() {
        int hash = Pair.class.hashCode();
        hash ^= Objects.hashCode(first);
        hash ^= Objects.hashCode(second);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Pair(%s,%s)",
                             Objects.toString(first),
                             Objects.toString(second));
    }
}
