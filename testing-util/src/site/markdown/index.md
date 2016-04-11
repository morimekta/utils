Testing Utilities
=================

Testing Utilities. Includes a `FakeClock` java.time clock implementation that
can be on-demand controlled by the test method millisecond my millisecond (as
opposed to the static `Clock.fixed(...)` that never will return a different
time instant), and an extra set of standard hamcrest matchers in
`ExtraMatchers`:

* `isEqualIgnoreIndent`: Same as isEqualIgnoreSpaces, but only ignores
  differences in spaces before the first printable character on *each line*. All
  other space chars must match.
* `isInRange`: checks that the number is in a specific numeric range.
