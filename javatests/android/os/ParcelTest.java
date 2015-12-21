package android.os;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testByte() {
        Parcel parcel = Parcel.obtain();

        parcel.writeByte((byte) 4);
        assertEquals((byte) 4, parcel.readByte());

        parcel.writeByte((byte) 125);
        assertEquals((byte) 125, parcel.readByte());

        parcel.writeByte((byte) -128);
        assertEquals((byte) -128, parcel.readByte());
    }

    @Test
    public void testDouble() {
        Parcel parcel = Parcel.obtain();

        parcel.writeDouble(-7.8d);
        assertEquals(-7.8d, parcel.readDouble(), 0.0001);

        parcel.writeDouble(7.8d);
        assertEquals(7.8d, parcel.readDouble(), 0.0001);

        parcel.writeDouble(548934892789238E203);
        assertEquals(548934892789238E203, parcel.readDouble(), 0.0001);

        parcel.writeDouble(-548934892789238E203);
        assertEquals(-548934892789238E203, parcel.readDouble(), 0.0001);
    }
}
