package android.os;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

@RunWith(BlockJUnit4ClassRunner.class)
public class ParcelTest {
    @After
    public void tearDown() {
        Parcel.clearPool();
    }

    @Test
    public void testObtain() {
        Parcel parcel = Parcel.obtain();
        Parcel other = Parcel.obtain();

        // Creates new instances when the pool is empty.
        assertNotSame(parcel, other);

        parcel.recycle();

        Parcel other2 = Parcel.obtain();

        // Make sure the object is reused.
        assertSame(parcel, other2);
    }
}
