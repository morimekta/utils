package net.morimekta.console;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for unicode char.
 */
public class UnicodeTest {
    @Test
    public void testSurrogatePair() {
        Unicode unicode = new Unicode(0x2A6B2);

        assertEquals(2, unicode.printableWidth());
        assertEquals(2, unicode.length());
        assertEquals(2, unicode.toString().length());
        assertEquals(0x2A6B2, unicode.codepoint());
    }
}
