package net.morimekta.testing;

import net.morimekta.testing.matchers.IsEqualIgnoreIndent;
import net.morimekta.testing.matchers.IsInRange;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for extra matchers.
 */
public class ExtraMatchersTest {
    @Test
    public void testInRange() {
        Matcher<Number> matcher = ExtraMatchers.isInRange(10, 100.5);

        assertThat(matcher, instanceOf(IsInRange.class));
        assertTrue(matcher.matches(50));
        assertTrue(matcher.matches(10));
        assertTrue(matcher.matches(100));

        assertFalse(matcher.matches(9.8));
        assertFalse(matcher.matches(101));
    }

    @Test
    public void testIsEqualIgnoreIndent() {
        Matcher<String> matcher = ExtraMatchers.isEqualIgnoreIndent("a\n    b");

        assertThat(matcher, instanceOf(IsEqualIgnoreIndent.class));
        assertTrue(matcher.matches("a\nb"));
        assertFalse(matcher.matches("a\n\n    b"));
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
