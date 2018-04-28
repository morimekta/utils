---
layout: page
toc_title: "Licenses"
title: "Copyright / Licenses and Exceptions"
category: doc
date: 2018-01-01 12:00:00
order: 1
---

Most of this project is licensed under the [Apache 2.0](http://www.apache.org/licenses/)
license, and copyrighted by the Morimekta Utils Authord, but there are some
exceptions, as the code has been copied from other Open Source projects and
adapted for modern java uses.

So the copyright holder is whoever contributed to each file, as noted in the
Copyright portion of the file. If the file does not have a Copyright (c)
notice, or the copyright section does not refer to the author(s) of the
project, then this project does **not** claim copyright over that file.

From [iharder/base64](http://iharder.sourceforge.net/current/java/base64/)
library (public domain, unlicensed):

* `android.util.Base64`: Modified to match the android version of the interface.
* `android.util.Base64OutputStream`: Separated from Base64 (was internal class),
  and modified to match android version of the interface.
* `android.util.Base64InputStream`: Separated from Base64 (was internal class),
                                    and modified to match android version of the
                                    interface.
* `net.morimekta.util.Base64`: Stripped to bare minimum to encode and decode
  standard base64.

Other imported code pieces (Apache 2.0 licensed):

* `android.util.Base64Test`: Mainly unmodified from original, from old android
  open sourced code (2010).
* `net.morimekta.diff.*`: Comes from diff-match-patch, `(c) Google 2012`, a
  pretty old but useful library for diffing files or simple string content.
  Repackaged since the library was almost unusable without some refactoring.
  Valid for `Bisect`, `Change`, `Diff`, `DiffBase`, `DiffOptions`,
  `LinesToCharsResult` classes and the `Operation` enum.
