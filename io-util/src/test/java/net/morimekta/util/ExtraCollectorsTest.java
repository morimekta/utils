package net.morimekta.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import static net.morimekta.util.ExtraCollectors.inBatchesOf;
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
        List<Integer> sizes = new LinkedList<>();
        ExtraStreams.range(0, 10000)
                    .mapToObj(String::valueOf)
                    .collect(inBatchesOf(1037))
                    .forEach(batch -> sizes.add(batch.size()));

        assertThat(sizes, is(equalTo(ImmutableList.of(1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 667))));

        sizes.clear();
        ExtraStreams.range(0, 10000)
                    .parallel()
                    .mapToObj(String::valueOf)
                    .collect(inBatchesOf(1037))
                    .forEach(batch -> sizes.add(batch.size()));

        assertThat(sizes, is(equalTo(ImmutableList.of(1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 1037, 667))));
    }

    @Test
    public void testJoin() {
        assertThat(ExtraStreams.range(0, 20, 3)
                               .boxed()
                               .collect(ExtraCollectors.join(", ")),
                   is("0, 3, 6, 9, 12, 15, 18"));

        assertThat(ExtraStreams.range(0, 20, 3)
                               .mapToObj(OverridesToString::new)
                               .parallel()
                               .collect(ExtraCollectors.join(",\n")),
                   is(",\n" +
                      "xxx,\n" +
                      "xxxxxx,\n" +
                      "xxxxxxxxx,\n" +
                      "xxxxxxxxxxxx,\n" +
                      "xxxxxxxxxxxxxxx,\n" +
                      "xxxxxxxxxxxxxxxxxx"));

        assertThat(ExtraStreams.range(0, 20, 3)
                               .boxed()
                               .collect(ExtraCollectors.join(",\n", i -> "i" + i)),
                   is("i0,\n" +
                      "i3,\n" +
                      "i6,\n" +
                      "i9,\n" +
                      "i12,\n" +
                      "i15,\n" +
                      "i18"));

    }

    private class OverridesToString {
        private final int i;

        public OverridesToString(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return Strings.times("x", i);
        }
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
