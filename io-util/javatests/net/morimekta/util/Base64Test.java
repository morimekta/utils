package net.morimekta.util;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for Base64 utility.
 */
public class Base64Test {
    private static final String lorem =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
            "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
            "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
            "culpa qui officia deserunt mollit anim id est laborum.";

    @Test
    public void testEncodeSizes() {
        assertEquals("", new String(Base64.decode("")));
        assertEquals("YQ", Base64.encodeToString(Strings.times("a", 1).getBytes(UTF_8)));
        assertEquals("YWE", Base64.encodeToString(Strings.times("a", 2).getBytes(UTF_8)));
        assertEquals("YWFh", Base64.encodeToString(Strings.times("a", 3).getBytes(UTF_8)));
        assertEquals("YWFhYQ", Base64.encodeToString(Strings.times("a", 4).getBytes(UTF_8)));
        assertEquals("YWFhYWE", Base64.encodeToString(Strings.times("a", 5).getBytes(UTF_8)));
        assertEquals("YWFhYWFh", Base64.encodeToString(Strings.times("a", 6).getBytes(UTF_8)));
        assertEquals("YWFhYWFhYQ", Base64.encodeToString(Strings.times("a", 7).getBytes(UTF_8)));
        assertEquals("YWFhYWFhYWE", Base64.encodeToString(Strings.times("a", 8).getBytes(UTF_8)));
    }

    @Test
    public void testDecodeSizes() {
        assertEquals("a", new String(Base64.decode("YQ")));
        assertEquals("aa", new String(Base64.decode("YWE")));
        assertEquals("aaa", new String(Base64.decode("YWFh")));
        assertEquals("aaaa", new String(Base64.decode("YWFhYQ")));
        assertEquals("aaaaa", new String(Base64.decode("YWFhYWE")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFhYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode("YWFhYWFhYQ")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE")));
    }

    @Test
    public void testDecodeSizesWithPadding() {
        assertEquals("", new String(Base64.decode("")));
        assertEquals("a", new String(Base64.decode("YQ==")));
        assertEquals("aa", new String(Base64.decode("YWE=")));
        assertEquals("aaa", new String(Base64.decode("YWFh")));
        assertEquals("aaaa", new String(Base64.decode("YWFhYQ==")));
        assertEquals("aaaaa", new String(Base64.decode("YWFhYWE=")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFhYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode("YWFhYWFhYQ==")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE=")));
    }

    @Test
    public void testDecodeSizesWithSpaces() {
        assertEquals("a", new String(Base64.decode("YQ")));
        assertEquals("aa", new String(Base64.decode("Y W E")));
        assertEquals("aaa", new String(Base64.decode("YW Fh")));
        assertEquals("aaaa", new String(Base64.decode("YWFh\nYQ==")));
        assertEquals("aaaaa", new String(Base64.decode("YWFh\r\nYWE=")));
        assertEquals("aaaaaa", new String(Base64.decode("YWFh\tYWFh")));
        assertEquals("aaaaaaa", new String(Base64.decode(" YWFhYWFhYQ = =")));
        assertEquals("aaaaaaaa", new String(Base64.decode("YWFhYWFhYWE =")));
    }

    @Test
    public void testDecodeBad() {
        assertBad("Bad Base64 input character decimal 46 in array position 16",
                  "With punctuation.");
        assertBad("Invalid base64 character \\u0255",
                  "Mis=matched padding=");
        assertBad("Input string was null.",
                  null);
    }

    private void assertBad(String message, String encoded) {
        try {
            Base64.decode(encoded);
            fail("Missing exception on bad input: " + message);
        } catch (IllegalArgumentException|NullPointerException e) {
            // e.printStackTrace();
            assertEquals(message, e.getMessage());
        }
    }
}
