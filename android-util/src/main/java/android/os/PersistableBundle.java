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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A simpler variant of {@link Bundle} that only contains a few basic types
 * and itself.
 */
@SuppressWarnings("unused")
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
                        if (entry.getValue().first.equals(Type.PERSISTABLE_BUNDLE)) {
                            PersistableBundle inner = (PersistableBundle) entry.getValue().second;
                            inner.writeToParcel(dest, flags);
                        } else {
                            throw new BadParcelableException(
                                    "Unknown type for persistable bundle serialization " + entry.getValue().first);
                        }
                    }
                }
            } finally {
                isWriting = false;
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

    private boolean isWriting = false;
}
