Morimekta Utilities
===================

Various utilities maintained and primarily developed by @morimekta, a.k.a.
Stein Eldar Johnsen. Contributions are welcome.

## Utilities

* [Android Utilities](utils-android/README.md) Utilities for developing
  android-integrated libraries that can be tested without requiring testing on
  android devices or heavy testing frameworks like robolectric.
* [Console Utilities](utils-console/README.md) Utilities for facilitating
  interactive command line interfaces.
* [IO Utilities](utils-io/README.md) Utilities for facilitating heavy
  computational IO, specifically reading and writing typed data to IO streams.
* [Testing Utilities](utils-testing/README.md) Extra matchers and other
  utilities needed for testing complex java 8 apps.

## Contributors / Authors

* [Stein Eldar Johnsen](http://www.github.com/morimekta) Main contributor and maintainer.

## License

The project is mainly licensed under the [Apache 2.0](LICENSE) license.
Exceptions are always noted in each file that differs.

### Copyright / License Exceptions

The copyright holder is whoever contributed to each file, as noted in the
Copyright portion of the file. If the file does not have a Copyright (c)
notice, or the copyright section does not refer to the author(s) of the
project, then this project does **not** claim copyright over that file.

From [iharder/base64](http://iharder.sourceforge.net/current/java/base64/)
library (public domain, unlicensed):

* [android.util.Base64](utils-android/java/android/util/Base64.java)
* [android.util.Base64OutputStream](utils-android/java/android/util/Base64OutputStream.java) 
* [android.util.Base64InputStream](utils-android/java/android/util/Base64InputStream.java) 
* [net.morimekta.util.Base64](utils-io/java/net/morimekta/util/Base64.java) 

From Android OSS project (Apache 2.0 licensed):

* [android.util.Base64Test](utils-android/javatests/android/util/Base64Test.java)

## Release Process

Follow the steps described [here](RELEASE.md).
