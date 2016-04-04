Android Utilities
=================

This is a set of android classes ported to work as a stand-alone library. The API
interfaces should be identical to that of the android classes, but uses only pure
java and java bindings to work. This way it can be linked in non-android projects
to act as a framework for testing of android utility libraries without depending
on the whole android SDK. 

## Interfaces

* [android.os.Parcelable](src/main/java/android/os/Parcelable.java) Parcelable class interface.

## Classes

* [android.os.BaseBundle](src/main/java/android/os/BaseBundle.java) Base class for Bundle and PersistableBundle.
* [android.os.Bundle](src/main/java/android/os/Bundle.java) Type safe map with parcel support.
* [android.os.Parcel](src/main/java/android/os/Parcel.java) Object serializer.
* [android.os.ParcelUuid](src/main/java/android/os/ParcelUuid.java) UUID wrapper for Parcel.
* [android.os.PersistableBundle](src/main/java/android/os/PersistableBundle.java) Bundle with persistable values.
* [android.util.Base64](src/main/java/android/util/Base64.java) Base64 utility class.
* [android.util.Base64InputStream](src/main/java/android/util/Base64InputStream.java) Base64 decoding input stream.
* [android.util.Base64OutputStream](src/main/java/android/util/Base64OutputStream.java) Base64 encoding output stream.
* [android.util.Pair](src/main/java/android/util/Pair.java) Immutable pair utility.

## Exceptions

* [android.os.BadParcelableException](src/main/java/android/os/BadParcelableException.java) Object contains data not suitable for parceling.
* [android.os.ParcelFormatException](src/main/java/android/os/ParcelFormatException.java) Parcel contains data not compatible with deserializing parcelable.
* [android.util.AndroidException](src/main/java/android/util/AndroidException.java) Base android exception.
* [android.util.AndroidRuntimeException](src/main/java/android/util/AndroidRuntimeException.java) Base android runtime exception.
