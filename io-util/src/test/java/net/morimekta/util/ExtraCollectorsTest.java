package net.morimekta.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.morimekta.util.ExtraCollectors.inBatchesOf;
import static net.morimekta.util.ExtraCollectors.inNumBatches;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Tests for extra collectors.
 */
public class ExtraCollectorsTest {
    @Test
    public void testInBatchesOf() {
        List<Integer> sizes = IntStream.range(0, 10000)
                                       .mapToObj(String::valueOf)
                                       .collect(inBatchesOf(1037))
                                       .map(List::size)
                                       .collect(Collectors.toList());
        assertThat(sizes, is(equalTo(ImmutableList.of(1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 667))));

        sizes = IntStream.range(0, 10000)
                         .parallel()
                         .boxed()
                         .collect(inBatchesOf(1037))
                         .map(List::size)
                         .collect(Collectors.toList());

        assertThat(sizes, is(equalTo(ImmutableList.of(1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 667))));
    }

    @Test
    public void testInNumBatches() {
        List<Integer> sizes = IntStream.range(0, 10000)
                                       .mapToObj(String::valueOf)
                                       .collect(inNumBatches(9))
                                       .map(List::size)
                                       .collect(Collectors.toList());
        assertThat(sizes, is(equalTo(ImmutableList.of(1112, 1111, 1111, 1111, 1111, 1111, 1111, 1111, 1111))));

        sizes = IntStream.range(0, 10000)
                         .parallel()
                         .boxed()
                         .collect(inNumBatches(11))
                         .map(List::size)
                         .collect(Collectors.toList());

        assertThat(sizes, is(equalTo(ImmutableList.of(910, 909, 909, 909, 909, 909, 909, 909, 909, 909, 909))));
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<ExtraCollectors> c = ExtraCollectors.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());

        c.setAccessible(true);
        c.newInstance();  // to make code coverage closer to 100%.
        c.setAccessible(false);
    }
}
