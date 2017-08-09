package net.morimekta.util;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static net.morimekta.util.ExtraStreams.times;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for extra streams.
 */
public class ExtraStreamsTest {
    @Test
    public void testRange() {
        assertRange(times(7), 0, 1, 2, 3, 4, 5, 6);
        assertRange(times(7), 0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testInvalidArguments() {
        try {
            times(-42);
            fail("No exception on invalid increment");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid recurrence count: -42", e.getMessage());
        }

        try {
            times(0);
            fail("No exception on invalid increment");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid recurrence count: 0", e.getMessage());
        }
    }

    @Test
    public void testTimes() {
        AtomicInteger calls = new AtomicInteger();
        ExtraStreams.times(55).forEach(i -> calls.incrementAndGet());
        assertThat(calls.get(), is(55));
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<ExtraStreams> c = ExtraStreams.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());

        c.setAccessible(true);
        c.newInstance();  // to make code coverage 100%.
        c.setAccessible(false);
    }

    public void assertRange(IntStream range, int... values) {
        List<Integer> expected = new LinkedList<>();
        for (int i : values) {
            expected.add(i);
        }
        List<Integer> actual = new LinkedList<>();
        range.forEachOrdered(actual::add);

        assertEquals(expected, actual);
    }
}
