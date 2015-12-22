package android.os;

import android.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(BlockJUnit4ClassRunner.class)
public class BaseBundleTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        BaseBundle bundle = new BaseBundleImpl(100);
        assertEquals(0, bundle.size());

        // Check that its writable.
        bundle.putString("KEY", "value");
        assertEquals("value", bundle.getString("KEY"));
    }

    @Test
    public void testEmptyBunble() {
        @SuppressWarnings("unchecked")
        BaseBundle empty = new BaseBundleImpl(Collections.EMPTY_MAP);
        assertEquals(0, empty.size());

        assertTrue(empty.isEmpty());
        assertFalse(empty.containsKey("KEY"));

        // Check that its NOT writable.
        thrown.expect(UnsupportedOperationException.class);
        empty.putString("KEY", "value");
    }

    @Test
    public void testKeySet() {
        BaseBundle bundle = new BaseBundleImpl(10);
        bundle.putBoolean("A", true);
        bundle.putBoolean("B", true);
        bundle.putBoolean("C", false);
        bundle.putBoolean("D", false);
        bundle.putBoolean("E", true);

        Set<String> set = bundle.keySet();

        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertTrue(set.contains("C"));
        assertTrue(set.contains("D"));
        assertTrue(set.contains("E"));
        assertEquals(5, set.size());

        assertTrue(bundle.containsKey("A"));
        assertTrue(bundle.containsKey("B"));
        assertTrue(bundle.containsKey("C"));
        assertTrue(bundle.containsKey("D"));
        assertTrue(bundle.containsKey("E"));
        assertEquals(5, bundle.size());
    }

    @Test
    public void testBoolean() {
        BaseBundle bundle = new BaseBundleImpl(10);
        bundle.putBoolean("A", true);
        bundle.putBoolean("B", false);
        bundle.putBoolean("C", true);
        bundle.putBoolean("D", false);
        bundle.putBoolean("E", true);

        assertTrue(bundle.getBoolean("A", false));
        assertFalse(bundle.getBoolean("B", true));
        assertTrue(bundle.getBoolean("C", false));
        assertFalse(bundle.getBoolean("D", true));
        assertTrue(bundle.getBoolean("E", false));

        // non-existing
        assertFalse(bundle.getBoolean("F", false));
        assertTrue(bundle.getBoolean("G", true));

        // wrong type.
        assertEquals(4, bundle.getInt("B", 4));
    }

    private class BaseBundleImpl
            extends BaseBundle {
        public BaseBundleImpl(Map<String, Pair<Type, Object>> map) {
            super(map);
        }

        public BaseBundleImpl(int capacity) {
            super(capacity);
        }
    }
}
