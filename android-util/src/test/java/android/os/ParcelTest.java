/*
 * Copyright (c) 2016, Stein Eldar johnsen
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

package android.os;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(BlockJUnit4ClassRunner.class)
public class ParcelTest {
    @After
    public void tearDown() {
        Parcel.clearPool();
    }

    @Test
    public void testObtain() {
        Parcel parcel = Parcel.obtain();
        Parcel other = Parcel.obtain();

        // Creates new instances when the pool is empty.
        assertNotSame(parcel, other);

        parcel.recycle();

        Parcel other2 = Parcel.obtain();

        // Make sure the object is reused.
        assertSame(parcel, other2);

        List<Parcel> parcels = new LinkedList<>();
        for (int i = 0; i < (2 * Parcel.kMaxPoolSize); ++i) {
            parcels.add(Parcel.obtain());
        }
        for (Parcel p : parcels) {
            p.recycle();
        }
    }

    @Test
    public void testBasics() {
        Parcel parcel = Parcel.obtain();
        Parcel other = Parcel.obtain();

        parcel.setDataCapacity(100);
        other.setDataCapacity(10);

        assertThat(parcel.dataCapacity(), is(100));
        assertThat(other.dataCapacity(), is(10));

        assertNotEquals(parcel, other);
        assertNotEquals(parcel, null);
        assertNotEquals(parcel, new Object());

        parcel.writeString("a string");
        other.writeString("another");

        assertThat(other.dataCapacity(), is(11));
        assertThat(parcel.dataAvail(), is(12));
        assertThat(parcel.dataPosition(), is(0));

        parcel.appendFrom(other, 0, other.dataSize());

        assertThat(parcel.dataAvail(), is(23));

        assertThat(parcel.readString(), is("a string"));
        assertThat(parcel.dataPosition(), is(12));
        assertThat(parcel.readString(), is("another"));
        assertThat(parcel.dataPosition(), is(23));

        parcel.setDataPosition(12);

        assertThat(parcel.readString(), is("another"));

        try {
            parcel.setDataCapacity(20);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("New capacity 20 is smaller than current data size: 23"));
        }
        try {
            parcel.setDataPosition(30);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("New position 30 is after last known byte: 23"));
        }
        try {
            parcel.setDataPosition(-30);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Negative position -30"));
        }

        parcel.setDataSize(30);
        assertThat(parcel.dataSize(), is(30));
        assertThat(parcel.readInt(), is(0));

        parcel.setDataSize(30);
        assertThat(parcel.dataSize(), is(30));
        parcel.setDataSize(12);
        parcel.setDataPosition(0);

        assertThat(parcel.readString(), is("a string"));
        try {
            parcel.readString();
            fail("no exception");
        } catch (ParcelFormatException e) {
            assertThat(e.getMessage(), is("Unable to read 4 bytes at position 12 only 0 bytes available"));
        }

        assertThat(parcel.marshall(),
                   is(new byte[]{8, 0, 0, 0, 97, 32, 115, 116, 114, 105, 110, 103}));
        other.setDataPosition(0);
        other.unmarshall(new byte[]{8, 0, 0, 0, 97, 32, 115, 116, 114, 105, 110, 103},
                         0, 12);

        assertThat(other.readString(), is("a string"));
    }

    @Test
    public void testByte() {
        Parcel parcel = Parcel.obtain();

        parcel.writeByte((byte) 4);
        parcel.writeByte((byte) -4);
        parcel.writeByte((byte) 125);
        parcel.writeByte((byte) -128);
        assertEquals(4, parcel.dataSize());

        assertEquals((byte) 4, parcel.readByte());
        assertEquals((byte) -4, parcel.readByte());
        assertEquals((byte) 125, parcel.readByte());
        assertEquals((byte) -128, parcel.readByte());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testDouble() {
        Parcel parcel = Parcel.obtain();

        parcel.writeDouble(-7.8d);
        parcel.writeDouble(7.8d);
        parcel.writeDouble(1797693048934892789238E287);
        parcel.writeDouble(-1797693048934892789238E287);
        assertEquals(4 * 8, parcel.dataSize());

        assertEquals(-7.8d, parcel.readDouble(), 0.000000001);
        assertEquals(7.8d, parcel.readDouble(), 0.000000001);
        assertEquals(1797693048934892789238E287, parcel.readDouble(), 0.000000001);
        assertEquals(-1797693048934892789238E287, parcel.readDouble(), 0.000000001);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testFloat() {
        Parcel parcel = Parcel.obtain();

        parcel.writeFloat(-7.8f);
        parcel.writeFloat(7.8f);
        parcel.writeFloat(33489348927892E25f);
        parcel.writeFloat(-33489348927892E25f);
        assertEquals(4 * 4, parcel.dataSize());

        assertEquals(-7.8d, parcel.readFloat(), 0.0001);
        assertEquals(7.8d, parcel.readFloat(), 0.0001);
        assertEquals(33489348927892E25f, parcel.readFloat(), 0.0001);
        assertEquals(-33489348927892E25f, parcel.readFloat(), 0.0001);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testInt() {
        Parcel parcel = Parcel.obtain();

        parcel.writeInt(8);
        parcel.writeInt(-8);
        parcel.writeInt(2048934892);
        parcel.writeInt(-2048934892);
        assertEquals(4 * 4, parcel.dataSize());

        assertEquals(8, parcel.readInt());
        assertEquals(-8, parcel.readInt());
        assertEquals(2048934892, parcel.readInt());
        assertEquals(-2048934892, parcel.readInt());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testLong() {
        Parcel parcel = Parcel.obtain();

        parcel.writeLong(8);
        parcel.writeLong(-8);
        parcel.writeLong(8548934892113347344L);
        parcel.writeLong(-8548934892113347344L);
        assertEquals(4 * 8, parcel.dataSize());

        assertEquals(8, parcel.readLong());
        assertEquals(-8, parcel.readLong());
        assertEquals(8548934892113347344L, parcel.readLong());
        assertEquals(-8548934892113347344L, parcel.readLong());
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testString() {
        Parcel parcel = Parcel.obtain();

        parcel.writeString("blaargh");
        parcel.writeString("");
        parcel.writeString("\0something\0");
        parcel.writeString("asdjkh asdjkh aSLJKDH JHKL asdjkhg asdjkhg asdjkhg asdjkhgf asdjkhgf asdjkhgf sajkdhgf skajdhgf asdjhkgf asdjkhg sdjkh");
        assertEquals(152, parcel.dataSize());

        assertEquals("blaargh", parcel.readString());
        assertEquals("", parcel.readString());
        assertEquals("\0something\0", parcel.readString());
        assertEquals("asdjkh asdjkh aSLJKDH JHKL asdjkhg asdjkhg asdjkhg asdjkhgf asdjkhgf asdjkhgf sajkdhgf skajdhgf asdjhkgf asdjkhg sdjkh", parcel.readString());
        assertEquals(0, parcel.dataAvail());

        parcel.recycle();
        parcel = Parcel.obtain();

        parcel.writeString(null);
        assertThat(parcel.readString(), is(nullValue()));
    }

    @Test
    public void testBooleanArray() {
        Parcel parcel = Parcel.obtain();

        boolean[] arr1 = new boolean[]{};
        boolean[] arr2 = new boolean[]{true, false, true, false, true, false, true, false, true, false, true, false};

        parcel.writeBooleanArray(arr1);
        parcel.writeBooleanArray(arr2);

        assertArrayEquals(arr1, parcel.createBooleanArray());
        assertArrayEquals(arr2, parcel.createBooleanArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);

        boolean[] arr3 = new boolean[arr2.length];
        parcel.readBooleanArray(arr3);
        assertThat(arr3, is(new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false}));

        parcel.readBooleanArray(arr3);
        assertThat(arr3, is(new boolean[]{true, false, true, false, true, false, true, false, true, false, true, false}));
    }

    @Test
    public void testByteArray() {
        Parcel parcel = Parcel.obtain();

        byte[] arr1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0x10, 0x20, 0x50, (byte) 0xff, -123};
        byte[] arr2 = new byte[]{8, 7, 6};

        parcel.writeByteArray(arr1);
        parcel.writeByteArray(arr2);
        parcel.writeByteArray(arr1, 3, 7);

        assertArrayEquals(arr1, parcel.createByteArray());
        assertArrayEquals(arr2, parcel.createByteArray());
        assertArrayEquals(new byte[]{4, 5, 6, 7, 8, 9, 0}, parcel.createByteArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);

        byte[] arr3 = new byte[5];
        parcel.readByteArray(arr3);
        assertThat(arr3, is(new byte[]{1, 2, 3, 4, 5}));
    }

    @Test
    public void testCharArray() {
        Parcel parcel = Parcel.obtain();

        char[] arr1 = new char[]{'a', 'b', 'ý', 'æ', '\0', 'G', '\n'};
        char[] arr2 = new char[]{};

        parcel.writeCharArray(arr1);
        parcel.writeCharArray(arr2);

        assertArrayEquals(arr1, parcel.createCharArray());
        assertArrayEquals(arr2, parcel.createCharArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        char[] arr3 = new char[]{0, 0, 0, 0, 0};
        parcel.readCharArray(arr3);
        assertThat(arr3, is(new char[]{'a', 'b', 'ý', 'æ', '\0'}));
    }

    @Test
    public void testDoubleArray() {
        Parcel parcel = Parcel.obtain();

        double[] arr1 = new double[]{-7.8d, 7.8d, -1.797693048934892789238E308, 0.24703286234234E-323};
        double[] arr2 = new double[]{};

        parcel.writeDoubleArray(arr1);
        parcel.writeDoubleArray(arr2);

        assertArrayEquals(arr1, parcel.createDoubleArray(), 0.000000001);
        assertArrayEquals(arr2, parcel.createDoubleArray(), 0.000000001);
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        double[] arr3 = new double[3];
        parcel.readDoubleArray(arr3);
        assertThat(arr3, is(new double[]{-7.8d, 7.8d, -1.797693048934892789238E308}));
    }

    @Test
    public void testFloatArray() {
        Parcel parcel = Parcel.obtain();

        float[] arr1 = new float[]{-7.8f, 7.8f, -3.4028231753567E38f, 7.006499E-46f};
        float[] arr2 = new float[]{};

        parcel.writeFloatArray(arr1);
        parcel.writeFloatArray(arr2);

        assertArrayEquals(arr1, parcel.createFloatArray(), 0.000000001f);
        assertArrayEquals(arr2, parcel.createFloatArray(), 0.000000001f);
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        float[] arr3 = new float[3];
        parcel.readFloatArray(arr3);
        assertThat(arr3, is(new float[]{-7.8f, 7.8f, -3.4028231753567E38f}));
    }

    @Test
    public void testIntArray() {
        Parcel parcel = Parcel.obtain();

        int[] arr1 = new int[]{-78, 78, -340282317, 700649900};
        int[] arr2 = new int[]{};

        parcel.writeIntArray(arr1);
        parcel.writeIntArray(arr2);

        assertArrayEquals(arr1, parcel.createIntArray());
        assertArrayEquals(arr2, parcel.createIntArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        int[] arr3 = new int[2];
        parcel.readIntArray(arr3);
        assertThat(arr3, is(new int[]{-78, 78}));
    }

    @Test
    public void testLongArray() {
        Parcel parcel = Parcel.obtain();

        long[] arr1 = new long[]{-78, 78, -3402823174735833452L, 3402823174735833452L};
        long[] arr2 = new long[]{};

        parcel.writeLongArray(arr1);
        parcel.writeLongArray(arr2);

        assertArrayEquals(arr1, parcel.createLongArray());
        assertArrayEquals(arr2, parcel.createLongArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        long[] arr3 = new long[2];
        parcel.readLongArray(arr3);
        assertThat(arr3, is(new long[]{-78L, 78L}));
    }

    @Test
    public void testStringArray() {
        Parcel parcel = Parcel.obtain();

        String[] arr1 = new String[]{};
        String[] arr2 = new String[]{"all", "work", "and", "no", "play", "makes", "jack", "a", "dull", "boy"};

        parcel.writeStringArray(arr1);
        parcel.writeStringArray(arr2);

        assertArrayEquals(arr1, parcel.createStringArray());
        assertArrayEquals(arr2, parcel.createStringArray());
        assertEquals(0, parcel.dataAvail());

        parcel.setDataPosition(0);
        parcel.createStringArray();
        String[] arr3 = new String[2];
        parcel.readStringArray(arr3);
        assertThat(arr3, is(new String[]{"all", "work"}));
    }

    @Test
    public void testParcelable() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid parcelable = new ParcelUuid(UUID.randomUUID());

        parcel.writeParcelable(parcelable, 0);

        ParcelUuid copy = parcel.readParcelable(ClassLoader.getSystemClassLoader());

        assertNotSame(parcelable, copy);
        assertEquals(parcelable, copy);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testParcelableArray() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid[] arr = new ParcelUuid[400];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = new ParcelUuid(UUID.randomUUID());
        }

        parcel.writeParcelableArray(arr, 0);

        ParcelUuid[] out = parcel.readParcelableArray(ClassLoader.getSystemClassLoader());

        assertEquals(arr.length, out.length);

        for (int i = 0; i < out.length; ++i) {
            assertEquals(arr[i], out[i]);
        }
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testTypedObject() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid parcelable = new ParcelUuid(UUID.randomUUID());
        parcel.writeTypedObject(parcelable, 0);

        ParcelUuid copy = parcel.readTypedObject(ParcelUuid.CREATOR);

        assertNotSame(parcelable, copy);
        assertEquals(parcelable, copy);
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testTypedArray() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid[] arr = new ParcelUuid[400];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = new ParcelUuid(UUID.randomUUID());
        }

        parcel.writeTypedArray(arr, 0);

        ParcelUuid[] out = parcel.createTypedArray(ParcelUuid.CREATOR);

        assertEquals(arr.length, out.length);

        for (int i = 0; i < out.length; ++i) {
            assertEquals(arr[i], out[i]);
        }
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testTypedArrayList() {
        Parcel parcel = Parcel.obtain();

        ArrayList<ParcelUuid> list = new ArrayList<>(400);
        for (int i = 0; i < 400; ++i) {
            ParcelUuid tmp = new ParcelUuid(UUID.randomUUID());
            list.add(tmp);
        }

        parcel.writeTypedList(list);

        ArrayList<ParcelUuid> out = parcel.createTypedArrayList(ParcelUuid.CREATOR);

        assertEquals(list.size(), out.size());

        for (int i = 0; i < out.size(); ++i) {
            ParcelUuid a = list.get(i);
            ParcelUuid b = out.get(i);
            assertEquals(a, b);
        }
        assertEquals(0, parcel.dataAvail());
    }

    @Test
    public void testExtra() {
        assertThat(new ParcelFormatException().getMessage(),
                   is(nullValue()));
        assertThat(new BadParcelableException("a").getMessage(),
                   is("a"));
        assertThat(new BadParcelableException(new IOException("e")).getMessage(),
                   is("e"));
    }
}
