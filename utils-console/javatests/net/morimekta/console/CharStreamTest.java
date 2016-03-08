package net.morimekta.console;

import net.morimekta.util.Base64;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testCharSpliterator() {

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
}
