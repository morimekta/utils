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
package net.morimekta.util;

import net.morimekta.util.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by SteinEldar on 24.12.2015.
 */
public class BinaryTest {
    private byte[] a1;
    private byte[] a2;
    private byte[] b1;
    private byte[] b2;
    private byte[] c1;
    private byte[] c2;

    @Before
    public void setUp() {
        a1 = new byte[]{'a', 'b', 'c'};
        a2 = new byte[]{'a', 'b', 'c'};
        b1 = new byte[]{'a', 'b', 'd'};
        b2 = new byte[]{'a', 'b', 'd'};
        c1 = new byte[]{'a', 'b', 'c', 'd'};
        c2 = new byte[]{'a', 'b', 'c', 'd'};
    }

    @Test
    public void testWrap() {
        // Wrap refers to the same array, so they will be modified in sync
        // with the original array.
        Binary a = Binary.wrap(a1);
        Binary b = Binary.wrap(a1);

        assertEquals(a, b);
        assertEquals("616263", a.toHexString());

        a1[2] = 'e';

        assertEquals(a, b);
        assertEquals("616265", a.toHexString());
    }

    @Test
    public void testEmpty() {
        // Copy makes an in-memory copy, and will thus be unaffected by
        // modifications to the original.
        Binary empty = Binary.empty();

        // Not modified.
        assertEquals("", empty.toHexString());
    }

    @Test
    public void testCopy() {
        // Copy makes an in-memory copy, and will thus be unaffected by
        // modifications to the original.
        Binary a = Binary.copy(a1);
        Binary c = Binary.copy(c1, 1, 2);

        assertEquals("616263", a.toHexString());
        assertEquals("6263", c.toHexString());

        a1[2] = 'e';
        c1[2] = 'f';

        // Not modified.
        assertEquals("616263", a.toHexString());
        assertEquals("6263", c.toHexString());
    }

    @Test
    public void testGet() {
        // Gets a copy of the array, so modifications to it will not affect
        // the contained array.
        Binary c = Binary.wrap(c1);
        assertEquals("61626364", c.toHexString());

        byte[] copy = c.get();

        assertArrayEquals(c1, copy);

        copy[1] = 'B';

        assertFalse(Arrays.equals(copy, c1));
        assertEquals("61626364", c.toHexString());

        Arrays.fill(copy, (byte) 0);

        c.get(copy);
        assertArrayEquals(c1, copy);

        byte[] sh = new byte[3];
        c.get(sh);
        assertArrayEquals(a1, sh);
    }

    @Test
    public void testBase64() throws IOException {
        String a = Base64.encodeToString(a1);

        assertEquals(a,
                     Binary.wrap(a1)
                           .toBase64());
        assertEquals(Binary.wrap(a2), Binary.fromBase64(a));
    }

    @Test
    public void testUUID() {
        UUID uuid = UUID.randomUUID();
        Binary binary = Binary.fromUUID(uuid);

        assertThat(binary.toUUID(), is(equalTo(uuid)));
        assertThat(binary.toHexString(), is(equalTo(uuid.toString().replaceAll("-", ""))));

        try {
            Binary.wrap(a1).toUUID();
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Length not compatible with UUID: 3 != 16"));
        }

        try {
            Binary.fromUUID(null);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Null UUID for binary"));
        }
    }

    @Test
    public void testHexString() {
        Binary a = Binary.wrap(a1);
        assertEquals("616263", a.toHexString());
        assertEquals(a, Binary.fromHexString("616263"));

        try {
            Binary.fromHexString("a");
            fail("No exception on bad input");
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal hex string length: 1", e.getMessage());
        }
    }

    @Test
    public void testGetByteBuffer() {
        // Just test that the byte buffer contains a copy, and not the
        // original array.
        Binary a = Binary.wrap(a1);
        ByteBuffer b = a.getByteBuffer();
        assertNotSame(a1, b.array());
    }

    @Test
    public void testGetInputStream() throws IOException {
        Binary a = Binary.wrap(a1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(a.getInputStream(), baos);

        assertThat(baos.toByteArray(), is(equalTo(a1)));
    }

    @Test
    public void testIoStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Binary c = Binary.wrap(c1);
        c.write(baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Binary o = Binary.read(bais, baos.size());

        assertEquals(c, o);
    }

    @Test
    public void testReadNotEnough() {
        ByteArrayInputStream bais = new ByteArrayInputStream(c1);

        try {
            Binary.read(bais, c1.length + 1);
            fail("No exception on too short input array");
        } catch (IOException e) {
            assertEquals("End of stream before complete buffer read.", e.getMessage());
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(Binary.wrap(a1).hashCode(),
                     Binary.wrap(a2).hashCode());
        assertEquals(Binary.wrap(b1).hashCode(),
                     Binary.wrap(b2).hashCode());
        assertEquals(Binary.wrap(c1).hashCode(),
                     Binary.wrap(c2).hashCode());

        assertNotEquals(Binary.wrap(b1).hashCode(),
                        Binary.wrap(a2).hashCode());
        assertNotEquals(Binary.wrap(c1).hashCode(),
                        Binary.wrap(b2).hashCode());
        assertNotEquals(Binary.wrap(a1).hashCode(),
                        Binary.wrap(c2).hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(Binary.wrap(a1), Binary.wrap(a2));
        assertEquals(Binary.wrap(b1), Binary.wrap(b2));
        assertEquals(Binary.wrap(c1), Binary.wrap(c2));

        assertNotEquals(Binary.wrap(b1), Binary.wrap(a2));
        assertNotEquals(Binary.wrap(c1), Binary.wrap(b2));
        assertNotEquals(Binary.wrap(a1), Binary.wrap(c2));

        Binary a = Binary.wrap(a1);
        Binary b = null;
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(new Object()));
    }

    @Test
    public void testCompareTo() {
        TreeSet<Binary> set = new TreeSet<>();
        set.add(Binary.wrap(a1));
        set.add(Binary.wrap(a2));
        set.add(Binary.wrap(b1));
        set.add(Binary.wrap(b2));
        set.add(Binary.wrap(c1));
        set.add(Binary.wrap(c2));

        assertEquals(3, set.size());
        ArrayList<Binary> list = new ArrayList<>(set);
        assertEquals(list.get(0), Binary.wrap(a1));
        assertEquals(list.get(1), Binary.wrap(c1));
        assertEquals(list.get(2), Binary.wrap(b1));
    }

    @Test
    public void testToString() {
        assertEquals("binary(616263)", Binary.wrap(a1).toString());
    }
}
