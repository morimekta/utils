package net.morimekta.util;

/**
 * Interface for making objects support strong hash method. This will be
 * complementary to the java {@link Object#hashCode()} method, but with
 * higher entropy.
 */
public interface StrongHashable {
    /**
     * @return The string hash of the object.
     */
    long strongHash();
}
