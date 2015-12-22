package android.os;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertArrayEquals;
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
        parcel.writeByte((byte) -4);
        parcel.writeByte((byte) 125);
        parcel.writeByte((byte) -128);
        assertEquals(4, parcel.dataSize());

        assertEquals((byte) 4, parcel.readByte());
        assertEquals((byte) -4, parcel.readByte());
        assertEquals((byte) 125, parcel.readByte());
        assertEquals((byte) -128, parcel.readByte());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testDouble() {
        Parcel parcel = Parcel.obtain();

        parcel.writeDouble(-7.8d);
        parcel.writeDouble(7.8d);
        parcel.writeDouble(1797693048934892789238E287);
        parcel.writeDouble(-1797693048934892789238E287);
        assertEquals(4 * 8, parcel.dataSize());

        assertEquals(-7.8d, parcel.readDouble(), 0.000000001);
        assertEquals(7.8d, parcel.readDouble(), 0.000000001);
        assertEquals(1797693048934892789238E287, parcel.readDouble(), 0.000000001);
        assertEquals(-1797693048934892789238E287, parcel.readDouble(), 0.000000001);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testFloat() {
        Parcel parcel = Parcel.obtain();

        parcel.writeFloat(-7.8f);
        parcel.writeFloat(7.8f);
        parcel.writeFloat(33489348927892E25f);
        parcel.writeFloat(-33489348927892E25f);
        assertEquals(4 * 4, parcel.dataSize());

        assertEquals(-7.8d, parcel.readFloat(), 0.0001);
        assertEquals(7.8d, parcel.readFloat(), 0.0001);
        assertEquals(33489348927892E25f, parcel.readFloat(), 0.0001);
        assertEquals(-33489348927892E25f, parcel.readFloat(), 0.0001);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testInt() {
        Parcel parcel = Parcel.obtain();

        parcel.writeInt(8);
        parcel.writeInt(-8);
        parcel.writeInt(2048934892);
        parcel.writeInt(-2048934892);
        assertEquals(4 * 4, parcel.dataSize());

        assertEquals(8, parcel.readInt());
        assertEquals(-8, parcel.readInt());
        assertEquals(2048934892, parcel.readInt());
        assertEquals(-2048934892, parcel.readInt());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testLong() {
        Parcel parcel = Parcel.obtain();

        parcel.writeLong(8);
        parcel.writeLong(-8);
        parcel.writeLong(8548934892113347344L);
        parcel.writeLong(-8548934892113347344L);
        assertEquals(4 * 8, parcel.dataSize());

        assertEquals(8, parcel.readLong());
        assertEquals(-8, parcel.readLong());
        assertEquals(8548934892113347344L, parcel.readLong());
        assertEquals(-8548934892113347344L, parcel.readLong());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testString() {
        Parcel parcel = Parcel.obtain();

        parcel.writeString("blaargh");
        parcel.writeString("");
        parcel.writeString("\0something\0");
        parcel.writeString("asdjkh asdjkh aSLJKDH JHKL asdjkhg asdjkhg asdjkhg asdjkhgf asdjkhgf asdjkhgf sajkdhgf skajdhgf asdjhkgf asdjkhg sdjkh");
        assertEquals(152, parcel.dataSize());

        assertEquals("blaargh", parcel.readString());
        assertEquals("", parcel.readString());
        assertEquals("\0something\0", parcel.readString());
        assertEquals("asdjkh asdjkh aSLJKDH JHKL asdjkhg asdjkhg asdjkhg asdjkhgf asdjkhgf asdjkhgf sajkdhgf skajdhgf asdjhkgf asdjkhg sdjkh", parcel.readString());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testBooleanArray() {
        Parcel parcel = Parcel.obtain();

        boolean[] arr1 = new boolean[]{};
        boolean[] arr2 = new boolean[]{true, false, true, false, true, false, true, false, true, false, true, false};

        parcel.writeBooleanArray(arr1);
        parcel.writeBooleanArray(arr2);

        assertArrayEquals(arr1, parcel.createBooleanArray());
        assertArrayEquals(arr2, parcel.createBooleanArray());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testByteArray() {
        Parcel parcel = Parcel.obtain();

        byte[] arr1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0x10, 0x20, 0x50, (byte) 0xff, -123};
        byte[] arr2 = new byte[]{8, 7, 6};

        parcel.writeByteArray(arr1);
        parcel.writeByteArray(arr2);
        parcel.writeByteArray(arr1, 3, 7);

        assertArrayEquals(arr1, parcel.createByteArray());
        assertArrayEquals(arr2, parcel.createByteArray());
        assertArrayEquals(new byte[]{4, 5, 6, 7, 8, 9, 0}, parcel.createByteArray());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testCharArray() {
        Parcel parcel = Parcel.obtain();

        char[] arr1 = new char[]{'a', 'b', 'ý', 'æ', '\0', 'G', '\n'};
        char[] arr2 = new char[]{};

        parcel.writeCharArray(arr1);
        parcel.writeCharArray(arr2);

        assertArrayEquals(arr1, parcel.createCharArray());
        assertArrayEquals(arr2, parcel.createCharArray());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testDoubleArray() {
        Parcel parcel = Parcel.obtain();

        double[] arr1 = new double[]{-7.8d, 7.8d, -1.797693048934892789238E308, 0.24703286234234E-323};
        double[] arr2 = new double[]{};

        parcel.writeDoubleArray(arr1);
        parcel.writeDoubleArray(arr2);

        assertArrayEquals(arr1, parcel.createDoubleArray(), 0.000000001);
        assertArrayEquals(arr2, parcel.createDoubleArray(), 0.000000001);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testFloatArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testIntArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testLongArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testStringArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testParcelable() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testParcelableArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testTypedObject() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testTypedArray() {
        Parcel parcel = Parcel.obtain();

    }

    @Test
    public void testTypedArrayList() {
        Parcel parcel = Parcel.obtain();

    }

    public static class TestParcelable
            implements Parcelable {
        public int            number;
        public TestParcelable typedObject;
        public TestParcelable parcelable;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(number);
            if (typedObject != null) {
                dest.writeByte((byte) 0);
                dest.writeTypedObject(typedObject, flags);
            } else {
                dest.writeByte((byte) 1);
            }
            if (parcelable != null) {
                dest.writeByte((byte) 1);
                dest.writeParcelable(parcelable, flags);
            } else {
                dest.writeByte((byte) 0);
            }
        }

        public static Creator<TestParcelable> CREATOR = new Creator<TestParcelable>() {
            @Override
            public TestParcelable createFromParcel(Parcel source) {
                TestParcelable out = new TestParcelable();
                out.number = source.readInt();
                if (source.readByte() == 0) {
                    out.typedObject = source.readTypedObject(this);
                }
                if (source.readByte() == 1) {
                    out.parcelable = source.readParcelable(TestParcelable.class.getClassLoader());
                }
                return out;
            }

            @Override
            public TestParcelable[] newArray(int size) {
                return new TestParcelable[size];
            }
        };
    }
}
