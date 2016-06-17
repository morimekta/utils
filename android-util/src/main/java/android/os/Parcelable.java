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

/**
 * Interface for classes whose instances can be written to and restored from a
 * {@link Parcel}. Classes implementing the Parcelable interface must also have a
 * non-null static field called <code>CREATOR</code> of a type that implements
 * the {@link Parcelable.Creator} interface.
 *
 * See <a href="http://developer.android.com/reference/android/os/Parcelable.html">android.os.Parcelable</a>
 * documentation for reference.
 */
public interface Parcelable {
    /**
     * Bit masks for use with describeContents(): each bit represents a kind of
     * object considered to have potential special significance when
     * marshaled.
     */
    int CONTENTS_FILE_DESCRIPTOR = 0x00000001;

    /**
     * Flag for use with writeToParcel(Parcel, int): the object being written
     * is a return value, that is the result of a function such as "Parcelable
     * someFunction()", "void someFunction(out Parcelable)", or "void
     * someFunction(inout Parcelable)".
     */
    int PARCELABLE_WRITE_RETURN_VALUE = 0x00000001;

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshaled representation.
     *
     * @return Bitmask of CONTENTS_* flags.
     */
    int describeContents();

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest The parcelable to write to.
     * @param flags Bitmask of PARCELABLE_WRITE_* flags.
     */
    void writeToParcel(Parcel dest, int flags);

    /**
     * Creator class.
     */
    interface Creator<P> {
        /**
         * Create a new instance of the Parcelable class, instantiating it from
         * the given Parcel whose data had previously been written by
         * Parcelable.writeToParcel().
         *
         * @param source Parcel to read from.
         * @return The created object.
         */
        P createFromParcel(Parcel source);

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size The number of elements.
         * @return The object array.
         */
        P[] newArray(int size);
    }

    /**
     * Specialization of Parcelable.Creator that allows you to receive the
     * ClassLoader the object is being created in.
     */
    interface ClassLoaderCreator<P>
            extends Creator<P> {
        /**
         * Create a new instance of the Parcelable class, instantiating it from
         * the given Parcel whose data had previously been written by
         * Parcelable.writeToParcel() and using the given ClassLoader.
         *
         * @param source Parcel to read from.
         * @param loader The classloader to use to load instance classes.
         * @return The created object.
         */
        P createFromParcel(Parcel source, ClassLoader loader);
    }
}
