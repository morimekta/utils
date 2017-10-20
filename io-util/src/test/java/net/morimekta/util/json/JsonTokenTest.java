package net.morimekta.util.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing special aspects of the JsonToken class.
 */
public class JsonTokenTest {
    private final char[] buffer = "[\"\\\\↓ÑI©ôðé\\b\\f\\r\\n\\t\\\"\\u4f92\",{\"key\":1337,123.45:null}]".toCharArray();

    @Test
    public void testIsNull() {
        JsonToken token;

        token = new JsonToken(JsonToken.Type.TOKEN, buffer, 50, 4, 1, 5);
        assertEquals(JsonToken.Type.TOKEN, token.getType());
        assertEquals(1, token.getLineNo());
        assertEquals(5, token.getLinePos());
        assertEquals("null", token.asString());
        assertTrue(token.isNull());

        token = new JsonToken(JsonToken.Type.TOKEN, buffer, 50, 3, 1, 1);
        assertEquals("nul", token.asString());
        assertFalse(token.isNull());
    }

    @Test
    public void testIsSymbol() {
        JsonToken token;

        token = new JsonToken(JsonToken.Type.SYMBOL, buffer, 0, 1, 1, 1);
        assertTrue(token.isSymbol());

        token = new JsonToken(JsonToken.Type.SYMBOL, buffer, 1, 1, 1, 1);
        assertFalse(token.isSymbol());

        token = new JsonToken(JsonToken.Type.SYMBOL, buffer, 0, 1, 1, 1);
        assertTrue(token.isSymbol('['));
        assertFalse(token.isSymbol(']'));
    }

    @Test
    public void testIsBoolean() {
        JsonToken token;

        token = new JsonToken(JsonToken.Type.TOKEN, "true".toCharArray(), 0, 4, 1, 1);
        assertTrue(token.isBoolean());
        assertTrue(token.booleanValue());
        token = new JsonToken(JsonToken.Type.TOKEN, "false".toCharArray(), 0, 5, 1, 1);
        assertTrue(token.isBoolean());
        assertFalse(token.booleanValue());
        token = new JsonToken(JsonToken.Type.TOKEN, "yes".toCharArray(), 0, 3, 1, 1);
        assertFalse(token.isBoolean());
    }

    @Test
    public void testNumbers() {
        JsonToken token;

        token = new JsonToken(JsonToken.Type.TOKEN, "44".toCharArray(), 0, 2, 1, 1);
        assertFalse(token.isNumber());
        assertFalse(token.isInteger());
        assertFalse(token.isDouble());

        token = new JsonToken(JsonToken.Type.NUMBER, "44".toCharArray(), 0, 2, 1, 1);
        assertTrue(token.isNumber());
        assertTrue(token.isInteger());
        assertFalse(token.isDouble());
        assertEquals((byte) 44, token.byteValue());
        assertEquals((short) 44, token.shortValue());
        assertEquals(44, token.intValue());
        assertEquals(44L, token.longValue());
        assertEquals(44.0, token.doubleValue(), 0.001);

        token = new JsonToken(JsonToken.Type.NUMBER, "44.44".toCharArray(), 0, 5, 1, 1);
        assertTrue(token.isNumber());
        assertFalse(token.isInteger());
        assertTrue(token.isDouble());
        assertEquals(44.44, token.doubleValue(), 0.001);
    }

    @Test
    public void testRawJsonLiteral() {
        JsonToken token = new JsonToken(JsonToken.Type.LITERAL, buffer, 1, 29, 1, 1);
        assertEquals("\"\\\\↓ÑI©ôðé\\b\\f\\r\\n" +
                     "\\t\\\"\\u4f92\"", token.asString());
        assertEquals("\\\\↓ÑI©ôðé\\b\\f\\r\\n" +
                     "\\t\\\"\\u4f92", token.rawJsonLiteral());

        // and with illegal escape characters.
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\0\"".toCharArray(), 0, 4, 1, 1);
        assertEquals("\\0", token.rawJsonLiteral());
        // and with illecal escaped unicode.
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\u01\"".toCharArray(), 0, 6, 1, 1);
        assertEquals("\\u01", token.rawJsonLiteral());
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\ubals\"".toCharArray(), 0, 8, 1, 1);
        assertEquals("\\ubals", token.rawJsonLiteral());
    }

    @Test
    public void testDecodeJsonLiteral() {
        JsonToken token = new JsonToken(JsonToken.Type.LITERAL, buffer, 1, 29, 1, 1);
        assertEquals("\"\\\\↓ÑI©ôðé\\b\\f\\r\\n" +
                     "\\t\\\"\\u4f92\"", token.asString());
        assertEquals("\\↓ÑI©ôðé\b\f\r\n" +
                     "\t\"侒", token.decodeJsonLiteral());

        // and with illegal escape characters.
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\0\"".toCharArray(), 0, 4, 1, 1);
        assertEquals("?", token.decodeJsonLiteral());
        // and with illecal escaped unicode.
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\u01\"".toCharArray(), 0, 6, 1, 1);
        assertEquals("?", token.decodeJsonLiteral());
        token = new JsonToken(JsonToken.Type.LITERAL, "\"\\ubals\"".toCharArray(), 0, 8, 1, 1);
        assertEquals("?", token.decodeJsonLiteral());
    }

    @Test
    public void testHashCode() {
        JsonToken token1 = new JsonToken(JsonToken.Type.LITERAL, buffer, 1, 36, 1, 1);
        JsonToken token2 = new JsonToken(JsonToken.Type.SYMBOL, buffer, 0, 1, 1, 1);
        JsonToken token3 = new JsonToken(JsonToken.Type.TOKEN, buffer, 52, 4, 1, 1);

        assertNotEquals(token1, token2);
        assertNotEquals(token1, token3);
        assertNotEquals(token2, token3);

        assertNotEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1.hashCode(), token3.hashCode());
        assertNotEquals(token2.hashCode(), token3.hashCode());
    }

    @Test
    public void testNotEquals() {
        JsonToken token1 = new JsonToken(JsonToken.Type.LITERAL, buffer, 1, 36, 1, 1);

        assertTrue(token1.equals(token1));
        assertFalse(token1.equals(null));
        assertFalse(token1.equals(new Object()));
    }
}
