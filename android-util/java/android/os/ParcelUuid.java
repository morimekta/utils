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

import java.util.Objects;
import java.util.UUID;

/**
 * Parcelable wrapper around the {@link java.util.UUID} class.
 */
public class ParcelUuid implements Parcelable {
    public ParcelUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public static ParcelUuid fromString(String uuid) {
        return new ParcelUuid(UUID.fromString(uuid));
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ParcelUuid)) return false;
        ParcelUuid other = (ParcelUuid) o;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return Objects.toString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }

    public static final Creator<ParcelUuid> CREATOR = new Creator<ParcelUuid>() {
        @Override
        public ParcelUuid createFromParcel(Parcel source) {
            return fromString(source.readString());
        }

        @Override
        public ParcelUuid[] newArray(int size) {
            return new ParcelUuid[size];
        }
    };

    private final UUID uuid;
}
