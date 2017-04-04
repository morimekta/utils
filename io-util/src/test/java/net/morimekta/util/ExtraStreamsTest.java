package net.morimekta.util;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static net.morimekta.util.ExtraStreams.range;
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
        assertRange(range(0, 10), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertRange(range(0, 10, 1, true), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertRange(range(0, 10, 3, true), 0, 3, 6, 9);
        assertRange(range(100, 110, 3), 100, 103, 106, 109);
        assertRange(times(7), 0, 1, 2, 3, 4, 5, 6);
        assertRange(times(7), 0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testInvalidArguments() {
        try {
            range(0, 1, 0);
            fail("No exception on invalid increment");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid increment 0", e.getMessage());
        }

        try {
            range(0, 0, 1);
            fail("No exception on invalid increment");
        } catch (IllegalArgumentException e) {
            assertEquals("[0,0) not a valid range", e.getMessage());
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
