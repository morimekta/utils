Diff Utilities
==============

Utility classes for finding changes between strings. The main classes and uses
are:

- `Diff` finds diff between two strings. It is pretty advanced and can find minute
  differences in pretty long strings like data files etc. Returns a change set with
  equal, inserted and deleted parts of the text.
- `DiffLines` finds changes when comparing line-by-line only, not searching for changes
  within the lines themselves.
- `Bisect` is similar to `Diff`, but is limited to only using the `bisect` method, which
  makes it significantly faster when comparing smaller strings (less than a kilobyte),
  or content of files on very few very long lines.
