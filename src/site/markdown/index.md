Morimekta Utilities
===================

Morimekta utilities is a collection of utility libraries for java. Ranging
in functionality from emulating android libraries to handling high performance
IO operations.

## Android Util

Library containing a set of mirrored data-classes from the `android.os` and
`android.util` packages. These classes are useful for data-keeping and
transferring, so is nice to have available (also with real functionality)
when testing common android libraries using these without including large
systems like `robolectric`, or having it to run in a real phone.

## Config Util

Library for handling config files that are deeply structured in a simple and
type-safe manner.

## Console Util

Library for handling advanced console (TTY) input and output. It is designed
for unix style terminals with interactive keyboard interface, not web or
GUI interface.

## Diff Util

Utility classes for diffing text.

## IO Util

Libraries for handling binary, JSON and human readable input and output. Only
the "human readable input" is kept in the `console-util` module.

## Testing Util

Library with test-related classes. Extra hamcrest matchers, and some fake
system classes to help with advanced testing.
