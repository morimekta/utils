IO Utilities
============

Utilities for facilitating heavy computational IO, specifically reading and
writing typed data to IO streams. The library has grown over time and has
in later time become more of a `commons-lang` type library, which includes
a number of non-I/O but still pretty core language feature classes.

### Core Classes

The library adds a number of classes that helps with a number of tasks,
specifically to store data in memory in a truly immutable fashion, make
interfaces toward general data, and to handle locking and to handle base64
and other string encoding and decoding.

**Stringable** and **Numeric** interfaces are simple interfaces to be able
to mark a class as being able to make a descriptive string (not the same as
`toString()`) using the `asString()` method, and toward being integer-numeric
using the `asInteger()` method.

**Base64**: A port of the
[iharder base64 implementation](http://iharder.sourceforge.net/current/java/base64/)
which have been trimmed down to be as fast as possible while handling
the standard base64 format. It will also read URL-safe base64, but not
write it.

**Binary**: A class that keeps a byte array (a.k.a. `binary`) in an immutable
container. It is preferred to use over `ByteBuffer` as the latter is not
thread-safe, and even holds a read-position state that messed up uses of
the binary data. The `Binary` class is a pure immutable `byte[]` holder made to
solve that task specifically.

**Slice**: A class that keeps a reference to a slice of a byte array. It is
among others used in tokenizers (See `JsonTokenizer`) to refer to a portion
of the read data that is a single token without needing to copy that region
of bytes into the token itself too.

**Pair**: A pair of two values. Actually a copy of the `android.util.Pair`
class from the `android-util` module.

**FileWatcher**: A class that watches files and updates on them and notifies
it's listeners. _**PS**: May be moved to a different package in the future_.

**ExtraStreams**: A couple of utility streams based on range or repeating
something.

**ExtraCollectors**: A couple of utility collectors to help with properly
splitting a stream in batches or join strings together.

### I/O Related Classes

These are classes that revolves around pure reading and writing data, with
one general specialization: no unnecessary buffering whatsoever. Some of
the classes here already exists, but in versions that does internal buffering,
which is a problem if you e.g. need to read just a portion of an InputStream
using a StreamReader with utf-8 decoding.

**ByteBufferIO**: The byte buffer input and output streams are simply wrappers
of the ByteBuffer class that handled it for reading as input stream or output
stream.

**CountingOutputSteram**: Is a simple output stream wrapper that counts the
number of bytes written.

**Utf8StreamRW**: Are replacements for the standard `StreamReader` and
`StreamWriter` classes with the `utf-8` encoding that enforce a no-buffering
policy. Both the original reader and writer classes holds internal buffers
that makes them a little more efficient, but that can be problematic if
they do a lot of small jobs in a long stream instead of the whole.

**IndentedPrintWriter**: Is a print-writer that keeps track of and enforces
indentation. It works a little different than a standard `PrintWriter` with
regard to when newlines are enforced, but that is done to make it easier
to generate nicely indented code or data files.

**BinaryRW**: The `BinaryReader` and `BinaryWriter` and the two variants
`LittleEndian*` and `BigEndian*` of each are readers and writer classes meant
to handle pure binary encoded data consistently over any data stream. In
addition to being able to read and write BE / LE encoded numbers, and raw
byte data, it can handle base128 variable length encoded numbers and
even a zig-zag encoded variant thereof.

**IOUtils**: Contains extra utilities for handling reading from Input streams
skipping data consistently, and copying between streams.

### Concurrency and Execution

In the concurrency category are two notable classes: The `ProcessExecutor`
and the `ReentrantReadWriteMutex`.

**ProcessExecutor** is a class designed to simplify running external processes,
control the process' input, and get hold of various output.

**ReentrantReadWriteMutex** is a wrapper around the java concurrency
`ReentrantReadWriteLock` classes, that handled it in a way more similar to
how I want to work with locking in java.

### Json Parsing and Writing

Since JSON has become the norm of quite a lot of data serialization standards,
and is so ubiquitous (and efficient) in browsers, handling JSON well is really
important for anything that does serialization. And hence the `json` classes
of `io-util`. The two important classes to note are:

**JsonTokenizer** is a class that handles parsing a JSON file into distinct
tokens. Note that the current tokenizer does *not* check that the entire file
follows the JSON format, just that each *token* does so.

**JsonWriter** is a class that writes JSON on the go as objects, keys and values
are written. This class enforces that proper JSON structure is used.
