/*
 * Copyright (c) 2016, Stein Eldar johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package android.os;

import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Main bundle class. A bundle is a type-safe string to object map.
 *
 * The Bundle collection contains more data types than PersistableBundle.
 *
 * See also {@link PersistableBundle} and {@link BaseBundle}.
 */
@SuppressWarnings("unused")
public final class Bundle extends BaseBundle implements Parcelable, Cloneable {
    public Bundle() {
        this(null, kDefaultSize);
    }

    public Bundle(ClassLoader loader) {
        this(loader, kDefaultSize);
    }

    public Bundle(int capacity) {
        this(null, capacity);
    }

    public Bundle(PersistableBundle bundle) {
        this(null, bundle.size());
        map.putAll(bundle.map);
    }

    public Bundle(Bundle bundle) {
        this(bundle.loader, bundle.size());
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

    @Override
    public Object clone() {
        return new Bundle(this);
    }

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
            if (isWriting) {
                throw new BadParcelableException("Trying to write with circular references.");
            }
            try {
                isWriting = true;
                dest.writeInt(size());
                for (Map.Entry<String, Pair<Type, Object>> entry : map.entrySet()) {
                    if (!writeToParcel(dest, entry.getKey(), entry.getValue().first, entry.getValue().second)) {
                        Object value = entry.getValue().second;
                        switch (entry.getValue().first) {
                            case BUNDLE: {
                                Bundle b = cast(value);
                                b.writeToParcel(dest, flags);
                                break;
                            }
                            case BYTE:
                                dest.writeByte((byte) cast(value));
                                break;
                            case BYTE_ARRAY:
                                dest.writeByteArray((byte[]) cast(value));
                                break;
                            case CHAR:
                                dest.writeInt((int) (char) ((Character) value));
                                break;
                            case CHAR_ARRAY:
                                dest.writeCharArray((char[]) cast(value));
                                break;
                            case CHAR_SEQUENCE:
                                dest.writeString(value == null ? null : value.toString());
                                break;
                            case CHAR_SEQUENCE_ARRAY: {
                                CharSequence[] sequences = cast(value);
                                String[] strings = new String[sequences.length];
                                for (int i = 0; i < sequences.length; ++i) {
                                    strings[i] = sequences[i] == null ? null : sequences[i].toString();
                                }
                                dest.writeStringArray(strings);
                                break;
                            }
                            case FLOAT:
                                dest.writeFloat((float) cast(value));
                                break;
                            case FLOAT_ARRAY:
                                dest.writeFloatArray((float[]) cast(value));
                                break;
                            case INT_ARRAY_LIST: {
                                ArrayList<Integer> list = cast(value);
                                int[] ints = new int[list.size()];
                                for (int i = 0; i < list.size(); ++i) {
                                    ints[i] = list.get(i);
                                }
                                dest.writeIntArray(ints);
                                break;
                            }
                            case PARCELABLE:
                                dest.writeParcelable((Parcelable) cast(value), 0);
                                break;
                            case PARCELABLE_ARRAY:
                                dest.writeParcelableArray((Parcelable[]) cast(value), 0);
                                break;
                            case PARCELABLE_ARRAY_LIST: {
                                @SuppressWarnings("unchecked")
                                ArrayList<Parcelable> alp = cast(value);
                                dest.writeParcelableArray(alp.toArray(new Parcelable[alp.size()]), 0);
                                break;
                            }
                            case SERIALIZABLE:
                                try {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                                    oos.writeObject(value);
                                    oos.flush();
                                    dest.writeByteArray(baos.toByteArray());
                                } catch (IOException e) {
                                    throw new BadParcelableException(
                                            "Value at key " + entry.getKey() + " is not serializable.");
                                }
                                break;
                            case SHORT:
                                dest.writeInt((Short) value);
                                break;
                            case SHORT_ARRAY: {
                                short[] shorts = cast(value);
                                int[] ints = new int[shorts.length];
                                for (int i = 0; i < shorts.length; ++i) {
                                    ints[i] = shorts[i];
                                }
                                dest.writeIntArray(ints);
                                break;
                            }
                            case STRING_ARRAY_LIST:
                                ArrayList<String> als = cast(value);
                                dest.writeStringArray(als.toArray(new String[als.size()]));
                                break;
                            default:
                                throw new BadParcelableException(
                                        "Unknown type for bundle serialization " + entry.getValue().first);
                        }
                    }
                }
            } finally {
                isWriting = false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) {
        return (T) o;
    }

    public static Creator<Bundle> CREATOR = new ClassLoaderCreator<Bundle>() {
        @Override
        public Bundle createFromParcel(Parcel source, ClassLoader loader) {
            final int size = source.readInt();
            Bundle bundle = new Bundle(loader, size);
            for (int i = 0; i < size; ++i) {
                String key = source.readString();
                Type type = Type.valueOf(source.readInt());
                if (!bundle.readFromParcel(source, key, type)) {
                    switch (type) {
                        case BUNDLE:
                            bundle.put(key, type, createFromParcel(source));
                            break;
                        case BYTE:
                            bundle.put(key, type, source.readByte());
                            break;
                        case BYTE_ARRAY:
                            bundle.put(key, type, source.createByteArray());
                            break;
                        case CHAR:
                            bundle.put(key, type, (char) source.readInt());
                            break;
                        case CHAR_ARRAY:
                            bundle.put(key, type, source.createCharArray());
                            break;
                        case CHAR_SEQUENCE:
                            bundle.put(key, type, source.readString());
                            break;
                        case CHAR_SEQUENCE_ARRAY: {
                            String[] strings = source.createStringArray();
                            CharSequence[] out = new CharSequence[strings.length];
                            System.arraycopy(strings, 0, out, 0, strings.length);
                            bundle.put(key, type, out);
                            break;
                        }
                        case FLOAT:
                            bundle.put(key, type, source.readFloat());
                            break;
                        case FLOAT_ARRAY:
                            bundle.put(key, type, source.createFloatArray());
                            break;
                        case INT_ARRAY_LIST: {
                            int[] ints = source.createIntArray();
                            ArrayList<Integer> out = new ArrayList<>(ints.length);
                            for (int v : ints) {
                                out.add(v);
                            }
                            bundle.put(key, type, out);
                            break;
                        }
                        case PARCELABLE:
                            bundle.put(key, type, source.readParcelable(loader));
                            break;
                        case PARCELABLE_ARRAY:
                            bundle.put(key, type, source.readParcelableArray(loader));
                            break;
                        case PARCELABLE_ARRAY_LIST: {
                            ArrayList<Parcelable> out = new ArrayList<>();
                            Collections.addAll(out, source.readParcelableArray(loader));
                            bundle.put(key, type, out);
                            break;
                        }
                        case SERIALIZABLE:
                            try {
                                ByteArrayInputStream bais = new ByteArrayInputStream(source.createByteArray());
                                ObjectInputStream ios = new ObjectInputStream(bais);
                                bundle.put(key, type, ios.readObject());
                                ios.close();
                            } catch (IOException ie) {
                                throw new ParcelFormatException("IOException: " + ie.getMessage());
                            } catch (ClassNotFoundException e) {
                                throw new ParcelFormatException("ClassNotFoundException: " + e.getMessage());
                            }
                            break;
                        case SHORT:
                            bundle.put(key, type, (short) source.readInt());
                            break;
                        case SHORT_ARRAY: {
                            int[] ints = source.createIntArray();
                            short[] out = new short[ints.length];
                            for (int j = 0; j < ints.length; ++j) {
                                out[j] = (short) ints[j];
                            }
                            bundle.put(key, type, out);
                            break;
                        }
                        case STRING_ARRAY_LIST: {
                            String[] strings = source.createStringArray();
                            ArrayList<String> out = new ArrayList<>(strings.length);
                            Collections.addAll(out, strings);
                            bundle.put(key, type, out);
                            break;
                        }
                        default:
                            throw new ParcelFormatException(
                                    "Unknown type for bundle deserialization " + type);
                    }
                }
            }
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

    // --- PRIVATE AFTER HERE ---

    private static final int kDefaultSize = 1 << 8;

    private Bundle(Map<String, Pair<Type, Object>> map) {
        super(map);
        this.loader = ClassLoader.getSystemClassLoader();
    }

    private Bundle(ClassLoader loader, int capacity) {
        super(capacity);
        this.loader = loader != null ? loader : ClassLoader.getSystemClassLoader();
    }

    private boolean isWriting = false;
    private final ClassLoader loader;
}
