package net.morimekta.console;

/**
 * General interface implemented by character-like classes.
 */
public interface Char {
    /**
     * Unicode codepoint representing this character.
     *
     * @return The 32 but signed unicode codepoint, or -1 if not representing a
     *         single unicode character.
     */
    int codepoint();

    /**
     * The number of spaces taken up by this character.
     * @return The printable width of the character.
     */
    int printableWidth();

    /**
     * Number of utf-16 characters that this character takes up if enclosed
     * in a java string.
     *
     * @return The char count of the character.
     */
    int length();
}
