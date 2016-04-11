IO Utilities
============

Utilities for facilitating heavy computational IO, specifically reading and
writing typed data to IO streams.

## Components

Notable components of the IO utilities.

* **JSON**: Optomized JSON `reader` and `writer`. These classes
  uses the JSON syntax, but does not enforce complete JSON compatibility. They
  are optimized for heavy IO operations (e.g. heavy-duty serialization systems
  that know more about it's content than a generic JsonParser or formatter
  can).
* **Binary**: Binary `reader` and `writer` that can write typed binary data to
  IOStream and read it back. Contains utilities for reading and writing lots of
  various binary formats, including base128 varint, zigzag encoded varints (the
  zigzag encoding uses the same format as ProtoBuffers).
* **IndentedPrintWriter**: PrintWriter that prints consistently indented lines.
  Note that newlines works a bit different than with "traditional" print
  writers.
* **Utf8StreamReader**: A non-buffering Utf8 decoding stream reader.
* **Base64**: Compact base64 encoding /
  decoding utility for encoding and decoding base64 with as little as possible
  overhead as possible. Only supports encoding to non-wrapped non-padded
  standard base64, but can deserialize any combination of base64 and url-safe
  base64, including padding, spaces and line wrapping.
* **Binary** and **Slice** are binary data holder classes.
  Binary is an immutable byte[] holder, and Slice is a no-copy "part of byte[]
  array" reference holder that can be compared and hashed.
* **Strings**: Extra string utilities.
