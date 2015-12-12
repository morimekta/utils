package android.os;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import android.util.Pair;

public final class PersistableBundle
        extends BaseBundle
        implements Parcelable {
    public PersistableBundle(int capacity) {
        super(capacity);
    }

    public PersistableBundle() {
        this(0);
    }

    public PersistableBundle(PersistableBundle bundle) {
        this(bundle.size());
        putAll(bundle);
    }

    public Object clone() {
        return new PersistableBundle(this);
    }

    public PersistableBundle getPersistableBundle(String key) {
        Pair<Type, Object> entry = map.get(key);
        if (entry != null && entry.first.equals(Type.PERSISTABLE_BUNDLE)) {
            return (PersistableBundle) entry.second;
        }
        return null;
    }

    public void putPersistableBundle(String key, PersistableBundle bundle) {
        put(key, Type.PERSISTABLE_BUNDLE, bundle);
    }

    @Override
    public String toString() {
        synchronized (map) {
            StringBuilder builder = new StringBuilder();
            builder.append("PersistableBundle(");
            boolean first = true;
            for (Map.Entry<String, Pair<Type, Object>> entry : map.entrySet()) {
                if (first)
                    first = false;
                else
                    builder.append(',');
                builder.append(entry.getKey())
                       .append("=")
                       .append(Objects.toString(entry.getValue().second));
            }
            builder.append(')');
            return builder.toString();
        }
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
                    if (entry.getValue().first.equals(Type.PERSISTABLE_BUNDLE)) {
                        PersistableBundle inner = (PersistableBundle) entry.getValue().second;
                        inner.writeToParcel(dest, flags);
                    } else {
                        throw new BadParcelableException(
                                "Unknown type for persistable bundle serialization " + entry.getValue().first);
                    }
                }
            }
        }
    }

    public static final Creator<PersistableBundle> CREATOR = new Creator<PersistableBundle>() {
        @Override
        public PersistableBundle createFromParcel(Parcel source) {
            final int size = source.readInt();
            PersistableBundle bundle = new PersistableBundle(size);
            for (int i = 0; i < size; ++i) {
                String key = source.readString();
                Type type = Type.valueOf(source.readInt());
                if (!bundle.readFromParcel(source, key, type)) {
                    if (Type.PERSISTABLE_BUNDLE.equals(type)) {
                        bundle.putPersistableBundle(key, createFromParcel(source));
                    } else {
                        throw new ParcelFormatException("Unknown type for persistable bundle deserialization " + type);
                    }
                }
            }
            return bundle;
        }

        @Override
        public PersistableBundle[] newArray(int size) {
            return new PersistableBundle[size];
        }
    };

    @SuppressWarnings("unchecked")
    public static final PersistableBundle EMPTY = new PersistableBundle(Collections.EMPTY_MAP);

    private PersistableBundle(Map<String, Pair<Type, Object>> map) {
        super(map);
    }
}
