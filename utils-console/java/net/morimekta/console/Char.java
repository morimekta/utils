package net.morimekta.console;

/**
 * General interface implemented by character-like classes. A char
 * can represent an actual character (Unicode), or a control sequence
 * (Control, Color) that can alter the look or behaviour of the console
 * the application runs in.
 */
public interface Char {
    /**
     * Unicode codepoint representing this character.
     *
     * @return The 31 bit unsigned unicode code-point, or -1 if not
     *         representing a single unicode character.
     */
    int codepoint();

    /**
     * The number of character spaces taken up by this symbol. Usually 1, but
     * 0 for control sequences, control characters, and 2 for wide symbols like
     * CJK characters.
     *
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
