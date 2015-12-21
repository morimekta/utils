package android.os;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

/**
 * Non-Android port of android serialization utility class.
 * <p>
 * Base post of the
 * <a href="http://developer.android.com/reference/android/os/Parcel.html">android.os.Parcel</a>
 * class. This is mimicking the interface and base functionality of the parcel,
 * but not the IPC callable functionality. As opposed to the Android version
 * this will serialize everything immediately. There are also a couple of
 * methods *not* implemented as they refer to android-internal utility classes
 * or android system functionality.
 * <p>
 * E.g. methods referencing android.util.* classes are omitted (SparseArray,
 * Size, SizeF, IBinder).
 * <p>
 * The main reason for porting this is to be able to extensively test parcelable
 * classes without needing the whole
 * <p>
 * Parcel: http://developer.android.com/reference/android/os/Parcel.html
 * Parcelable: http://developer.android.com/reference/android/os/Parcelable.html
 * <p>
 * Also see {@link android.os.Parcelable}.
 */
@SuppressWarnings("unused")
public final class Parcel {
    /**
     * Retrieve a new Parcel object from the pool.
     *
     * @return The obtained parcel.
     */
    public static Parcel obtain() {
        synchronized (pool) {
            if (pool.size() > 0) {
                return pool.poll();
            }
        }
        return new Parcel();
    }

    /**
     * Put a Parcel object back into the pool.
     */
    public void recycle() {
        size = 0;
        position = 0;
        // clear data so it won't leak.
        fill(buffer, 0, buffer.length, (byte) 0);

        synchronized (pool) {
            // If the pool is already full, cycle back in new parcels to enable
            // refreshing memory.
            if (pool.size() == kMaxPoolSize) {
                pool.poll();
            }
            pool.offer(this);
        }
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
            throw new IllegalArgumentException(
                    "New capacity " + capacity + " is smaller than current data size: " + size);
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
        if (newSize == size)
            return;
        if (newSize < size) {
            trim(newSize);
        } else {
            grow(newSize);
        }
    }

    public byte[] marshall() {
        byte[] result = new byte[size];
        arraycopy(buffer, 0, result, 0, size);
        return result;
    }

