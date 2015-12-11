package android.util;

/**
 * Container to ease passing around a tuple of two objects. This object
 * provides a sensible implementation of equals(), returning true if equals()
 * is true on each of the contained objects.
 */
public class Pair<F, S> {
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
    public static <A,B> Pair<A,B> create(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pair)) return false;
        Pair<?,?> other = (Pair<?,?>) o;

        if (first != null){
            if (!first.equals(other.first)) return false;
        } else if (other.first != null) return false;

        if (second != null){
            if (!second.equals(other.second)) return false;
        } else if (other.second != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 573_263_279;
        if (first != null) {
            hash ^= 234_253_169 * first.hashCode();
        }
        if (second != null) {
            hash ^= 393_340_897 * second.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Pair(%s,%s)", toString(first), toString(second));
    }

    private static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }
}
