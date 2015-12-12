package android.os;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Pair;

public abstract class BaseBundle {
    protected final Map<String, Pair<Type, Object>> map;

    protected BaseBundle(Map<String, Pair<Type, Object>> map) {
        this.map = Collections.synchronizedMap(map);
    }

    protected BaseBundle() {
        this(new HashMap<>());
    }

    /**
     * Clear the mapping contained in the Bundle.
     */
    public void clear() {
        map.clear();
    }

    /**
     * @return True if the bundle has no mappings.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @param key The mapping key to check.
     * @return True if the key is contained in the bundle.
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * @return A set of the keys contained in the bundle.
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Put all values from provided persistable bundle into this.
     *
     * @param bundle Persistable bundle to get values from.
     */
    public void putAll(PersistableBundle bundle) {
        map.putAll(bundle.map);
    }

    /**
     * @param key Remove mapping for this key.
     */
    public void remove(String key) {
        map.remove(key);
    }

    /**
     * @return Number of mappings in the bundle.
     */
    public int size() {
        return map.size();
    }

    /**
     * @param key The key to get mapping value for.
     * @return The mapping value or null if no mapping exists for this key.
     */
    public Object get(String key) {
        Pair<Type, Object> entry = map.get(key);
        if (entry != null) {
            return entry.second;
        }
        return null;
    }

    boolean getBoolean(String key, boolean defaultValue) {
        return get(key, Type.BOOLEAN, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        put(key, Type.BOOLEAN, value);
    }

    public boolean[] getBooleanArray(String key) {
        return get(key, Type.BOOLEAN_ARRAY, null);
    }

    public void putBooleanArray(String key, boolean[] array) {
        put(key, Type.BOOLEAN_ARRAY, array);
    }

    public double getDouble(String key, double defaultValue) {
        return get(key, Type.DOUBLE, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public void putDouble(String key, double value) {
        put(key, Type.DOUBLE, value);
    }

    public double[] getDoubleArray(String key) {
        return get(key, Type.DOUBLE_ARRAY, null);
    }

    public void putDoubleArray(String key, double[] array) {
        put(key, Type.DOUBLE_ARRAY, array);
    }

    public int getInt(String key, int defaultValue) {
        return get(key, Type.INT, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public void putInt(String key, int value) {
        put(key, Type.INT, value);
    }

    public int[] getIntArray(String key) {
        return get(key, Type.INT_ARRAY, null);
    }

    public void putIntArray(String key, int[] array) {
        put(key, Type.INT_ARRAY, array);
    }

    public long getLong(String key, long defaultValue) {
        return get(key, Type.LONG, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public void putLong(String key, long value) {
        put(key, Type.LONG, value);
    }

    public long[] getLongArray(String key) {
        return get(key, Type.LONG_ARRAY, null);
    }

    public void putLongArray(String key, long[] array) {
        put(key, Type.LONG_ARRAY, array);
    }

    public String getString(String key, String defaultValue) {
        return get(key, Type.STRING, defaultValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public void putString(String key, String value) {
        put(key, Type.STRING, value);
    }

    public String[] getStringArray(String key) {
        return get(key, Type.STRING_ARRAY, null);
    }

    public void putStringArray(String key, String[] array) {
        put(key, Type.STRING_ARRAY, array);
    }

    protected <T> T get(String key, Type type, T defaultValue) {
        Pair<Type, Object> val = map.get(key);
        if (val != null && type.equals(val.first)) {
            return (T) val.second;
        }
        return defaultValue;
    }

    protected void put(String key, Type type, Object entry) {
        map.put(key, Pair.create(type, entry));
    }

    protected boolean writeToParcel(Parcel dest, String key, Type type, Object value) {
        dest.writeString(key);
        dest.writeInt(type.ordinal());
        if (value == null) {
            dest.writeByte((byte)0);
            return true;
        }
        dest.writeByte((byte) 1);

        switch (type) {
            case BOOLEAN:
                dest.writeByte((byte) (((Boolean) value) ? 1 : 0));
                return true;
            case BOOLEAN_ARRAY:
                dest.writeBooleanArray((boolean[]) value);
                return true;
            case DOUBLE:
                dest.writeDouble((Double) value);
                return true;
            case DOUBLE_ARRAY:
                dest.writeDoubleArray((double[]) value);
                return true;
            case INT:
                dest.writeInt((Integer) value);
                return true;
            case INT_ARRAY:
                dest.writeIntArray((int[]) value);
                return true;
            case LONG:
                dest.writeLong((Long) value);
                return true;
            case LONG_ARRAY:
                dest.writeLongArray((long[]) value);
                return true;
            case STRING:
                dest.writeString((String) value);
                return true;
            case STRING_ARRAY:
                dest.writeStringArray((String[]) value);
                return true;
            default:
                return false;
        }
    }

    protected boolean readFromParcel(Parcel source, String key, Type type) {
        if (Type.UNKNOWN.equals(type)) {
            throw new ParcelFormatException("Unknown value type.");
        }

        if (source.readByte() == 0) {
            put(key, type, null);
            return true;
        }

        switch (type) {
            case BOOLEAN:
                putBoolean(key, source.readByte() > 0);
                return true;
            case BOOLEAN_ARRAY:
                putBooleanArray(key, source.createBooleanArray());
                return true;
            case DOUBLE:
                putDouble(key, source.readDouble());
                return true;
            case DOUBLE_ARRAY:
                putDoubleArray(key, source.createDoubleArray());
                return true;
            case INT:
                putInt(key, source.readInt());
                return true;
            case INT_ARRAY:
                putIntArray(key, source.createIntArray());
                return true;
            case LONG:
                putLong(key, source.readLong());
                return true;
            case LONG_ARRAY:
                putLongArray(key, source.createLongArray());
                return true;
            case STRING:
                putString(key, source.readString());
                return true;
            case STRING_ARRAY:
                putStringArray(key, source.createStringArray());
                return true;
            default:
                return false;
        }
    }

    protected enum Type {
        UNKNOWN,

        // --- BaseBundle
        BOOLEAN,
        BOOLEAN_ARRAY,
        DOUBLE,
        DOUBLE_ARRAY,
        INT,
        INT_ARRAY,
        LONG,
        LONG_ARRAY,
        STRING,
        STRING_ARRAY,

        // --- PersistableBundle
        PERSISTABLE_BUNDLE,

        // --- Bundle
        BUNDLE,
        BYTE,
        BYTE_ARRAY,
        CHAR,
        CHAR_ARRAY,
        CHAR_SEQUENCE,
        CHAR_SEQUENCE_ARRAY,
        FLOAT,
        FLOAT_ARRAY,
        INT_ARRAY_LIST,
        PARCELABLE,
        PARCELABLE_ARRAY,
        PARCELABLE_ARRAY_LIST,
        SERIALIZABLE,
        SHORT,
        SHORT_ARRAY,
        STRING_ARRAY_LIST,
        ;

        public static Type valueOf(int ordinal) {
            for (Type type : values()) {
                if (type.ordinal() == ordinal) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
