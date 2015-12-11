package android.os;

import android.util.SparseBooleanArray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;

/**
 * ...
 */
public final class Parcel {
    public static final Parcelable.Creator<String> STRING_CREATOR = new Parcelable.Creator<String>() {
        @Override
        public String createFromParcel(Parcel source) {
            return source.readString();
        }

        @Override
        public String[] newArray(int size) {
            return new String[size];
        }
    };

    /**
     * Retrieve a new Parcel object from the pool.
     *
     * @return The obtained parcel.
     */
    public static Parcel obtain() {
        return new Parcel();
    }

    /**
     * Put a Parcel object back into the pool.
     */
    public void recycle() {
        size = 0;
        position = 0;
    }

    public void appendFrom(Parcel parcel, int offset, int length) {
        append(parcel.buffer, offset, length);
    }

    public int dataAvail() {
        return size - position;
    }

    public int dataCapacity() {
        return buffer.length;
    }

    public int dataPosition() {
        return position;
    }

    public int dataSize() {
        return size;
    }

    public void setDataCapacity(int capacity) {
        if (capacity < size) {
            throw new IllegalArgumentException("New capacity " + capacity + " is smaller than current data size: " + size);
        }
        byte[] tmp = new byte[capacity];
        arraycopy(buffer, 0, tmp, 0, size);
        buffer = tmp;
    }

    public void setDataPosition(int pos) {
        if (pos > size) {
            throw new IllegalArgumentException("New position is after last known byte.");
        }
        position = pos;
    }

    public void setDataSize(int newSize) {
        if (newSize == size) return;
        if (newSize < size) {
            trim(newSize);
        } else {
            grow(newSize);
        }
    }

    public synchronized byte[] marshall() {
        byte[] result = new byte[size];
        arraycopy(buffer, 0, result, 0, size);
        return result;
    }

    public synchronized void unmarshall(byte[] data, int offset, int length) {
        size = 0;
        append(data, offset, length);
    }

    // --- Data readers and writers.

    public void writeByte(byte b) {
        ensureCapacity(size + 1);
        size += encode(b, buffer, size);
    }

    public byte readByte() {
        ensureAvailable(1);
        byte tmp = buffer[position];
        ++position;
        return tmp;
    }

    public void writeDouble(double d) {
        ensureCapacity(size + 8);
        long value = Double.doubleToLongBits(d);
        size += encode(value, buffer, size);
    }

    public double readDouble() {
        ensureAvailable(8);
        long value = decode(buffer, position, 8);
        position += 8;
        return Double.longBitsToDouble(value);
    }

    public void writeFloat(float f) {
        ensureCapacity(size + 4);
        int value = Float.floatToIntBits(f);
        size += encode(value, buffer, size);
    }

    public float readFloat() {
        ensureAvailable(4);
        int value = (int) decode(buffer, position, 4);
        position += 4;
        return Float.intBitsToFloat(value);
    }

    public void writeInt(int i) {
        ensureCapacity(size + 4);
        size += encode(i, buffer, size);
    }

    public int readInt() {
        ensureAvailable(4);
        int value = (int) decode(buffer, position, 4);
        position += 4;
        return value;
    }

    public void writeLong(long l) {
        ensureCapacity(size + 8);
        size += encode(l, buffer, size);
    }

    public long readLong() {
        ensureAvailable(8);
        long value = decode(buffer, position, 8);
        position += 8;
        return value;
    }

    public void writeString(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        ensureCapacity(size + 4 + bytes.length);
        size += encode(bytes.length, buffer, size);
        arraycopy(bytes, 0, buffer, size, bytes.length);
        size += bytes.length;
    }

    public String readString() {
        ensureAvailable(4);
        int len = (int) decode(buffer, position, 4);
        position += 4;
        ensureAvailable(len);
        String out = new String(buffer, position, len, StandardCharsets.UTF_8);
        position += len;
        return out;
    }

    public void writeBooleanArray(boolean[] arr) {
        byte[] bytes = packBits(arr);
        ensureCapacity(4 + bytes.length);
        encode(arr.length, buffer, 4);
        size += 4;
        arraycopy(bytes, 0, buffer, size, bytes.length);
        size += bytes.length;
    }

