package net.morimekta.testing;

import net.morimekta.testing.matchers.EqualIgnoreIndent;
import net.morimekta.testing.matchers.EqualToLines;
import net.morimekta.testing.matchers.InRange;
import net.morimekta.testing.matchers.OneOf;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for extra matchers.
 */
public class ExtraMatchersTest {
    @Test
    public void testInRange() {
        Matcher<Number> matcher = ExtraMatchers.inRange(10, 100.5);

        assertThat(matcher, instanceOf(InRange.class));
        assertTrue(matcher.matches(50));
        assertTrue(matcher.matches(10));
        assertTrue(matcher.matches(100));

        assertFalse(matcher.matches(9.8));
        assertFalse(matcher.matches(101));
    }

    @Test
    public void testEqualLines() {
        Matcher<String> matcher = ExtraMatchers.equalToLines("a\n  b\nc");

        assertThat(matcher, is(instanceOf(EqualToLines.class)));
        assertThat(matcher.matches("a\n  b\nc"), is(true));
        assertThat(matcher.matches("a\r\n  b\r\nc"), is(true));

        assertThat(matcher.matches("a\r\n  b\r\n"), is(false));
        assertThat(matcher.matches(null), is(false));
    }

    @Test
    public void testEqualIgnoreIndent() {
        Matcher<String> matcher = ExtraMatchers.equalIgnoreIndent("a\n    b");

        assertThat(matcher, instanceOf(EqualIgnoreIndent.class));
        assertTrue(matcher.matches("a\nb"));
        assertTrue(matcher.matches("a\r\nb"));
        assertTrue(matcher.matches("a\n    b"));
        assertFalse(matcher.matches("a\n\n    b"));
    }

    @Test
    public void testOneOf() {
        Matcher<String> matcher = ExtraMatchers.oneOf("a", "b", null);

        assertThat(matcher, is(instanceOf(OneOf.class)));
        assertThat(matcher.matches("a"), is(true));
        assertThat(matcher.matches("b"), is(true));
        assertThat(matcher.matches("c"), is(false));
        assertThat(matcher.matches(null), is(true));
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = ExtraMatchers.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }
}
