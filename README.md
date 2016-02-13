Android Utilities
=================

This is a set of android classes ported to work as a stand-alone library. The API
interfaces should be identical to that of the android classes, but uses only pure
java and java bindings to work. This way it can be linked in non-android projects
to act as a framework for testing of android utility libraries without depending
on the whole android SDK. 

## Interfaces

* [android.os.Parcelable](java/android/os/Parcelable.java) Parcelable class interface.

## Classes

* [android.os.BaseBundle](java/android/os/BaseBundle.java) Base class for Bundle and PersistableBundle.
* [android.os.Bundle](java/android/os/Bundle.java) Type safe map with parcel support.
* [android.os.Parcel](java/android/os/Parcel.java) Object serializer.
* [android.os.ParcelUuid](java/android/os/ParcelUuid.java) UUID wrapper for Parcel.
* [android.os.PersistableBundle](java/android/os/PersistableBundle.java) Bundle with persistable values.
* [android.util.Base64](java/android/util/Base64.java) Base64 utility class.
* [android.util.Base64InputStream](java/android/util/Base64InputStream.java) Base64 decoding input stream.
* [android.util.Base64OutputStream](java/android/util/Base64OutputStream.java) Base64 encoding output stream.
* [android.util.Pair](java/android/util/Pair.java) Immutable pair utility.

## Exceptions

* [android.os.BadParcelableException](java/android/os/BadParcelableException.java) Object contains data not suitable for parceling.
* [android.os.ParcelFormatException](java/android/os/ParcelFormatException.java) Parcel contains data not compatible with deserializing parcelable.
* [android.util.AndroidException](java/android/util/AndroidException.java) Base android exception.
* [android.util.AndroidRuntimeException](java/android/util/AndroidRuntimeException.java) Base android runtime exception.

# Contributors

* [Stein Eldar Johnsen](http://www.github.com/morimekta) Main contributor and maintainer of Android Utilities.
