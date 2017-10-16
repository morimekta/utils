package net.morimekta.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class StrongHashBuilderTest {
    private static class MyHashable implements StrongHashable {
        private final String value;
        public MyHashable(String value) {
            this.value = value;
        }

        @Override
        public long strongHash() {
            return new StrongHashBuilder().add(value).strongHash();
        }
    }

    @Test
    public void testSimple() {
        StrongHashBuilder a = new StrongHashBuilder();
        StrongHashBuilder b = new StrongHashBuilder();
        StrongHashBuilder c = new StrongHashBuilder();

        Stream.of(a, b).forEach(bld -> {
            bld.add((Object) true);
            bld.add((Object) (byte) 1);
            bld.add((Object) (short) 2);
            bld.add((Object) 3);
            bld.add((Object) 4L);
            bld.add((Object) 5.1f);
            bld.add((Object) 6.2d);
            bld.add((Object) '7');
            bld.add((Object) "8");
            bld.add(UUID.nameUUIDFromBytes("test".getBytes(UTF_8)));
            bld.add((Object) new boolean[]{false});
            bld.add((Object) new byte[]{1});
            bld.add((Object) new short[]{2});
            bld.add((Object) new int[]{3});
            bld.add((Object) new long[]{4L});
            bld.add((Object) new float[]{5.1f});
            bld.add((Object) new double[]{6.2d});
            bld.add((Object) new char[]{'7'});
            bld.add((Object) new String[]{"8"});
            bld.add((Object) new Object[]{UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8))});
            bld.add((Object) ImmutableList.of(UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8))));
            bld.add((Object) ImmutableMap.of(
                    UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8)),
                    55L));
            bld.add((Object) new MyHashable("test-3"));
        });

        // Same content, just different order.
        c.add((Object) true);
        c.add((Object) (byte) 1);
        c.add((Object) (short) 2);
        c.add((Object) 3);
        c.add((Object) 4L);
        c.add((Object) 5.1f);
        c.add((Object) 6.2d);
        c.add((Object) '7');
        c.add((Object) "8");
        c.add(UUID.nameUUIDFromBytes("test".getBytes(UTF_8)));
        c.add((Object) new boolean[]{false});
        c.add((Object) new byte[]{1});
        c.add((Object) new short[]{2});
        c.add((Object) new int[]{3});
        c.add((Object) new long[]{4L});
        c.add((Object) new float[]{5.1f});
        c.add((Object) new double[]{6.2d});
        c.add((Object) new String[]{"8"});  // swapped with
        c.add((Object) new char[]{'7'});    // this
        c.add((Object) new Object[]{UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8))});
        c.add((Object) ImmutableList.of(UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8))));
        c.add((Object) ImmutableMap.of(
                UUID.nameUUIDFromBytes("test-2".getBytes(UTF_8)),
                55L));
        c.add((Object) new MyHashable("test-3"));

        assertThat(a.strongHash(), is(b.strongHash()));
        assertThat(a.strongHash(), is(not(c.strongHash())));
        assertThat(a.strongHash(), is(-8065934364529681497L));
        assertThat(c.strongHash(), is(6693314637656873799L));
    }

    @Test
    public void testNulls() {
        StrongHashBuilder a = new StrongHashBuilder();

        a.add((boolean []) null);
        a.add((byte[]) null);
        a.add((short[]) null);
        a.add((int[]) null);
        a.add((long[]) null);
        a.add((char[]) null);
        a.add((float[]) null);
        a.add((double[]) null);
        a.add((Object[]) null);
        a.add((CharSequence) null);
        a.add((Collection) null);
        a.add((Map) null);
        a.add((Object) null);
        a.add((StrongHashable) null);

        assertThat(a.strongHash(), is(2796216992127785722L));
    }

    @Test
    public void testConstructor() {
        StrongHashBuilder a = new StrongHashBuilder();
        StrongHashBuilder b = new StrongHashBuilder(56197, 37);

        a.add("boo");
        b.add("boo");

        assertThat(a.strongHash(), is(not(b.strongHash())));
        assertThat(a.strongHash(), is(-5623952953802110992L));
    }
}
