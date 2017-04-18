package net.morimekta.util;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.morimekta.util.BinaryUtil.fromBinaryCollection;
import static net.morimekta.util.BinaryUtil.toBinaryCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BinaryUtilTest {

    private Random random = new Random();

    @Test
    public void testReadWriteList() throws IOException {
        List<Binary> expected = IntStream.range(0, random.nextInt(5) + 5)
                                         .mapToObj(i -> {
                                             byte[] bytes = new byte[random.nextInt(50) + 50];
                                             random.nextBytes(bytes);
                                             return Binary.wrap(bytes);
                                         }).collect(Collectors.toList());
        byte[] converted = fromBinaryCollection(expected);
        List<Binary> actual = new ArrayList<>(toBinaryCollection(converted));
        assertThat(expected, is(actual));
    }

    @Test
    public void testReadWriteSet() throws IOException {
        Set<Binary> expected = IntStream.range(0, random.nextInt(5) + 5)
                                        .mapToObj(i -> {
                                             byte[] bytes = new byte[random.nextInt(50) + 50];
                                             random.nextBytes(bytes);
                                             return Binary.wrap(bytes);
                                         }).collect(Collectors.toSet());
        byte[] converted = fromBinaryCollection(expected);
        Set<Binary> actual = new HashSet<>(toBinaryCollection(converted));
        assertThat(expected, is(actual));
    }

}
