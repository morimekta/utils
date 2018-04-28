---
layout: page
title: "About Morimekta Utilities"
---

Morimekta utilities is a collection of utility libraries for java. Ranging
in functionality from emulating android libraries to handling high performance
IO operations.

A lot of the libraries here sprung out from the needs of my other open source
libraries, or tasks that I felt was needed but not prioritized at my workplace.
It has been programmed mainly in my spare time. The main users of the
utilities libraries are:

* [providence](http://www.morimekta.net/providence/): A pure java based
  replacement for [Apache Thrift](https://thrift.apache.org/).
* [idltool](https://github.com/morimekta/idltool): A java based port and
  functionality-cleanup of Über's node.js based [idl](https://github.com/uber-node/idl)
  tool.

The project contains a wide variety of modules, from `io-util`, which is
more akin to the commons-lang library of apache. To `android-util` which
emulates a number of core libraries used by android related to I/O and
storage.