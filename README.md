Android Utilities
=================

This is a set of android classes ported to work as a stand-alone library. The API
interfaces should be identical to that of the android classes, but uses only pure
java and java bindings to work. This way it can be linked in non-android projects
to act as a framework for testing of android utility libraries without depending
on the whole android SDK. 

## Interfaces

* [android.os.Parcelable](src/android/os/Parcelable.java)

## Classes

* [android.os.Parcel](src/android/os/Parcel.java)
* [android.os.BaseBundle](src/android/os/BaseBundle.java)
* [android.os.Bundle](src/android/os/Bundle.java)
* [android.os.ParcelUuid](src/android/os/ParcelUuid.java)
* [android.os.PersistableBundle](src/android/os/PersistableBundle.java)
* [android.util.Pair](src/android/util/Pair.java)

## Exceptions

* [android.os.BadParcelableException](src/android/os/BadParcelableException.java)
* [android.os.ParcelFormatException](src/android/os/ParcelFormatException.java)
* [android.util.AndroidException](src/android/util/AndroidException.java)
* [android.util.AndroidRuntimeException](src/android/util/AndroidRuntimeException.java)
