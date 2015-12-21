package android.os;

import android.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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

        // Check that its NOT writable.
        thrown.expect(UnsupportedOperationException.class);
        empty.putString("KEY", "value");
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
