package net.morimekta.config;

import org.junit.Test;

import static net.morimekta.config.Value.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Tests for value objects.
 */
public class ValueTest {
    @Test
    public void testValue() {
        Value num = create(12.34);
        Value i32 = create(1234567890);
        Value i64 = create(1234567890L);

        assertEquals(i32, create(1234567890));
        assertEquals(i32, i64); // int vs long...
        assertNotEquals(i32, num); // int vs long...
        assertEquals("Value(number,1234567890)", i32.toString());
        assertEquals("Value(number,12.34)", num.toString());

        Value str1 = create("a");
        Value str1_1 = create("a");
        Value str2 = create("b");

        assertEquals(str1, str1_1);
        assertNotEquals(str1, str2);
        assertEquals("Value(string,a)", str1.toString());
        assertEquals("Value(string,b)", str2.toString());
    }
    @Test
    public void testAsBoolean() throws ConfigException {
        assertAsBoolean(create(true), true);
        assertAsBoolean(create(false), false);
        assertAsBoolean(create(1), true);
        assertAsBoolean(create(0), false);
        assertAsBoolean(create("true"), true);
        assertAsBoolean(create("false"), false);
        assertAsBoolean(create("yes"), true);
        assertAsBoolean(create("no"), false);

        assertBadBoolean("Unable to parse the string \"foo\" to boolean", create("foo"));
        assertBadBoolean("Unable to convert number 4 to boolean", create(4));
        assertBadBoolean("Unable to convert double value to boolean", create(4.4));
        assertBadBoolean("Unable to convert type SEQUENCE to a boolean", create(Sequence.builder(Value.Type.BOOLEAN).build()));
    }

    private void assertAsBoolean(Value value, boolean b) throws ConfigException {
        assertEquals(b, value.asBoolean());
    }

    private void assertBadBoolean(String message, Value value) {
        try {
            value.asBoolean();
            fail("No exception on bad bool " + value);
        } catch (ConfigException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testAsInteger() throws ConfigException {
        assertAsInteger(create(1), 1);
        assertAsInteger(create(-1234567890), -1234567890);
        assertAsInteger(create("-1234567890"), -1234567890);

        assertBadInteger("Unable to convert type BOOLEAN to an int", create(false));
        assertBadInteger("Unable to parse string \"false\" to an int", create("false"));
    }

    private void assertAsInteger(Value value, int b) throws ConfigException {
        assertEquals(b, value.asInteger());
    }

    private void assertBadInteger(String message, Value value) {
        try {
            value.asInteger();
            fail("No exception on bad int " + value);
        } catch (ConfigException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testAsLong() throws ConfigException {
        assertAsLong(create(1L), 1);
        assertAsLong(create(-1234567890L), -1234567890L);
        assertAsLong(create("-1234567890"), -1234567890L);

        assertBadLong("Unable to convert type BOOLEAN to a long", create(false));
        assertBadLong("Unable to parse string \"false\" to a long", create("false"));
    }

    private void assertAsLong(Value value, long b) throws ConfigException {
        assertEquals(b, value.asLong());
    }

    private void assertBadLong(String message, Value value) {
        try {
            value.asLong();
            fail("No exception on bad long " + value);
        } catch (ConfigException e) {
            assertEquals(message, e.getMessage());
        }
    }


    @Test
    public void testAsDouble() throws ConfigException {
        assertAsDouble(create(1.0), 1.0);
        assertAsDouble(create(-12345.67890), -12345.67890);
        assertAsDouble(create("-12345.67890"), -12345.67890);

        assertBadDouble("Unable to convert type BOOLEAN to a double", create(false));
        assertBadDouble("Unable to parse string \"false\" to a double", create("false"));
    }

    private void assertAsDouble(Value value, double b) throws ConfigException {
        assertEquals(b, value.asDouble(), 0.0);
    }

    private void assertBadDouble(String message, Value value) {
        try {
            value.asDouble();
            fail("No exception on bad double " + value);
        } catch (ConfigException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testAsString() throws ConfigException {
        assertEquals("string",
                     create("string").asString());
        assertEquals("1234",
                     create(1234).asString());

        try {
            create(Sequence.builder(Value.Type.STRING).add("A").build()).asString();
        } catch (ConfigException e) {
            assertEquals("Unable to convert SEQUENCE to a string", e.getMessage());
        }
    }

    @Test
    public void testAsSequence() throws ConfigException {
        assertEquals(Sequence.builder(Value.Type.STRING).add("A").build(),
                     create(Sequence.create("A")).asSequence());

        try {
            create("string").asSequence();
        } catch (ConfigException e) {
            assertEquals("Unable to convert STRING to a sequence", e.getMessage());
        }
    }

    @Test
    public void testAsConfig() throws ConfigException {
        assertEquals(Config.builder().putString("a", "b").build(),
                     create(Config.builder().putString("a", "b").build()).asConfig());

        try {
            create("string").asConfig();
        } catch (ConfigException e) {
            assertEquals("Unable to convert STRING to a config", e.getMessage());
        }
    }

}
