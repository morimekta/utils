package net.morimekta.config;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for sequence objects.
 */
public class SequenceTest {

    @Test
    public void testNumberSequence() {
        Sequence sequence1 = Sequence.create(2);
        Sequence sequence1l = Sequence.create(2L);
        Sequence sequence2 = Sequence.create(2, 5);
        Sequence sequence3 = Sequence.create(5, 2);

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("Sequence(number:[2])", sequence1.toString());
        assertEquals("Sequence(number:[2,5])", sequence2.toString());
    }

    @Test
    public void testStringSequence() {
        Sequence sequence1 = Sequence.create("a");
        Sequence sequence1l = Sequence.create("a");
        Sequence sequence2 = Sequence.create("a", "b");
        Sequence sequence3 = Sequence.create("a");

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("Sequence(string:[a])", sequence1.toString());
        assertEquals("Sequence(string:[a,b])", sequence2.toString());
    }

    @Test
    public void testBooleanSequence() {
        Sequence sequence1 = Sequence.create(true);
        Sequence sequence1l = Sequence.create(true);
        Sequence sequence2 = Sequence.create(true, false);
        Sequence sequence3 = Sequence.create(false, true);

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("Sequence(boolean:[true])", sequence1.toString());
        assertEquals("Sequence(boolean:[true,false])", sequence2.toString());
    }

    @Test
    public void testSequenceSequence() {
        // TODO make.
    }

    @Test
    public void testConfigSequence() {

    }

    @Test
    public void testIterator() {
        Sequence sequence3 = Sequence.create("b", "a");
        Iterator<?> it = sequence3.iterator();

        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
    }


    @Test
    public void testGetString() throws ConfigException {
        Sequence sequence3 = Sequence.create("a", "b");

        assertEquals("a", sequence3.getString(0));
        assertEquals("b", sequence3.getString(1));
    }

    @Test
    public void testGetNumber() throws ConfigException {
        Sequence sequence3 = Sequence.create(1, 5L, 12.34);

        assertEquals(1, sequence3.getInteger(0));
        assertEquals(5L, sequence3.getLong(1));
        assertEquals(12.34, sequence3.getDouble(2), 0.0);
    }

    @Test
    public void testGetBoolean() throws ConfigException {
        Sequence sequence3 = Sequence.create(true);

        assertTrue(sequence3.getBoolean(0));
    }

    @Test
    public void testBadGet() {
        Sequence sequence3 = Sequence.create(true);

        try {
            sequence3.get(-1);
            fail("No exception on bad get index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }

        try {
            sequence3.get(1);
            fail("No exception on bad get index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 1, Size: 1", e.getMessage());
        }
    }

    @Test
    public void testCollector() {
        Collection<Integer> coll = new LinkedList<>();
        Collections.addAll(coll, 44, 32);

        Sequence seq = coll.stream().collect(Sequence.collect(Value.Type.STRING));

        assertEquals("Sequence(string:[44,32])", seq.toString());
    }

    @Test
    public void testBadAdd() {
        assertBadAdd("Not a string value: Config", Value.Type.STRING, new Config());
        assertBadAdd("Not a boolean value: truth", Value.Type.BOOLEAN, "truth");
        assertBadAdd("Not a boolean value: 1.1", Value.Type.BOOLEAN, 1.1);
        assertBadAdd("Not a number value: true", Value.Type.NUMBER, true);
        assertBadAdd("Not a number type: java.lang.Object", Value.Type.NUMBER, new Object());
        assertBadAdd("Not a config type: String", Value.Type.CONFIG, "{a:b}");
        assertBadAdd("Not a sequence type: String", Value.Type.SEQUENCE, "[a,b]");
    }

    private <T> void assertBadAdd(String message, Value.Type seqType, T value) {
        Sequence seq = new Sequence(seqType);

        try {
            seq.add(value);
            fail("No exception on bad add");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testBadInsert() {
        assertBadInsert("Index: -1, Size: 1", Value.Type.BOOLEAN, -1, true);
        assertBadInsert("Index: 2, Size: 1", Value.Type.BOOLEAN, 2, "true");

        assertBadInsert("Not a string value: Config", Value.Type.STRING, 0, new Config());
        assertBadInsert("Not a boolean value: truth", Value.Type.BOOLEAN, 0, "truth");
        assertBadInsert("Not a boolean value: -1", Value.Type.BOOLEAN, 0, -1);
        assertBadInsert("Not a boolean value: 1.0", Value.Type.BOOLEAN, 0, 1.0);
        assertBadInsert("Not a number value: true", Value.Type.NUMBER, 0, true);
        assertBadInsert("Not a number type: java.lang.Object", Value.Type.NUMBER, 0, new Object());
        assertBadInsert("Not a config type: String", Value.Type.CONFIG, 0, "{a:b}");
        assertBadInsert("Not a sequence type: String", Value.Type.SEQUENCE, 0, "[a,b]");
    }

    private void assertBadInsert(String message, Value.Type seqType, int i, Object value) {
        Sequence seq = new Sequence(seqType);
        switch (seqType) {
            case BOOLEAN:
                seq.addValue(Value.create(true));
                break;
            case NUMBER:
                seq.addValue(Value.create(0));
                break;
            case STRING:
                seq.addValue(Value.create("a"));
                break;
        }

        try {
            seq.add(i, value);
            fail("No exception on bad add");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testBuilderGetValue() {
        Sequence seq = new Sequence(Value.Type.STRING);
        seq.add(5);

        assertEquals(Value.create("5"), seq.getValue(0));
    }

    @Test
    public void testBuilderBadGetValue() {
        Sequence seq = new Sequence(Value.Type.STRING);
        seq.add(5);

        try {
            seq.getValue(-1);
            fail("No exception on invalid getValue index.");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }

        try {
            seq.getValue(1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 1, Size: 1", e.getMessage());
        }
    }

    @Test
    public void testBuilderRemove() {
        Sequence seq = new Sequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.remove(0);

        Sequence seq2 = new Sequence(Value.Type.STRING);
        seq2.add(10);

        assertEquals(seq, seq2);


        try {
            seq.remove(-1);
            fail("No exception on invalid getValue index.");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }

        try {
            seq.remove(1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 1, Size: 1", e.getMessage());
        }
    }

    @Test
    public void testBuilderRemoveLast() {
        Sequence seq = new Sequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.removeLast();

        Sequence seq2 = new Sequence(Value.Type.STRING);
        seq2.add(5);

        assertEquals(seq2, seq);

        seq.removeLast();

        assertEquals(new Sequence(Value.Type.STRING),
                     seq);

        try {
            seq.removeLast();
            fail("No exception from ");
        } catch (IllegalStateException e) {
            assertEquals("Unable to remove last of empty sequence", e.getMessage());
        }
    }

    @Test
    public void testBuilderReplace() {
        Sequence seq = new Sequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.replaceValue(0, Value.create(100));

        assertEquals(new Sequence(Value.Type.STRING, 100, 10),
                     seq);

        try {
            seq.replaceValue(-1, Value.create(0));
            fail("No exception from invalid index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }

        try {
            seq.replaceValue(2, Value.create(0));
            fail("No exception from invalid index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 2, Size: 2", e.getMessage());
        }
    }

    @Test
    public void testBuilderAddAll() {
        Sequence seq = new Sequence(Value.Type.STRING, 5, 10);

        Collection<?> coll = Collections.singletonList(15);
        seq.addAll(coll);

        assertEquals(new Sequence(Value.Type.STRING, 5, 10, 15), seq);
    }
}
