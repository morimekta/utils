package android.os;

import java.nio.ByteBuffer;
import java.io.Serializable;

/**
 * ...
 */
public class Parcel {
    private ByteBuffer buffer;
    private int position;

    private Parcel() {
        buffer = new ByteBuffer();
        position = 0;
    }

    public static Parcel obtain() {
        return new Parcel();
    }

    public void appendFrom(Parcel parcel, int offset, int length) {
        buffer.append(parcel.buffer);
    }
    
    public int dataAvail() {
        return buffer.size() - position;
    }

    public int dataCapacity() {
        return buffer.capacity() - buffer.size();
    }
    public int dataPosition() {
        return position;
    }
    public int dataSize() {
        return buffer.size();
    }

    public void setDataCapacity(int size);
    public void setDataPosition(int pos);
    public void setDataSize(int size);

    public byte[] marshall();
    public void unmarshall(byte[] data, int offset, int length);

    // --- Data readers and writers.

    public void writeByte(byte b) {}
    public byte readByte();

    public void writeDouble(double d);
    public double readDouble();

    public void writeFloat(float f);
    public float readFloat();

    public void writeInt(int i);
    public int readInt();

    public void writeLong(lont l);
    public long readLong();

    public void writeString(String s);
    public String readString();

    public void writeBooleanArray(boolean[] arr);
    public void readBooleanArray(boolean[] dest);
    public boolean[] createBooleanArray();

    public void writeByteArray(byte[] arr);
    public void writeByteArray(byte[] arr, int offset, int len);
    public void readByteArray(byte[] dest);
    public byte[] createByteArray();

    public void writeCharArray(char[] arr);
    public void readCharArray(char[] dest);
    public char[] createCharArray();

    public void writeDoubleArray(double[] arr);
    public void readDoubleArray(double[] dest);
    public double[] createDoubleArray();

    public void writeFloatArray(float[] arr);
    public void readFloatArray(float[] dest);
    public float[] createFloatArray();

    public void writeIntArray(int[] arr);
    public void readIntArray(int[] dest);
    public int[] createIntArray();

    public void writeLongArray(long[] arr);
    public void readLongArray(long[] dest);
    public long[] createLongArray();
    
    public void writeStringArray(String[] arr);
    public void readStringArray(String[] dest);
    public String[] createStringArray();
    
    public void writeSparseBooleanArray(SparseBooleanArray arr);
    public SparseBooleanArray readSparseBooleanArray();

    public void writeParcelable(Parcelable p, int flags);
    public <T extends Parcelable> T readParcelable(ClassLoader loader);

    public <T extends Parcelable> void writeParcelableArray(T[] arr, int flags);
    public <T extends Parcelable> T[] readParcelableArray(ClassLoader loader);

    public <T extends Parcelable> void writeTypedObject(T source, int flags);
    public <T extends Parcelable> T readTypedObject(Parcelable.Creator<T> creator);

    public <T extends Parcelable> void writeTypedArray(T[] arr, int flags);
    public <T extends Parcelable> T[] createTypedArray(Parcelable.Creaor<T> creator);

    public <T extends Parcelable> void writeTypedList(List<T> list);
    public <T extends Parcelable> ArrayList<T> createTypedArrayList(Parcelable.Creaor<T> creator);
}
