package android.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(BlockJUnit4ClassRunner.class)
public class PairTest {
    @Test
    public void testEquals() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b1 = Pair.create(4, "b");
        Pair<Integer, String> b2 = Pair.create(4, "b");

        assertNotEquals(a, b1);
        assertEquals(b1, b2);
    }

    @Test
    public void testHashCode() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b1 = Pair.create(4, "b");
        Pair<Integer, String> b2 = Pair.create(4, "b");
        Pair<UUID, String> c = Pair.create(null, "c");

        assertNotEquals(a.hashCode(), b1.hashCode());
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    public void testToString() {
        Pair<Integer, String> a = Pair.create(4, "a");
        Pair<Integer, String> b = Pair.create(4, "b");
        Pair<UUID, String> c = Pair.create(null, "c");

        assertEquals("(4,a)", a.toString());
        assertEquals("(4,b)", b.toString());
        assertEquals("(null,c)", c.toString());
    }
}
