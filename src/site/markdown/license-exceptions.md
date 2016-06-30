Copyright / License Exceptions
==============================

The copyright holder is whoever contributed to each file, as noted in the
Copyright portion of the file. If the file does not have a Copyright (c)
notice, or the copyright section does not refer to the author(s) of the
project, then this project does **not** claim copyright over that file.

From [iharder/base64](http://iharder.sourceforge.net/current/java/base64/)
library (public domain, unlicensed):

* `android.util.Base64`: Modified to match the android version of the interface.
* `android.util.Base64OutputStream`: Separated from Base64 (was internal class),
  and modified to match android version of the interface.
* `android.util.Base64InputStream`: Separated from Base64 (was internal class),
                                    and modified to match android version of the interface.
* `net.morimekta.util.Base64`: Stripped to bare minimum to encode and decode
  standard base64.

From Android OSS project (Apache 2.0 licensed):

* `android.util.Base64Test`: Mainly unmodified from original, from old android
  open sourced code (2010).
* `net.morimekta.diff.*`: Comes from diff-match-patch, `(c) Google 2012`, a
  pretty old but useful library for diffing files. Repackaged since the library
  is almost un-usable without some refactoring.