    public void unmarshall(byte[] data, int offset, int length) {
        position = size = 0;
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
        if (s != null) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            ensureCapacity(size + 4 + bytes.length);
            size += encode(bytes.length, buffer, size);
            arraycopy(bytes, 0, buffer, size, bytes.length);
            size += bytes.length;
        } else {
            writeInt(-1);
        }
    }

    public String readString() {
        final int len = readInt();
        if (len < 0)
            return null;
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
        arraycopy(arr, 0, dest, 0, min(dest.length, arr.length));
    }

    public boolean[] createBooleanArray() {
        final int len = readInt();
        byte[] tmp = new byte[booleanArraySizeToBytes(len)];
        arraycopy(buffer, position, tmp, 0, tmp.length);
        position += tmp.length;
        return unpackBits(tmp, len);
    }

    public void writeByteArray(byte[] arr) {
        writeByteArray(arr, 0, arr.length);
    }

    public void writeByteArray(byte[] arr, int offset, int len) {
        ensureCapacity(size + 4 + len);
        size += encode(len, buffer, size);
        arraycopy(arr, offset, buffer, size, len);
        size += len;
    }

    public void readByteArray(byte[] dest) {
        final int len = readInt();
        final int copyLen = min(dest.length, len);
        position += 4;
        ensureAvailable(len);
        arraycopy(buffer, position, dest, 0, copyLen);
        position += len;
    }

    public byte[] createByteArray() {
        final int len = readInt();
        ensureAvailable(len);
        byte[] out = new byte[len];
        arraycopy(buffer, position, out, 0, len);
        position += len;
        return out;
    }

    public void writeCharArray(char[] arr) {
        writeByteArray(chars2bytes(arr));
    }

    public void readCharArray(char[] dest) {
        char[] out = createCharArray();
        arraycopy(out, 0, dest, 0, min(out.length, dest.length));
    }

    public char[] createCharArray() {
        return bytes2chars(createByteArray());
    }

    public void writeDoubleArray(double[] arr) {
        ensureCapacity(4 + (8 * arr.length));
        writeInt(arr.length);
        for (double d : arr) {
            writeDouble(d);
        }
    }

    public void readDoubleArray(double[] dest) {
        double[] out = createDoubleArray();
        arraycopy(out, 0, dest, 0, min(dest.length, out.length));
    }

    public double[] createDoubleArray() {
        final int len = readInt();
        final double[] out = new double[len];
        for (int i = 0; i < len; ++i) {
            out[i] = readDouble();
        }
        return out;
    }

    public void writeFloatArray(float[] arr) {
        ensureCapacity(4 + (4 * arr.length));
        writeInt(arr.length);
        for (float f : arr) {
            writeFloat(f);
        }
    }

    public void readFloatArray(float[] dest) {
        float[] out = createFloatArray();
        arraycopy(out, 0, dest, 0, min(dest.length, out.length));
    }

    public float[] createFloatArray() {
        final int len = readInt();
        final float[] out = new float[len];
        for (int i = 0; i < len; ++i) {
            out[i] = readFloat();
        }
        return out;
    }

    public void writeIntArray(int[] arr) {
        ensureCapacity(4 + (4 * arr.length));
        writeInt(arr.length);
        for (int i : arr) {
            writeInt(i);
        }
    }

    public void readIntArray(int[] dest) {
        int[] out = createIntArray();
        arraycopy(out, 0, dest, 0, min(dest.length, out.length));
    }

    public int[] createIntArray() {
        final int len = readInt();
        final int[] out = new int[len];
        for (int i = 0; i < len; ++i) {
            out[i] = readInt();
        }
        return out;
    }

    public void writeLongArray(long[] arr) {
        ensureCapacity(4 + (8 * arr.length));
        writeInt(arr.length);
        for (long l : arr) {
            writeLong(l);
        }
    }

    public void readLongArray(long[] dest) {
        long[] out = createLongArray();
        arraycopy(out, 0, dest, 0, min(dest.length, out.length));
    }

    public long[] createLongArray() {
        final int len = readInt();
        final long[] out = new long[len];
        for (int i = 0; i < len; ++i) {
            out[i] = readLong();
        }
        return out;
    }

    public void writeStringArray(String[] arr) {
        writeInt(arr.length);
        for (String s : arr) {
            writeString(s);
        }
    }

    public void readStringArray(String[] dest) {
        final int len = readInt();
        for (int i = 0; i < len; i++) {
            String tmp = readString();
            if (i < dest.length) {
                dest[i] = tmp;
            }
        }
    }

    public String[] createStringArray() {
        final int len = readInt();
        String[] out = new String[len];
        for (int i = 0; i < len; ++i) {
            out[i] = readString();
        }
        return out;
    }

    public void writeParcelable(Parcelable p, int flags) {
        writeCreator(p.getClass());
        p.writeToParcel(this, flags);
    }

    public <T extends Parcelable> T readParcelable(ClassLoader loader) {
        Parcelable.Creator<T> creator = readCreator(loader);
        return creator.createFromParcel(this);
    }

    public <T extends Parcelable> void writeParcelableArray(T[] arr, int flags) {
        writeCreator(arr.getClass().getComponentType());
        writeTypedArray(arr, flags);
    }

    public <T extends Parcelable> T[] readParcelableArray(ClassLoader loader) {
        Parcelable.Creator<T> creator = readCreator(loader);
        return createTypedArray(creator);
    }

    public <T extends Parcelable> void writeTypedObject(T source, int flags) {
        source.writeToParcel(this, flags);
    }

    public <T extends Parcelable> T readTypedObject(Parcelable.Creator<T> creator) {
        return creator.createFromParcel(this);
    }

    public <T extends Parcelable> void writeTypedArray(T[] arr, int flags) {
        writeInt(arr.length);
        for (T t : arr) {
            writeTypedObject(t, flags);
        }
    }

    public <T extends Parcelable> T[] createTypedArray(Parcelable.Creator<T> creator) {
        final int len = readInt();
        final T[] out = creator.newArray(len);
        for (int i = 0; i < len; ++i) {
            out[i] = readTypedObject(creator);
        }
        return out;
    }

    public <T extends Parcelable> void writeTypedList(List<T> list) {
        writeInt(list.size());
        for (T value : list) {
            writeTypedObject(value, 0);
        }
    }

    public <T extends Parcelable> ArrayList<T> createTypedArrayList(Parcelable.Creator<T> creator) {
        final int size = readInt();
        ArrayList<T> out = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            out.add(readTypedObject(creator));
        }
        return out;
    }

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

    // --- PRIVATE AFTER HERE ---

    private byte[] buffer;
    private int    size;
    private int    position;

    private static final int kCapacityStep = 1 << 10;  // 1k per capacity step.
    private static final int kMaxPoolSize  = 10;
    private static final Queue<Parcel> pool;

    static {
        pool = new ArrayDeque<>(kMaxPoolSize);
    }

    // Only used in tests.
    @SuppressWarnings("unused")
    protected static void clearPool() {
        synchronized (pool) {
            pool.clear();
        }
    }

    private Parcel() {
        buffer = new byte[kCapacityStep];
        size = 0;
        position = 0;
    }

    private void ensureCapacity(int capacity) {
        if (buffer.length < capacity) {
            capacity = (int) ceil((float) capacity / kCapacityStep) * kCapacityStep;
            byte[] newBuffer = new byte[capacity];
            arraycopy(buffer, 0, newBuffer, 0, size);
            buffer = newBuffer;
        }
    }

    private void ensureAvailable(int avail) {
        if ((position + avail) > size) {
            throw new ParcelFormatException(
                    "Unable to read " + avail + " bytes at position " + position +
                    " only " + (size - position) + " bytes available");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Parcelable.Creator<T> getCreator(Class<T> klass) {
        if (klass.equals(String.class)) {
            return (Parcelable.Creator<T>) STRING_CREATOR;
        } else {
            try {
                Field field = klass.getDeclaredField("CREATOR");
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Parcelable.Creator<T> creator = (Parcelable.Creator<T>) field.get(null);
                field.setAccessible(accessible);
                return creator;
            } catch (NoSuchFieldException e) {
                throw new BadParcelableException("No creator for parcelable class " + klass.getSimpleName());
            } catch (IllegalAccessException e) {
                throw new BadParcelableException("Unable to access creator for class " + klass.getSimpleName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Parcelable.Creator<T> readCreator(ClassLoader loader) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        String name = readString();
        try {
            return getCreator((Class<T>) loader.loadClass(name));
        } catch (ClassNotFoundException cfe) {
            throw new BadParcelableException(cfe);
        }
    }

    private void writeCreator(Class<?> klass) {
        writeString(klass.getName());
    }

    private void trim(int newSize) {
        if (newSize < size) {
            size = newSize;
        }
    }

    private void grow(int newSize) {
        if (newSize > size) {
            ensureCapacity(newSize);
            fill(buffer, size, newSize, (byte) 0);
            size = newSize;
        }
    }

    private void append(byte[] array, int offset, int length) {
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
        into[offset]     = (byte)  (value & 0x00000000000000ffL);
        into[offset + 1] = (byte) ((value & 0x000000000000ff00L) >> 8);
        into[offset + 2] = (byte) ((value & 0x0000000000ff0000L) >> 16);
        into[offset + 3] = (byte) ((value & 0x00000000ff000000L) >> 24);
        into[offset + 4] = (byte) ((value & 0x000000ff00000000L) >> 32);
        into[offset + 5] = (byte) ((value & 0x0000ff0000000000L) >> 40);
        into[offset + 6] = (byte) ((value & 0x00ff000000000000L) >> 48);
        into[offset + 7] = (byte) ((value & 0xff00000000000000L) >> 56);
        return 8;
    }

    private static long decode(byte[] from, int pos, int bytes) {
        long result = 0;
        switch (bytes) {
            case 8:
                result |= valueOf(from[pos + 7]) << 56;
            case 7:
                result |= valueOf(from[pos + 6]) << 48;
            case 6:
                result |= valueOf(from[pos + 5]) << 40;
            case 5:
                result |= valueOf(from[pos + 4]) << 32;
            case 4:
                result |= valueOf(from[pos + 3]) << 24;
            case 3:
                result |= valueOf(from[pos + 2]) << 16;
            case 2:
                result |= valueOf(from[pos + 1]) << 8;
            case 1:
                result |= valueOf(from[pos]);
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

    private static int setBit(int b, boolean value, int pos) {
        int bit = (value ? (0x01 << pos) : 0);
        return b | bit;
    }

    private static byte[] packBits(boolean[] arr) {
        final int len = booleanArraySizeToBytes(arr.length);
        final byte[] dest = new byte[len];

        int pos = 0;
        for (int i = 0; i < len; ++i) {
            int b = 0;
            for (int j = 0; j < 8; ++j, ++pos) {
                if (pos < arr.length) {
                    b = setBit(b, arr[pos], j);
                }
            }
            dest[i] = (byte) b;
        }
        return dest;
    }

    private static boolean[] unpackBits(byte[] arr, int len) {
        final boolean[] out = new boolean[len];
        for (int i = 0; i < len; ++i) {
            out[i] = isTrue(arr, i);
        }
        return out;
    }

    private static long valueOf(byte b) {
        if (b < 0)
            return 0x100 + b;
        return b;
    }

    private byte[] chars2bytes(char[] chars) {
        byte[] out = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; ++i) {
            int op = i * 2;
            out[op] = (byte) (chars[i] % 0x00ff);
            out[op + 1] = (byte) ((chars[i] % 0xff00) >> 8);
        }
        return out;
    }

    private char[] bytes2chars(byte[] bytes) {
        char[] out = new char[bytes.length / Character.BYTES];
        for (int i = 0; i < out.length; ++i) {
            int bp = i * 2;
            int a = bytes[bp + 1];
            if (a < 0) a = 0x100 + a;
            int b = bytes[bp];
            if (b < 0) b = 0x100 + b;
            out[i] = (char) (a << 8 | b);
        }
        return out;
    }
}

