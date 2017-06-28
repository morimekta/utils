Testing Utilities
=================

This module contains a number of utilities and helpers to help with testing.
It is based on the [junit 4](http://junit.org/junit4/) paradigms, but may be
updated to work with [junit 5](http://junit.org/junit5/) when that is out and
stable. It consists of a number of mostly independent parts.

### Concurrency and Time

In the concurrency and time group you have the `FakeClock` and two fake
`ExecutorService` implementations. These are designed so that you can modify
the clock as time goes on, and that will in turn trigger the executor to do
scheduled tasks. E.g.:

```java
class MyTest {
    private FakeClock clock;
    private FakeScheduledExecutor executor;

    @Before
    public void setUp() {
        clock = new FakeClock();
        executor = new FakeScheduledExecutor(clock);
    }

    @Test
    public void testSomethingScheduled() {
        Something something = new Something();
        executor.schedule(something, 10, TimeUnit.SECONDS);

        assertThat(something.isComplete(), is(false));

        clock.tick(10, TimeUnit.SECONDS);

        assertThat(something.isComplete(), is(true));
    }
}
```

There is also a simpler `ImmediateExecutor` that is a simple `ExecutorService`
(not scheduled), and will run all tasks immediately. Note that this may cause
problems if you have tasks that spawns new tasks all the time.

### Terminal Input and Output

When having a program (or module) that prints to standard out, error or needs
input from standard in, you need to emulate a real console or terminal. With
the `ConsoleWatcher` you can easily emulate standard in input, and get whatever
was written to standard out to be verified in the test. E.g.:

```java
class MyTest {
    @Rule
    ConsoleWatcher console = new ConsoleWatcher().dumpOnFailure();

    @Test
    public void testSomething() {
        console.setInput('a', '\n', Char.ESC);

        AtomicInteger exit = new AtomicInteger(0);
        MyProgram program = new MyProgram() {
            @Override
            protected void exit(i) {
                exit.set(i);
            }
        };

        program.execute("--no-worry");

        assertThat(console.getOutput(), is(""));
        assertThat(console.getError(), is("Invalid input: " + Strings.escape(Char.ESC)));
        assertThat(exit.get(), is(1));
    }
}
```

### Extra Matchers and Utils

There is also a set of extra matchers following the hamcrest matcher
interface. All of these are static methods of the `ExtraMatchers` class.

* `equalIgnoreIndent`: Same as isEqualIgnoreSpaces, but only ignores
  differences in spaces before the first printable character on *each line*. All
  other space chars must match.
* `equalToLines`: Is essentially the same as a string-based `equalTo`, but will
  make a line by line diff-based output instead of the standard char-by-char output.
  If a rather large multi-line string is compared, it may simplify debugging quite
  a lot to get the right failure output.
* `allItemsMatch`: Is a matcher that takes any collection, and compares every item
  in that collection with a given matcher.
* `oneOf`: Is a matcher that checks that the given value is one of the expected
  values. This forwards to a hamcrest matcher.
* `distinctFrom`: Is a matcher that takes any collection, compares it to another
  collection, and checks that there are no common items between the two.
* `inRange`: checks that a number is in a specific numeric range. E.g.
  `assertThat(sut.getNumSeals(), is(inRange(0, 10)))` will check that 'getNumSeals()'
  returns a vlaue of [0..9] respectively (inRange follows the lower-inclusive,
  higher-exclusive range standard).
* `matchesRegex`: Is a string matcher that checks if the string matches a specific
  regex. It uses the standard java Pattern syntax.

It is also common to have test resources that needs to be moved into temporary
folders for a specific method to be able to be tested. The `ResourceUtils` class
has a number of helper methods related to this.