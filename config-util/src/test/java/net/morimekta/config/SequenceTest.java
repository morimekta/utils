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
    public void testBuilder() {
        Sequence.Builder builder = Sequence.builder(Value.Type.NUMBER);

        assertEquals(0, builder.size());
        assertEquals(Value.Type.NUMBER, builder.type());
    }

    @Test
    public void testNumberSequence() {
        Sequence sequence1 = Sequence.builder(Value.Type.NUMBER)
                                     .add(2)
                                     .build();
        Sequence sequence1l = Sequence.builder(Value.Type.NUMBER)
                                      .add(2L)
                                      .build();
        Sequence sequence2 = Sequence.builder(Value.Type.NUMBER)
                                     .add(2)
                                     .add(5)
                                     .build();
        Sequence sequence3 = Sequence.builder(Value.Type.NUMBER)
                                     .add(5)
                                     .add(2)
                                     .build();

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
        Sequence sequence1 = Sequence.builder(Value.Type.STRING)
                                     .add("a")
                                     .build();
        Sequence sequence1l = Sequence.builder(Value.Type.STRING)
                                      .add("a")
                                      .build();
        Sequence sequence2 = Sequence.builder(Value.Type.STRING)
                                     .add("a")
                                     .add("b")
                                     .build();
        Sequence sequence3 = Sequence.builder(Value.Type.STRING)
                                     .add("a")
                                     .insert(0, "b")
                                     .build();

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
        Sequence sequence1 = Sequence.builder(Value.Type.BOOLEAN)
                                     .add(true)
                                     .build();
        Sequence sequence1l = Sequence.builder(Value.Type.BOOLEAN)
                                      .add(true)
                                      .build();
        Sequence sequence2 = Sequence.builder(Value.Type.BOOLEAN)
                                     .add(true)
                                     .add(false)
                                     .build();
        Sequence sequence3 = Sequence.builder(Value.Type.BOOLEAN)
                                     .add(true)
                                     .insert(0, false)
                                     .build();

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
        Sequence sequence3 = Sequence.builder(Value.Type.STRING)
                                     .add("a")
                                     .insert(0, "b")
                                     .build();
        Iterator<?> it = sequence3.iterator();

        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
    }


    @Test
    public void testGetString() throws ConfigException {
        Sequence sequence3 = Sequence.builder(Value.Type.STRING)
                                     .addAll("a", "b")
                                     .build();

        assertEquals("a", sequence3.getString(0));
        assertEquals("b", sequence3.getString(1));
    }

    @Test
    public void testGetNumber() throws ConfigException {
        Sequence sequence3 = Sequence.builder(Value.Type.NUMBER)
                                     .addAll(1, 5L, 12.34)
                                     .build();

        assertEquals(1, sequence3.getInteger(0));
        assertEquals(5L, sequence3.getLong(1));
        assertEquals(12.34, sequence3.getDouble(2), 0.0);
    }

    @Test
    public void testGetBoolean() throws ConfigException {
        Sequence sequence3 = Sequence.builder(Value.Type.BOOLEAN)
                                     .addAll(true)
                                     .build();

        assertTrue(sequence3.getBoolean(0));
    }

    @Test
    public void testBadGet() {
        Sequence sequence3 = Sequence.builder(Value.Type.BOOLEAN)
                                     .addAll(true)
                                     .build();

        try {
            sequence3.get(-1);
            fail("No exception on bad get index");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid index -1", e.getMessage());
        }

        try {
            sequence3.get(1);
            fail("No exception on bad get index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 1 outside range of sequence length 1", e.getMessage());
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
        assertBadAdd("Not a string value: Config", Value.Type.STRING, Config.builder().build());
        assertBadAdd("Not a boolean value: String", Value.Type.BOOLEAN, "true");
        assertBadAdd("Not a boolean value: Integer", Value.Type.BOOLEAN, 1);
        assertBadAdd("Not a boolean value: Double", Value.Type.BOOLEAN, 1.0);
        assertBadAdd("Not a number value: Boolean", Value.Type.NUMBER, true);
        assertBadAdd("Not a number value: String", Value.Type.NUMBER, "1");
        assertBadAdd("Not a number value: Object", Value.Type.NUMBER, new Object());
        assertBadAdd("Not a config type: String", Value.Type.CONFIG, "{a:b}");
        assertBadAdd("Not a sequence type: String", Value.Type.SEQUENCE, "[a,b]");
    }

    private <T> void assertBadAdd(String message, Value.Type seqType, T value) {
        Sequence.Builder seq = Sequence.builder(seqType);

        try {
            seq.add(value);
            fail("No exception on bad add");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testBadInsert() {
        assertBadInsert("Illegal insert index -1", Value.Type.BOOLEAN, -1, true);
        assertBadInsert("Insert index 2 outside range of sequence length 1", Value.Type.BOOLEAN, 2, "true");

        assertBadInsert("Not a string value: Config", Value.Type.STRING, 0, Config.builder().build());
        assertBadInsert("Not a boolean value: String", Value.Type.BOOLEAN, 0, "true");
        assertBadInsert("Not a boolean value: Integer", Value.Type.BOOLEAN, 0, 1);
        assertBadInsert("Not a boolean value: Double", Value.Type.BOOLEAN, 0, 1.0);
        assertBadInsert("Not a number value: Boolean", Value.Type.NUMBER, 0, true);
        assertBadInsert("Not a number value: String", Value.Type.NUMBER, 0, "1");
        assertBadInsert("Not a number value: Object", Value.Type.NUMBER, 0, new Object());
        assertBadInsert("Not a config type: String", Value.Type.CONFIG, 0, "{a:b}");
        assertBadInsert("Not a sequence type: String", Value.Type.SEQUENCE, 0, "[a,b]");
    }

    private void assertBadInsert(String message, Value.Type seqType, int i, Object value) {
        Sequence.Builder seq = Sequence.builder(seqType);
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
            seq.insert(i, value);
            fail("No exception on bad add");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testBuilderGetValue() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5);

        assertEquals(Value.create("5"), seq.getValue(0));
    }

    @Test
    public void testBuilderBadGetValue() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5);

        try {
            seq.getValue(-1);
            fail("No exception on invalid getValue index.");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid index -1", e.getMessage());
        }

        try {
            seq.getValue(1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 1 outside range of sequence length 1", e.getMessage());
        }
    }

    @Test
    public void testBuilderRemove() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5).add(10);

        seq.remove(0);

        assertEquals(Sequence.builder(Value.Type.STRING).add(10).build(),
                     seq.build());


        try {
            seq.remove(-1);
            fail("No exception on invalid getValue index.");
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal remove index -1", e.getMessage());
        }

        try {
            seq.remove(1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Remove index 1 outside range of sequence length 1", e.getMessage());
        }
    }

    @Test
    public void testBuilderRemoveLast() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5).add(10);

        seq.removeLast();

        assertEquals(Sequence.builder(Value.Type.STRING).add(5).build(),
                     seq.build());

        seq.removeLast();

        assertEquals(Sequence.builder(Value.Type.STRING).build(),
                     seq.build());

        try {
            seq.removeLast();
            fail("No exception from ");
        } catch (IllegalStateException e) {
            assertEquals("Unable to remove last of empty sequence", e.getMessage());
        }
    }

    @Test
    public void testBuilderReplace() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5).add(10);

        seq.replaceValue(0, Value.create(100));

        assertEquals(Sequence.builder(Value.Type.STRING).add(100).add(10).build(),
                     seq.build());

        try {
            seq.replaceValue(-1, Value.create(0));
            fail("No exception from invalid index");
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal replace index -1", e.getMessage());
        }

        try {
            seq.replaceValue(2, Value.create(0));
            fail("No exception from invalid index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Replace index 2 outside range of sequence length 2", e.getMessage());
        }
    }

    @Test
    public void testBuilderAddAll() {
        Sequence.Builder seq = Sequence.builder(Value.Type.STRING).add(5).add(10);

        Collection<?> coll = Collections.singletonList(15);
        seq.addAll(coll);
    }
}
