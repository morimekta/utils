package net.morimekta.console.terminal;

import net.morimekta.console.test_utils.ConsoleWatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LineBufferTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    private LineBuffer buffer;

    @Before
    public void setUp() {
        buffer = new LineBuffer(new Terminal(console.tty()));
    }

    @Test
    public void testAdd() {
        buffer.add("first", "second", "third");
        assertThat(console.output(),
                   is("first" +
                      "\r\n" +
                      "second" +
                      "\r\n" +
                      "third"));

        console.reset();
        buffer.add("fourth");
        assertThat(console.output(),
                   is("\r\n" +
                      "fourth"));
    }

    @Test
    public void testUpdate1() {
        buffer.add("first", "second", "third");
        console.reset();

        buffer.update(1, "fourth");
        assertThat(console.output(),
                   is("\r\033[1A\033[K" +
                      "fourth" +
                      "\r\033[1B\033[5C"));
    }


    @Test
    public void testClear() {
        buffer.add("first", "second", "third");
        console.reset();

        buffer.clear();
        assertThat(console.output(),
                   is("\r" +
                      "\033[K\033[A" +
                      "\033[K\033[A" +
                      "\033[K"));
    }

    @Test
    public void testClearLastN() {
        buffer.add("first", "second", "third");
        console.reset();

        buffer.clearLast(2);
        assertThat(console.output(),
                   is("\r" +
                      "\033[K\033[A" +
                      "\033[K\033[A" +
                      "\r\033[5C"));

        console.reset();
        buffer.clearLast(1);
        assertThat(console.output(),
                   is("\r\033[K"));
    }

}
