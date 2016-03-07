Testing Utilities
=================

* [ExtraMatchers](java/net/morimekta/testing/ExtraMatchers.java) Extra matcher
  creator methods to use in tests. See `java/net/morimekta/testing/matchers` for
  specific implementations.
* [FakeClock](java/net/morimekta/testing/tim/FakeClock.java) A fake java.time
  clock that can be controlled directly in the test (use this if you need to
  change the time dynamically during the test. Otherwise use the
  `Clock.fixed(Instant, ZoneId)` fixed clock.