    public void readBooleanArray(boolean[] dest) {
        boolean[] arr = createBooleanArray();
        arraycopy(arr, 0, dest, 0, Math.min(dest.length, arr.length));
    }

    public boolean[] createBooleanArray() {
        ensureAvailable(4);
        int len = (int) decode(buffer, position, 4);
        position += 4;
        byte[] tmp = new byte[booleanArraySizeToBytes(len)];
        arraycopy(buffer, position, tmp, 0, tmp.length);
        position += tmp.length;
        return unpackBits(tmp, len);
    }

    public void writeByteArray(byte[] arr) {
        writeByteArray(arr, 0, arr.length);
    }

    public synchronized void writeByteArray(byte[] arr, int offset, int len) {
        ensureCapacity(size + 4 + len);
        size += encode(len, buffer, size);
        arraycopy(arr, offset, buffer, size, len);
        size += len;
    }

    public synchronized void readByteArray(byte[] dest) {
        ensureAvailable(4);
        int len = (int) decode(buffer, position, 4);
        int copyLen = Math.min(dest.length, len);
        position += 4;
        ensureAvailable(len);
        arraycopy(buffer, position, dest, 0, copyLen);
        position += len;
    }

    public synchronized byte[] createByteArray() {
        ensureAvailable(4);
        int len = (int) decode(buffer, position, 4);
        position += 4;
        ensureAvailable(len);
        byte[] out = new byte[len];
        arraycopy(buffer, position, out, 0, len);
        position += len;
        return out;
    }

    public void writeCharArray(char[] arr) {
    }

    public void readCharArray(char[] dest) {

    }

    public char[] createCharArray() {
        return null;
    }

    public void writeDoubleArray(double[] arr) {

    }

    public void readDoubleArray(double[] dest) {

    }

    public double[] createDoubleArray() {
        return null;
    }

    public void writeFloatArray(float[] arr) {
    }

    public void readFloatArray(float[] dest) {

    }

    public float[] createFloatArray() {
        return null;
    }

    public void writeIntArray(int[] arr) {

    }

    public void readIntArray(int[] dest) {

    }

    public int[] createIntArray() {
        return null;
    }

    public void writeLongArray(long[] arr) {

    }

    public void readLongArray(long[] dest) {

    }

    public long[] createLongArray() {
        return null;
    }

    public void writeStringArray(String[] arr) {

    }

    public void readStringArray(String[] dest) {

    }

    public String[] createStringArray() {
        return null;
    }

    public void writeSparseBooleanArray(SparseBooleanArray arr) {

    }

    public SparseBooleanArray readSparseBooleanArray() {
        return null;
    }

    public void writeParcelable(Parcelable p, int flags) {

    }

    public <T extends Parcelable> T readParcelable(ClassLoader loader) {
        return null;
    }

    public <T extends Parcelable> void writeParcelableArray(T[] arr, int flags) {

    }

    public <T extends Parcelable> T[] readParcelableArray(ClassLoader loader) {
        return null;
    }

    public <T extends Parcelable> void writeTypedObject(T source, int flags) {

    }

    public <T extends Parcelable> T readTypedObject(Parcelable.Creator<T> creator) {
        return null;
    }

    public <T extends Parcelable> void writeTypedArray(T[] arr, int flags) {

    }

    public <T extends Parcelable> T[] createTypedArray(Parcelable.Creator<T> creator) {
        return null;
    }

    public <T extends Parcelable> void writeTypedList(List<T> list) {

    }

    public <T extends Parcelable> ArrayList<T> createTypedArrayList(Parcelable.Creator<T> creator) {
        return null;
    }

    // --- PRIVATE AFTER HERE ---

    private byte[] buffer;
    private int size;
    private int position;

    private Parcel() {
        buffer = new byte[1024];
        size = 0;
        position = 0;
    }

    private void ensureCapacity(int capacity) {
        if (buffer.length < capacity) {
            capacity = (int) Math.ceil(capacity / 1024) * 1024;
            byte[] newBuffer = new byte[capacity];
            arraycopy(buffer, 0, newBuffer, 0, size);
            buffer = newBuffer;
        }
    }

