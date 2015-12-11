package android.os;

public interface Parcelable {
    /**
     * Bit masks for use with describeContents(): each bit represents a kind of
     * object considered to have potential special significance when
     * marshalled.
     */
    static final int CONTENTS_FILE_DESCRIPTOR      = 0x00000001;

    /**
     * Flag for use with writeToParcel(Parcel, int): the object being written
     * is a return value, that is the result of a function such as "Parcelable
     * someFunction()", "void someFunction(out Parcelable)", or "void
     * someFunction(inout Parcelable)".
     */
    static final int PARCELABLE_WRITE_RETURN_VALUE = 0x00000001;

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     */
    int describeContents();

    /**
     * Flatten this object in to a Parcel.
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
         */
        P createFromParcel(Parcel source);

        /**
         * Create a new array of the Parcelable class.
         */
        P[] newArray(int size);
    }

    /**
     * Specialization of Parcelable.Creator that allows you to receive the
     * ClassLoader the object is being created in.
     */
    interface ClassLoaderCreator<P> extends Creator<P> {
        /**
	 * Create a new instance of the Parcelable class, instantiating it from
	 * the given Parcel whose data had previously been written by
	 * Parcelable.writeToParcel() and using the given ClassLoader.
         */
        P createFromParcel(Parcel source, ClassLoader loader);
    }
}
