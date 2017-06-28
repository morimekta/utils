Android Utilities
=================

This is a set of android classes ported to work as a stand-alone library. The
API interfaces should be identical to that of the android classes, but uses only
pure java and java bindings to work. This way it can be linked in non-android
projects to act as a framework for testing of android utility libraries without
depending on the whole android SDK.

It contains a set of mirrored data-classes from the `android.os` and
`android.util` packages. These classes are useful for data-keeping and
transferring, so is nice to have available (also with real functionality)
when testing common android libraries using these without including large
systems like `robolectric`, or having it to run on a real android phone.
