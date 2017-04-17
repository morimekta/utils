package net.morimekta.console.chr;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the char streams.
 */
public class CharStreamTest {
    @Test
    public void testIterator() {
        Iterator<Char> it = CharStream.iterator("abcdef");

        assertTrue(it.hasNext());
        assertEquals("a", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("b", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("c", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("d", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("e", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("f", it.next().toString());
        assertFalse(it.hasNext());
    }

        @Test
    public void testIteratorFailure() {
        Iterator<Char> it = CharStream.iterator("a\0333,");

        assertTrue(it.hasNext());
        assertEquals("a", it.next().toString());
        try {
            it.hasNext();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getCause().getMessage(), is("Invalid escape sequence: \"\\0333\""));
        }
        // After this is undefined.
    }

    @Test
    public void testLenientIterator() {
        Iterator<Char> it = CharStream.lenientIterator("a\0333,");

        assertTrue(it.hasNext());
        assertEquals("a", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("\033", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals("3", it.next().toString());
        assertTrue(it.hasNext());
        assertEquals(",", it.next().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testStream() {
        List<Char> list = CharStream.stream("abcdef").collect(Collectors.toList());
        assertEquals(6, list.size());
        assertEquals("a", list.get(0).toString());
        assertEquals("b", list.get(1).toString());
        assertEquals("c", list.get(2).toString());
        assertEquals("d", list.get(3).toString());
        assertEquals("e", list.get(4).toString());
        assertEquals("f", list.get(5).toString());
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = CharStream.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    @Test
    public void testReader() throws IOException {
        assertInput("");
        assertInput("a", 'a');
        assertInput("\033", '\033');
        assertInput("\033\033", '\033');
        assertInput(Color.RED.toString(), Color.RED);
        assertInput(Control.CTRL_DOWN.toString(), Control.CTRL_DOWN);

        assertInput(format("%sa%s%s", Color.RED, Control.CTRL_DOWN, CharUtil.makeNumeric(33)),
                    Color.RED, "a", Control.CTRL_DOWN, CharUtil.makeNumeric(33));
        assertInput(format("%s", Control.cursorSetPos(33)),
                    Control.cursorSetPos(33));

        assertInput("\033OB", new Control("\033OB"));
        assertInput("\033a", new Control("\033a"));
        assertInput("𪚲", 0x2A6B2);
    }

    @Test
    public void testFailures() {
        assertFailure("\033O", "Unexpected end of stream.");
        assertFailure("\033[", "Unexpected end of stream.");
        assertFailure("\033[5", "Unexpected end of stream.");
        assertFailure("\033[mb", "Invalid color control sequence: \"\\033[m\"");
        assertFailure("\033[.b", "Invalid escape sequence: \"\\033[.\"");
        assertFailure("\033O5b", "Invalid escape sequence: \"\\033O5\"");
        assertFailure("\0335b", "Invalid escape sequence: \"\\0335\"");
    }

    @Test
    public void testNoLenientFailures() {
        assertNoLenientFailure("\033O", new Unicode('\033'), "O");
        assertNoLenientFailure("\033[", new Unicode('\033'), "[");
        assertNoLenientFailure("\033[5", new Unicode('\033'), "[5");
        assertNoLenientFailure("\033[mb", new Unicode('\033'), "[mb");
        assertNoLenientFailure("\033[.b", new Unicode('\033'), "[.b");
        assertNoLenientFailure("\033O5b", new Unicode('\033'), "O5b");
        assertNoLenientFailure("\0335b", new Unicode('\033'), "5b");
    }

    private void assertInput(String in, Object... out) throws IOException {
        assertThat(CharStream.stream(in).collect(Collectors.toList()),
                   is(CharUtil.inputChars(out)));
    }

    private void assertFailure(String in, String message) {
        try {
            List<Char> ignore = CharStream.stream(in).collect(Collectors.toList());
            assertThat(ignore, is(ImmutableList.of()));
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getCause().getMessage(), is(message));
        }
    }

     private void assertNoLenientFailure(String in, Object... out) {
        assertThat(CharStream.lenientStream(in).collect(Collectors.toList()),
                   is(CharUtil.inputChars(out)));
     }

}