    private void ensureAvailable(int avail) {
        if ((position + avail) > size) {
            throw new IllegalStateException(
                    "Unable to read " + avail + " bytes at position " + position +
                            " only " + (size - position) + " bytes available");
        }
    }

    private synchronized void trim(int newSize) {
        if (newSize < size) {
            size = newSize;
        }
    }

    private synchronized void grow(int newSize) {
        if (newSize > size) {
            ensureCapacity(newSize);
            Arrays.fill(buffer, size, newSize, (byte) 0);
            size = newSize;
        }
    }

    private synchronized void append(byte[] array, int offset, int length) {
        ensureCapacity(size + length);
        arraycopy(array, offset, buffer, size, length);
        size += length;
    }

    private static int encode(byte value, byte[] into, int offset) {
        into[offset] = value;
        return 1;
    }

    private static int encode(int value, byte[] into, int offset) {
        into[offset] = (byte) (value & 0x000000ff);
        into[offset + 1] = (byte) ((value & 0x0000ff00) >> 8);
        into[offset + 2] = (byte) ((value & 0x00ff0000) >> 16);
        into[offset + 3] = (byte) ((value & 0xff000000) >> 24);
        return 4;
    }

    private static int encode(long value, byte[] into, int offset) {
        into[offset] = (byte) (value & 0x00000000000000ffL);
        into[offset + 1] = (byte) ((value % 0x000000000000ff00L) >> 8);
        into[offset + 2] = (byte) ((value % 0x0000000000ff0000L) >> 16);
        into[offset + 3] = (byte) ((value % 0x00000000ff000000L) >> 24);
        into[offset + 4] = (byte) ((value % 0x000000ff00000000L) >> 32);
        into[offset + 5] = (byte) ((value % 0x0000ff0000000000L) >> 40);
        into[offset + 6] = (byte) ((value % 0x00ff000000000000L) >> 48);
        into[offset + 7] = (byte) ((value % 0xff00000000000000L) >> 56);
        return 8;
    }

    private static long decode(byte[] from, int pos, int bytes) {
        long result = 0;
        switch (bytes) {
            case 8:
                result |= (byte) (valueOf(from[pos + 7]) << 56);
            case 7:
                result |= (byte) (valueOf(from[pos + 6]) << 48);
            case 6:
                result |= (byte) (valueOf(from[pos + 5]) << 40);
            case 5:
                result |= (byte) (valueOf(from[pos + 4]) << 32);
            case 4:
                result |= (byte) (valueOf(from[pos + 3]) << 24);
            case 3:
                result |= (byte) (valueOf(from[pos + 2]) << 16);
            case 2:
                result |= (byte) (valueOf(from[pos + 1]) << 8);
            case 1:
                result |= (byte) (valueOf(from[pos]));
        }
        return result;
    }

    private static int booleanArraySizeToBytes(int size) {
        int bytes = size / 8;
        return bytes + ((size % 8 > 0) ? 1 : 0);
    }

    private static boolean isTrue(byte b, int pos) {
        return ((b & (0x01 << pos)) != 0);
    }

    private static boolean isTrue(byte[] arr, int pos) {
        return isTrue(arr[pos / 8], pos % 8);
    }

    private static byte setBit(byte b, boolean value, int pos) {
        byte mask = (byte) (0xFF ^ (0x01 << pos));
        byte bit = (byte) (value ? (0x01 << pos) : 0);
        return (byte) ((b & mask) | bit);
    }

    private static byte[] packBits(boolean[] arr) {
        int len = booleanArraySizeToBytes(arr.length);
        byte[] dest = new byte[len];
        for (int i = 0; i < dest.length; ++i) {
            byte b = 0;
            for (int j = 0; j < 8; ++j) {
                int pos = 8 * j + i;
                if (pos < arr.length) {
                    b = setBit(b, arr[pos], j);
                }
            }
            dest[i] = b;
        }
        return dest;
    }

    private static boolean[] unpackBits(byte[] arr, int len) {
        boolean[] out = new boolean[len];
        for (int i = 0; i < len; ++i) {
            out[i] = isTrue(arr, i);
        }
        return out;
    }

    private static long valueOf(byte b) {
        if (b < 0) return 0x100 + b;
        return b;
    }
}
