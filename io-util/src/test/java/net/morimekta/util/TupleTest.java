package net.morimekta.util;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TupleTest {
    @Test
    public void testConstructor() {
        Tuple tmp = new Tuple("foo");
        assertThat(tmp.array().length, is(1));
        try {
            new Tuple();
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Empty tuple"));
        }
    }

    @Test
    public void testTuple1() {
        Tuple.Tuple1<String> tuple =
                Tuple.tuple("foo");

        assertThat(tuple.first(), is("foo"));
    }

    @Test
    public void testTuple2() {
        Tuple.Tuple2<String,Integer> tuple =
                Tuple.tuple("foo", 5);

        assertThat(tuple.first(), is("foo"));
        assertThat(tuple.second(), is(5));
    }

    @Test
    public void testTuple3() {
        Tuple.Tuple3<String,Integer,String> tuple =
                Tuple.tuple("foo", 5, "bar");

        assertThat(tuple.first(), is("foo"));
        assertThat(tuple.second(), is(5));
        assertThat(tuple.third(), is("bar"));
    }

    @Test
    public void testTuple4() {
        Tuple.Tuple4<String,Integer,String,Long> tuple =
                Tuple.tuple("foo", 5, "bar", 42L);

        assertThat(tuple.first(), is("foo"));
        assertThat(tuple.second(), is(5));
        assertThat(tuple.third(), is("bar"));
        assertThat(tuple.fourth(), is(42L));
    }

    @Test
    public void testTuple5() {
        Tuple.Tuple5<String,Integer,String,Long,Boolean> tuple =
                Tuple.tuple("foo", 5, "bar", 42L, false);

        assertThat(tuple.first(), is("foo"));
        assertThat(tuple.second(), is(5));
        assertThat(tuple.third(), is("bar"));
        assertThat(tuple.fourth(), is(42L));
        assertThat(tuple.fifth(), is(false));

        Tuple.Tuple5<String,Integer,String,Long,Boolean> eq =
                Tuple.tuple("foo", 5, "bar", 42L, false);
        Tuple.Tuple5<String,Integer,String,Long,Boolean> ne =
                Tuple.tuple("foo", 5, "bar", 41L, false);

        assertThat(tuple, is(tuple));
        assertThat(tuple, is(eq));
        assertThat(tuple, is(not(ne)));
        assertThat(tuple.equals(new Object()), is(false));
        assertThat(tuple.equals(Tuple.tuple(44)), is(false));
    }

    @Test
    public void testTuple6() {
        Tuple.Tuple6<String,Integer,String,Long,Boolean,UUID> tuple =
                Tuple.tuple("foo", 5, "bar", 42L, false, UUID.randomUUID());

        assertThat(tuple.first(), is("foo"));
        assertThat(tuple.second(), is(5));
        assertThat(tuple.third(), is("bar"));
        assertThat(tuple.fourth(), is(42L));
        assertThat(tuple.fifth(), is(false));
        assertThat(tuple.sixth(), is(instanceOf(UUID.class)));

        assertThat(tuple.array().length, is(6));
        assertThat(tuple.hashCode(), is(not(0)));
        assertThat(tuple.iterator().next(), is("foo"));
        assertThat(tuple.size(), is(6));
        assertThat(tuple.toString(), is("Tuple(\"foo\", 5, \"bar\", 42, false, " + tuple.sixth().toString() + ")"));
    }
}
