package android.os;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import android.util.Pair;

public final class Bundle extends BaseBundle implements Parcelable {
    public Bundle() {
        this.loader = ClassLoader.getSystemClassLoader();
    }

    public Bundle(ClassLoader loader) {
        this.loader = loader != null ? loader : ClassLoader.getSystemClassLoader();
    }

    public Bundle(int capacity) {
        this();
    }

    public Bundle(Bundle bundle) {
        this.loader = bundle.loader;
        map.putAll(bundle.map);
    }

    public void putAll(Bundle bundle) {
        map.putAll(bundle.map);
    }

    public boolean hasFileDescriptors() {
        return false;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public Object clone() {
        return new Bundle(this);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping
     * of the desired type exists for the given key or a null value is
     * explicitly associated with the key.
     */
    public Bundle getBundle(String key) {
        return get(key, Type.BUNDLE, null);
    }

    public void putBundle(String key, Bundle value) {
        put(key, Type.BUNDLE, value);
    }

    public byte getByte(String key, byte defaultValue) {
        return get(key, Type.BYTE, defaultValue);
    }

    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    public void putByte(String key, byte b) {
        put(key, Type.BYTE, b);
    }

    public byte[] getByteArray(String key) {
        return get(key, Type.BYTE_ARRAY, null);
    }

    public void putByteArray(String key, byte[] value) {
        put(key, Type.BYTE_ARRAY, value);
    }

    public char getChar(String key, char defaultValue) {
        return get(key, Type.CHAR, defaultValue);
    }

    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    public void putChar(String key, char c) {
        put(key, Type.CHAR, c);
    }

    public char[] getCharArray(String key) {
        return get(key, Type.CHAR_ARRAY, null);
    }

    public void putCharArray(String key, char[] value) {
        put(key, Type.CHAR_ARRAY, value);
    }

    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        return get(key, Type.CHAR_SEQUENCE, defaultValue);
    }

    public CharSequence getCharSequence(String key) {
        return getCharSequence(key, null);
    }

    public void putCharSequence(String key, CharSequence value) {
        put(key, Type.CHAR_SEQUENCE, value);
    }

    public CharSequence[] getCharSequenceArray(String key) {
        return get(key, Type.CHAR_SEQUENCE_ARRAY, null);
    }

    public void putCharSequenceArray(String key, CharSequence[] value) {
        put(key, Type.CHAR_SEQUENCE_ARRAY, value);
    }

    public float getFloat(String key, float defaultValue) {
        return get(key, Type.FLOAT, defaultValue);
    }

    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public void putFloat(String key, float value) {
        put(key, Type.FLOAT, value);
    }

    public float[] getFloatArray(String key) {
        return get(key, Type.FLOAT_ARRAY, null);
    }

    public void putFloatArray(String key, float[] value) {
        put(key, Type.FLOAT_ARRAY, value);
    }

    public ArrayList<Integer> getIntegerArrayList(String key) {
        return get(key, Type.INT_ARRAY_LIST, null);
    }

    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        put(key, Type.INT_ARRAY_LIST, value);
    }

    public <T extends Parcelable> T getParcelable(String key) {
        return get(key, Type.PARCELABLE, null);
    }

    public <T extends Parcelable> void putParcelable(String key, T value) {
        put(key, Type.PARCELABLE, value);
    }

    public <T extends Parcelable> T[] getParcelableArray(String key) {
        return get(key, Type.PARCELABLE_ARRAY, null);
    }

    public <T extends Parcelable> void putParcelableArray(String key, T[] value) {
        put(key, Type.PARCELABLE_ARRAY, value);
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        return get(key, Type.PARCELABLE_ARRAY_LIST, null);
    }

    public <T extends Parcelable> void putParcelableArrayList(String key, ArrayList<T> value) {
        put(key, Type.PARCELABLE_ARRAY_LIST, value);
    }

    public <T extends Serializable> T getSerializable(String key) {
        return get(key, Type.SERIALIZABLE, null);
    }

    public <T extends Serializable> void putSerializable(String key, T value) {
        put(key, Type.SERIALIZABLE, value);
    }

    public short getShort(String key, short defaultValue) {
        return get(key, Type.SHORT, defaultValue);
    }

    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    public void putShort(String key, short value) {
        put(key, Type.SHORT, value);
    }

    public short[] getShortArray(String key) {
        return get(key, Type.SHORT_ARRAY, null);
    }

    public void putShortArray(String key, short[] value) {
        put(key, Type.SHORT_ARRAY, value);
    }

    public ArrayList<String> getStringArrayList(String key) {
        return get(key, Type.STRING_ARRAY_LIST, null);
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        put(key, Type.STRING_ARRAY_LIST, value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        synchronized (map) {
            dest.writeInt(size());
            for (Map.Entry<String, Pair<Type, Object>> entry : map.entrySet()) {
                if (!writeToParcel(dest, entry.getKey(), entry.getValue().first, entry.getValue().second)) {
                    Object value = entry.getValue().second;
                    switch (entry.getValue().first) {
                        case BUNDLE:
                            Bundle b = (Bundle) value;
                            b.writeToParcel(dest, flags);
                            break;
                        case BYTE:
                            dest.writeByte((Byte) value);
                            break;
                        case BYTE_ARRAY:
                            dest.writeByteArray((byte[]) value);
                            break;
                        case CHAR:
                            dest.writeCharArray(new char[]{(Character) value});
                            break;
                        case CHAR_ARRAY:
                            dest.writeCharArray((char[]) value);
                            break;
                        case CHAR_SEQUENCE:
                            dest.writeString(value.toString());
                            break;
                        case CHAR_SEQUENCE_ARRAY:
                        case FLOAT:
                        case FLOAT_ARRAY:
                        case INT_ARRAY_LIST:
                        case PARCELABLE:
                        case PARCELABLE_ARRAY:
                        case PARCELABLE_ARRAY_LIST:
                        case SERIALIZABLE:
                        case SHORT:
                        case SHORT_ARRAY:
                        case STRING_ARRAY_LIST:
                        default:
                            throw new BadParcelableException(
                                    "Unknown type for bundle serialization " + entry.getValue().first);
                    }
                }
            }
        }
    }

    public static Creator<Bundle> CREATOR = new ClassLoaderCreator<Bundle>() {
        @Override
        public Bundle createFromParcel(Parcel source, ClassLoader loader) {
            final int size = source.readInt();
            Bundle bundle = new Bundle(size);
            return bundle;
        }

        @Override
        public Bundle createFromParcel(Parcel source) {
            return createFromParcel(source, ClassLoader.getSystemClassLoader());
        }

        @Override
        public Bundle[] newArray(int size) {
            return new Bundle[size];
        }
    };

    @SuppressWarnings("unchecked")
    public static Bundle EMPTY = new Bundle(Collections.EMPTY_MAP);

    private Bundle(Map<String, Pair<Type, Object>> map) {
        super(map);
    }

    // --- PRIVATE AFTER HERE ---

    ClassLoader loader;

}
