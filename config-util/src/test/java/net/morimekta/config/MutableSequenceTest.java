/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
public class MutableSequenceTest {

    @Test
    public void testNumberSequence() {
        Sequence sequence1 = MutableSequence.create(2);
        Sequence sequence1l = MutableSequence.create(2L);
        Sequence sequence2 = MutableSequence.create(2, 5);
        Sequence sequence3 = MutableSequence.create(5, 2);

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("MutableSequence(number:[2])", sequence1.toString());
        assertEquals("MutableSequence(number:[2,5])", sequence2.toString());
    }

    @Test
    public void testStringSequence() {
        Sequence sequence1 = MutableSequence.create("a");
        Sequence sequence1l = MutableSequence.create("a");
        Sequence sequence2 = MutableSequence.create("a", "b");
        Sequence sequence3 = MutableSequence.create("a");

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("MutableSequence(string:[a])", sequence1.toString());
        assertEquals("MutableSequence(string:[a,b])", sequence2.toString());
    }

    @Test
    public void testBooleanSequence() {
        Sequence sequence1 = MutableSequence.create(true);
        Sequence sequence1l = MutableSequence.create(true);
        Sequence sequence2 = MutableSequence.create(true, false);
        Sequence sequence3 = MutableSequence.create(false, true);

        assertEquals(sequence1, sequence1);
        assertNotEquals(sequence1, null);
        assertEquals(sequence1, sequence1l);
        assertNotEquals(sequence1, sequence2);
        assertNotEquals(sequence2, sequence3);
        assertEquals("MutableSequence(boolean:[true])", sequence1.toString());
        assertEquals("MutableSequence(boolean:[true,false])", sequence2.toString());
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
        Sequence sequence3 = MutableSequence.create("b", "a");
        Iterator<?> it = sequence3.iterator();

        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
    }


    @Test
    public void testGetString() throws ConfigException {
        Sequence sequence3 = MutableSequence.create("a", "b");

        assertEquals("a", sequence3.getString(0));
        assertEquals("b", sequence3.getString(1));
    }

    @Test
    public void testGetNumber() throws ConfigException {
        Sequence sequence3 = MutableSequence.create(1, 5L, 12.34);

        assertEquals(1, sequence3.getInteger(0));
        assertEquals(5L, sequence3.getLong(1));
        assertEquals(12.34, sequence3.getDouble(2), 0.0);
    }

    @Test
    public void testGetBoolean() throws ConfigException {
        Sequence sequence3 = MutableSequence.create(true);

        assertTrue(sequence3.getBoolean(0));
    }

    @Test
    public void testBadGet() {
        Sequence sequence3 = MutableSequence.create(true);

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

        Sequence seq = coll.stream().collect(MutableSequence.collect(Value.Type.STRING));

        assertEquals("MutableSequence(string:[44,32])", seq.toString());
    }

    @Test
    public void testBadAdd() {
        assertBadAdd("Not a string type: ImmutableConfig", Value.Type.STRING, new ImmutableConfig());
        assertBadAdd("Not a boolean value: truth", Value.Type.BOOLEAN, "truth");
        assertBadAdd("Not a boolean value: 1.1", Value.Type.BOOLEAN, 1.1);
        assertBadAdd("Not a number value: true", Value.Type.NUMBER, true);
        assertBadAdd("Not a number type: java.lang.Object", Value.Type.NUMBER, new Object());
        assertBadAdd("Not a config type: String", Value.Type.CONFIG, "{a:b}");
        assertBadAdd("Not a sequence type: String", Value.Type.SEQUENCE, "[a,b]");
    }

    private <T> void assertBadAdd(String message, Value.Type seqType, T value) {
        MutableSequence seq = new MutableSequence(seqType);

        try {
            seq.add(value);
            fail("No exception on bad add");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testBadInsert() {
        assertBadInsert("-1", Value.Type.BOOLEAN, -1, true);
        assertBadInsert("Index: 2, Size: 1", Value.Type.BOOLEAN, 2, "true");

        assertBadInsert("Not a string type: ImmutableConfig", Value.Type.STRING, 0, new ImmutableConfig());
        assertBadInsert("Not a boolean value: truth", Value.Type.BOOLEAN, 0, "truth");
        assertBadInsert("Not a boolean value: -1", Value.Type.BOOLEAN, 0, -1);
        assertBadInsert("Not a boolean value: 1.0", Value.Type.BOOLEAN, 0, 1.0);
        assertBadInsert("Not a number value: true", Value.Type.NUMBER, 0, true);
        assertBadInsert("Not a number type: java.lang.Object", Value.Type.NUMBER, 0, new Object());
        assertBadInsert("Not a config type: String", Value.Type.CONFIG, 0, "{a:b}");
        assertBadInsert("Not a sequence type: String", Value.Type.SEQUENCE, 0, "[a,b]");
    }

    private void assertBadInsert(String message, Value.Type seqType, int i, Object value) {
        MutableSequence seq = new MutableSequence(seqType);
        switch (seqType) {
            case BOOLEAN:
                seq.addValue(ImmutableValue.create(true));
                break;
            case NUMBER:
                seq.addValue(ImmutableValue.create(0));
                break;
            case STRING:
                seq.addValue(ImmutableValue.create("a"));
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
        MutableSequence seq = new MutableSequence(Value.Type.STRING);
        seq.add(5);

        assertEquals(ImmutableValue.create("5"), seq.getValue(0));
    }

    @Test
    public void testBuilderBadGetValue() {
        MutableSequence seq = new MutableSequence(Value.Type.STRING);
        seq.add(5);

        try {
            seq.getValue(-1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
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
        MutableSequence seq = new MutableSequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.remove(0);

        MutableSequence seq2 = new MutableSequence(Value.Type.STRING);
        seq2.add(10);

        assertEquals(seq, seq2);


        try {
            seq.remove(-1);
            fail("No exception on invalid getValue index.");
        } catch (IndexOutOfBoundsException e) {
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
        MutableSequence seq = new MutableSequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.removeLast();

        MutableSequence seq2 = new MutableSequence(Value.Type.STRING);
        seq2.add(5);

        assertEquals(seq2, seq);

        seq.removeLast();

        assertEquals(new MutableSequence(Value.Type.STRING),
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
        MutableSequence seq = new MutableSequence(Value.Type.STRING);
        seq.add(5);
        seq.add(10);

        seq.setValue(0, ImmutableValue.create(100));

        assertEquals(new MutableSequence(Value.Type.STRING, 100, 10),
                     seq);

        try {
            seq.setValue(-1, ImmutableValue.create(0));
            fail("No exception from invalid index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }

        try {
            seq.setValue(2, ImmutableValue.create(0));
            fail("No exception from invalid index");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 2, Size: 2", e.getMessage());
        }
    }

    @Test
    public void testBuilderAddAll() {
        MutableSequence seq = new MutableSequence(Value.Type.STRING, 5, 10);

        Collection<?> coll = Collections.singletonList(15);
        seq.addAll(coll);

        assertEquals(new MutableSequence(Value.Type.STRING, 5, 10, 15), seq);
    }
}
