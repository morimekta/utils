IO Utilities
============

Utilities for facilitating heavy computational IO, specifically reading and
writing typed data to IO streams.

## Components

Notable components of the IO utilities.

* *JSON* Optomized JSON [reader](java/net/morimekta/util/json/JsonTokenizer.java)
  and [writer](java/net/morimekta/util/json/JsonWriter.java). These classes
  uses the JSON syntax, but does not enforce complete JSON comatiblity. They
  are optimized for heavy IO operations (e.g. heavy-duty serialization systems
  that know more about it's content than a generic JsonParser or formatter
  can).
* *Binary* Binary [reader](java/net/morimekta/util/io/BinaryReader.java) and
  [writer](java/net/morimekta/util/io/BinaryWriter.java) that can write typed
  binary data to IOStream and read it back. Contains utilities for reading and
  writing lots of various binary formats, including base128 varint, zigzag
  encoded varints (the zigzag encoding uses the same format as ProtoBuffers).
* *[IndentedPrintWriter](java/net/morimekta/util/io/IndentedPrintWriter.java)*
  PrintWriter that prints consistently indented lines.
* *[Utf8StreamReader](java/net/morimekta/util/io/Utf8StreamReader.java)*
  A non-buffering Utf8 decoding stream reader.
* *[Base64](java/net/morimekta/util/Base64.java)* compact base64 encoding /
  decoding utility for encoding and decoding base64 with as little as possible
  overhead as possible. Only supports encoding to non-wrapped non-padded
  standard base64, but can deserialize any combination of base64 and url-safe
  base64.
* *[Binary](java/net/morimekta/util/Binary.java)* and
  *[Slice](java/net/morimekta/util/Slice.java)* are binary data holder classes.
  Binary is an immutable byte[] holder, and Slice is a no-copy "part of byte[]
  array" reference holder that can be compared and hashed.
* *[Strings](java/net/morimekta/util/Strings.java)* Extra string utilities.
