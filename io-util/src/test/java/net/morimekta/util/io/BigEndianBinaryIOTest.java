/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
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
package net.morimekta.util.io;

import net.morimekta.util.Binary;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test testing the pairing between BinaryWriter and BinaryReader, and that
 * what it writes will be read back exactly the same.
 */
public class BigEndianBinaryIOTest {
    ByteArrayOutputStream out;
    BinaryWriter writer;

    @Before
    public void setUp() throws InterruptedException, IOException {
        out = new ByteArrayOutputStream();
        writer = new BigEndianBinaryWriter(out);
    }

    private BinaryReader getReader() {
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        out.reset();
        return new BigEndianBinaryReader(in);
    }

    @Test
    public void testReadEndianNess() throws IOException {
        out.write(new byte[]{0, 4});
        assertEquals((short) 4, getReader().expectShort());

        out.write(new byte[]{0, 0, 0, 4});
        assertEquals(4, getReader().expectInt());

        out.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 4});
        assertEquals((short) 4, getReader().expectLong());

        out.write(new byte[]{0, 4});
        assertEquals((short) 4, getReader().expectUInt16());

        out.write(new byte[]{0, 0, 4});
        assertEquals((short) 4, getReader().expectUInt24());

        out.write(new byte[]{0, 0, 0, 4});
        assertEquals((short) 4, getReader().expectUInt32());
    }

    @Test
    public void testWriteEndianNess() throws IOException {
        writer.writeShort((short) 4);
        assertArrayEquals(new byte[]{0, 4},
                          out.toByteArray());
        out.reset();

        writer.writeInt(4);
        assertArrayEquals(new byte[]{0, 0, 0, 4},
                          out.toByteArray());
        out.reset();

        writer.writeLong(4L);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 4},
                          out.toByteArray());
        out.reset();

        writer.writeUInt16(4);
        assertArrayEquals(new byte[]{0, 4},
                          out.toByteArray());
        out.reset();

        writer.writeUInt24(4);
        assertArrayEquals(new byte[]{0, 0, 4},
                          out.toByteArray());
        out.reset();

        writer.writeUInt32(4);
        assertArrayEquals(new byte[]{0, 0, 0, 4},
                          out.toByteArray());
        out.reset();
    }

    @Test
    public void testRead() throws IOException {
        out.write(new byte[]{1, 2, 3, 4});

        BinaryReader reader = getReader();

        assertEquals(1, reader.read());

        byte[] tmp = new byte[10];
        assertEquals(3, reader.read(tmp, 2, 8));
        assertArrayEquals(new byte[]{0, 0, 2, 3, 4, 0, 0, 0, 0, 0},
                          tmp);

        try {
            reader.read(tmp, 5, 6);
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal arguments for read: byte[10], off:5, len:6",
                         e.getMessage());
        }
    }

    @Test
    public void testWrite() throws IOException {
        writer.write(new byte[]{1, 2, 3, 4}, 1, 2);
        writer.write(0x7f);

        byte[] tmp = new byte[4];
        assertEquals(3, getReader().read(tmp));
        assertArrayEquals(new byte[]{2, 3, 0x7f, 0}, tmp);
    }

    @Test
    public void testClose() {
        OutputStream out = mock(OutputStream.class);
        InputStream in = mock(InputStream.class);

        new BigEndianBinaryWriter(out).close();
        new BigEndianBinaryReader(in).close();

        verifyZeroInteractions(out, in);
    }

    @Test
    public void testBytes() throws IOException {
        byte[] bytes = new byte[]{0, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        // test writing and reading bytes.
        writer.write(bytes);

        BinaryReader reader = getReader();

        byte[] read = new byte[bytes.length];
        reader.read(read);

        assertArrayEquals(bytes, read);
    }

    @Test
    public void testByte() throws IOException {
        // test writing and reading bytes.
        writer.writeByte((byte) 1);
        writer.writeByte((byte) 0xff);
        writer.writeByte((byte) '\"');
        writer.writeByte((byte) 0);

        BinaryReader reader = getReader();

        assertEquals((byte) 1, reader.expectByte());
        assertEquals((byte) 0xff, reader.expectByte());
        assertEquals((byte) '\"', reader.expectByte());
        assertEquals((byte) 0, reader.expectByte());

        try {
            reader.expectByte();
        } catch (IOException e) {
            assertEquals("Missing expected byte", e.getMessage());
        }
    }

    @Test
    public void testShort() throws IOException {
        // test writing and reading shorts.
        writer.writeShort((short) 1);
        writer.writeShort((short) 0xffff);
        writer.writeShort((short) -12345);
        writer.writeShort((short) 0);

        BinaryReader reader = getReader();

        assertEquals((short) 1, reader.expectShort());
        assertEquals((short) 0xffff, reader.expectShort());
        assertEquals((short) -12345, reader.expectShort());
        assertEquals((short) 0, reader.expectShort());
    }

    @Test
    public void testBadShort(){
        assertBadExpectShort("Missing byte 1 to expected short", new byte[]{});
        assertBadExpectShort("Missing byte 2 to expected short", new byte[]{0});
    }

    private void assertBadExpectShort(String message, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            BinaryReader reader = new BigEndianBinaryReader(bais);
            reader.expectShort();
            fail("No exception on bad short");
        } catch (IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testInt() throws IOException {
        // test writing and reading shorts.
        writer.writeInt(1);
        writer.writeInt(0xdeadbeef);
        writer.writeInt(0xffffffff);
        writer.writeInt(-1234567890);
        writer.writeInt(0);

        BinaryReader reader = getReader();

        assertEquals(1, reader.expectInt());
        assertEquals(0xdeadbeef, reader.expectInt());
        assertEquals(0xffffffff, reader.expectInt());
        assertEquals(-1234567890, reader.expectInt());
        assertEquals(0, reader.expectInt());
    }

    @Test
    public void testBadInt() {
        assertBadExpectInt("Missing byte 1 to expected int", new byte[]{});
        assertBadExpectInt("Missing byte 2 to expected int", new byte[]{0});
        assertBadExpectInt("Missing byte 3 to expected int", new byte[]{0, 0});
        assertBadExpectInt("Missing byte 4 to expected int", new byte[]{0, 0, 0});
    }

    private void assertBadExpectInt(String message, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            BinaryReader reader = new BigEndianBinaryReader(bais);
            reader.expectInt();
            fail("No exception on bad int");
        } catch (IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testLong() throws IOException {
        // test writing and reading shorts.
        writer.writeLong(1);
        writer.writeLong(0xdeadbeefcafebabeL);
        writer.writeLong(0xffffffffffffffffL);
        writer.writeLong(-1234567890123456789L);
        writer.writeLong(0);

        BinaryReader reader = getReader();

        assertEquals(1, reader.expectLong());
        assertEquals(0xdeadbeefcafebabeL, reader.expectLong());
        assertEquals(0xffffffffffffffffL, reader.expectLong());
        assertEquals(-1234567890123456789L, reader.expectLong());
        assertEquals(0, reader.expectLong());
    }

    @Test
    public void testBadLong() {
        assertBadExpectLong("Missing byte 1 to expected long", new byte[]{});
        assertBadExpectLong("Missing byte 2 to expected long", new byte[]{0});
        assertBadExpectLong("Missing byte 3 to expected long", new byte[]{0, 0});
        assertBadExpectLong("Missing byte 4 to expected long", new byte[]{0, 0, 0});
        assertBadExpectLong("Missing byte 5 to expected long", new byte[]{0, 0, 0, 0});
        assertBadExpectLong("Missing byte 6 to expected long", new byte[]{0, 0, 0, 0, 0});
        assertBadExpectLong("Missing byte 7 to expected long", new byte[]{0, 0, 0, 0, 0, 0});
        assertBadExpectLong("Missing byte 8 to expected long", new byte[]{0, 0, 0, 0, 0, 0, 0});
    }

    private void assertBadExpectLong(String message, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            BinaryReader reader = new BigEndianBinaryReader(bais);
            reader.expectLong();
            fail("No exception on bad long");
        } catch (IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testDouble() throws IOException {
        // test writing and reading shorts.
        writer.writeDouble(1);
        writer.writeDouble(6.62607004E-34);
        writer.writeDouble(299792458);
        writer.writeDouble(-123456.123456);
        writer.writeDouble(0.0);

        BinaryReader reader = getReader();

        assertEquals(1.0, reader.expectDouble(), 0.0);
        assertEquals(6.62607004E-34, reader.expectDouble(), 0.0);
        assertEquals(299792458, reader.expectDouble(), 0.0);
        assertEquals(-123456.123456, reader.expectDouble(), 0.0);
        assertEquals(0.0, reader.expectDouble(), 0.0);
    }

    @Test
    public void testBinary() throws IOException {
        Binary bytes = Binary.wrap(new byte[]{0, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4, 3, 2, 1});
        // test writing and reading bytes.
        writer.writeBinary(bytes);

        BinaryReader reader = getReader();

        Binary read = reader.expectBinary(bytes.length());

        assertEquals(bytes, read);
    }

    @Test
    public void testBadBinary() throws IOException {
        Binary bytes = Binary.wrap(new byte[]{0, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4, 3, 2, 1});
        // test writing and reading bytes.
        writer.writeBinary(bytes);

        BinaryReader reader = getReader();

        try {
            reader.expectBinary(bytes.length() + 1);
            fail("No exception on bad binary data");
        } catch (IOException e) {
            assertEquals("Not enough data available on stream: 20 < 21", e.getMessage());
        }
    }

    @Test
    public void testExpectUnsigned() throws IOException {
        writer.writeUInt8(1);
        writer.writeUInt16(2);
        writer.writeUInt24(3);
        writer.writeUInt32(4);

        BinaryReader reader = getReader();

        assertEquals(1, reader.expectUInt8());
        assertEquals(2, reader.expectUInt16());
        assertEquals(3, reader.expectUInt24());
        assertEquals(4, reader.expectUInt32());
    }

    @Test
    public void testReadUnsigned() throws IOException {
        writer.writeUInt16(2);

        BinaryReader reader = getReader();

        assertEquals(2, reader.readUInt16());
        assertEquals(0, reader.readUInt16());

        writer.writeUInt8(5);
        reader = getReader();

        try {
            reader.readUInt16();
            fail("No exception in bad read");
        } catch (IOException e) {
            assertEquals("Missing byte 2 to read uint16", e.getMessage());
        }
    }

    @Test
    public void testUnsigned() throws IOException {
        writer.writeUnsigned(1, 1);
        writer.writeUnsigned(2, 2);
        writer.writeUnsigned(3, 3);
        writer.writeUnsigned(4, 4);

        try {
            writer.writeUnsigned(8, 8);
            fail("No exception on bad argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported byte count for unsigned: 8", e.getMessage());
        }

        BinaryReader reader = getReader();

        try {
            reader.expectUnsigned(8);
            fail("No exception on bad argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported byte count for unsigned: 8", e.getMessage());
        }

        assertEquals(1, reader.expectUnsigned(1));
        assertEquals(2, reader.expectUnsigned(2));
        assertEquals(3, reader.expectUnsigned(3));
        assertEquals(4, reader.expectUnsigned(4));
    }

    @Test
    public void testBadUnsigned() {
        assertBadExpectUnsigned("Missing unsigned byte", new byte[]{}, 1);

        assertBadExpectUnsigned("Missing byte 1 to expected uint16", new byte[]{}, 2);
        assertBadExpectUnsigned("Missing byte 2 to expected uint16", new byte[]{0}, 2);

        assertBadExpectUnsigned("Missing byte 1 to expected uint24", new byte[]{}, 3);
        assertBadExpectUnsigned("Missing byte 2 to expected uint24", new byte[]{0}, 3);
        assertBadExpectUnsigned("Missing byte 3 to expected uint24", new byte[]{0, 0}, 3);

        assertBadExpectUnsigned("Missing byte 1 to expected int", new byte[]{}, 4);
        assertBadExpectUnsigned("Missing byte 2 to expected int", new byte[]{0}, 4);
        assertBadExpectUnsigned("Missing byte 3 to expected int", new byte[]{0, 0}, 4);
        assertBadExpectUnsigned("Missing byte 4 to expected int", new byte[]{0, 0, 0}, 4);
    }

    private void assertBadExpectUnsigned(String message, byte[] data, int bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            BinaryReader reader = new BigEndianBinaryReader(bais);
            reader.expectUnsigned(bytes);
            fail("No exception on bad short");
        } catch (IOException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testSigned() throws IOException {
        writer.writeSigned(-1, 1);
        writer.writeSigned(-2, 2);
        writer.writeSigned(-3, 4);
        writer.writeSigned(-4, 8);
        writer.writeSigned(-100L, 1);
        writer.writeSigned(-200L, 2);
        writer.writeSigned(-300L, 4);
        writer.writeSigned(-400L, 8);

        try {
            writer.writeSigned(-8, 3);
            fail("No exception on bad argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported byte count for signed: 3", e.getMessage());
        }

        try {
            writer.writeSigned(-8L, 3);
            fail("No exception on bad argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported byte count for signed: 3", e.getMessage());
        }

        BinaryReader reader = getReader();

        try {
            reader.expectSigned(3);
            fail("No exception on bad argument");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported byte count for signed: 3", e.getMessage());
        }

        assertEquals(-1, reader.expectSigned(1));
        assertEquals(-2, reader.expectSigned(2));
        assertEquals(-3, reader.expectSigned(4));
        assertEquals(-4, reader.expectSigned(8));    }

    @Test
    public void testZigzag() throws IOException {
        // test integer (32 bit) varints.
        testZigzag(0, 1);
        testZigzag(1, 1);
        testZigzag(-1, 1);
        testZigzag(0xcafe, 3);
        testZigzag(-123456, 3);
        testZigzag(615671317, 5);
        testZigzag(Integer.MIN_VALUE, 5);
        testZigzag(Integer.MAX_VALUE, 5);

        // test long (64 bit) varints.

        testZigzag(0L, 1);
        testZigzag(1L, 1);
        testZigzag(-1L, 1);
        testZigzag(0xcafeL, 3);
        testZigzag(-123456L, 3);

        testZigzag(1234567890123456789L, 9);
        testZigzag(0xcafebabedeadbeefL, 9);
    }

    private void testZigzag(int value, int bytes) throws IOException {
        out.reset();
        writer.writeZigzag(value);
        assertEquals(bytes, out.size());
        BinaryReader reader = getReader();
        assertEquals(value, reader.readIntZigzag());
    }

    private void testZigzag(long value, int bytes) throws IOException {
        out.reset();
        writer.writeZigzag(value);
        assertEquals(bytes, out.size());
        BinaryReader reader = getReader();
        assertEquals(value, reader.readLongZigzag());
    }

    @Test
    public void testVarint() throws IOException {
        // test integer (32 bit) varints.
        testVarint(0, 1);
        testVarint(1, 1);
        testVarint(-1, 5);
        testVarint(0xcafe, 3);
        testVarint(-123456, 5);
        testVarint(Integer.MIN_VALUE, 5);
        testVarint(Integer.MAX_VALUE, 5);

        // test long (64 bit) varints.

        testVarint(0L, 1);
        testVarint(1L, 1);
        testVarint(-1L, 10);
        testVarint(0xcafeL, 3);
        testVarint(-123456L, 10);
        testVarint(Long.MIN_VALUE, 10);
        testVarint(Long.MAX_VALUE, 9);

        testVarint(1234567890123456789L, 9);
        testVarint(0xcafebabedeadbeefL, 10);
    }

    @Test
    public void testEmptyVaring() throws IOException {
        assertEquals(0, getReader().readIntVarint());
        assertEquals(0L, getReader().readLongVarint());
    }

    private void testVarint(int value, int bytes) throws IOException {
        out.reset();
        writer.writeVarint(value);
        assertEquals(bytes, out.size());
        BinaryReader reader = getReader();
        assertEquals(value, reader.readIntVarint());
    }

    private void testVarint(long value, int bytes) throws IOException {
        out.reset();
        writer.writeVarint(value);
        assertEquals(bytes, out.size());
        BinaryReader reader = getReader();
        assertEquals(value, reader.readLongVarint());
    }

    @Test
    public void tesExpectLarge() throws IOException {
        byte[] arr = new byte[1024 * 1024]; // 1 MB.
        new Random().nextBytes(arr);
        ByteArrayInputStream in = new ByteArrayInputStream(arr);
        BigEndianBinaryReader reader = new BigEndianBinaryReader(in);

        byte[] res = reader.expectBytes(arr.length);

        assertThat(arr, is(equalTo(res)));
    }


    @Test
    public void tesExpectTooLarge() throws IOException {
        byte[] arr = new byte[1024 * 10]; // 10 kB.
        new Random().nextBytes(arr);
        ByteArrayInputStream in = new ByteArrayInputStream(arr);
        BigEndianBinaryReader reader = new BigEndianBinaryReader(in);

        try {
            reader.expectBytes(1024 * 1024 * 10);
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Not enough data available on stream: 10240 < 10485760"));
        }
    }
}
