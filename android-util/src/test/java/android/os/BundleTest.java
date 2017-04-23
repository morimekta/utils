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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class BundleTest {
    private static class TestSerializable implements Serializable{
        private final String message;

        public TestSerializable(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || !o.getClass().equals(getClass())) return false;
            TestSerializable other = (TestSerializable) o;

            return Objects.equals(message, other.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(TestSerializable.class, message);
        }
    }

    @Test
    public void testConstructor() {
        Bundle bundle = new Bundle();

        bundle.putSerializable("a", new TestSerializable("b"));

        assertThat(bundle.describeContents(), is(0));
        assertThat(bundle.getClassLoader(), is(notNullValue()));
        assertThat(bundle.hasFileDescriptors(), is(false));
        assertThat(bundle.getSerializable("a"), is(equalTo((Serializable) new TestSerializable("b"))));

        PersistableBundle pa = new PersistableBundle();
        pa.putPersistableBundle("pb", new PersistableBundle());
        pa.putString("str", "string");

        Bundle cop = new Bundle(pa);

        assertThat(cop.getBundle("pb"), is(instanceOf(Bundle.class)));

        Bundle de = new Bundle();
        Bundle cl = new Bundle(getClass().getClassLoader());
        Bundle ca = new Bundle(100);

        assertThat(de, is(cl));
        assertThat(de, is(ca));
    }

    @Test
    public void testTypes() {
        Bundle bundle = new Bundle();
        Bundle inner = new Bundle();

        bundle.putByte("byte", (byte) 55);
        bundle.putShort("short", (short) 12345);
        bundle.putByteArray("byte_a", new byte[]{1, 2, 3, 4});
        bundle.putChar("char", '\033');
        bundle.putCharArray("char_a", new char[]{'ð', '&', '\033'});
        bundle.putCharSequence("char_s", "sequence");
        bundle.putCharSequenceArray("char_sa", new CharSequence[]{"a", "b"});

        inner.putAll(bundle);
        bundle.putBundle("bundle", inner);

        bundle.putFloat("float", 55.5f);
        bundle.putFloatArray("float_a", new float[]{1, 2, 3});
        bundle.putIntegerArrayList("int_a", list(3, 2, 1));

        ParcelUuid a = new ParcelUuid(UUID.randomUUID());
        ParcelUuid b = new ParcelUuid(UUID.randomUUID());
        bundle.putParcelable("test", a);
        bundle.putParcelableArray("test_a", new Parcelable[]{a, b});
        bundle.putParcelableArrayList("test_al", new ArrayList<>(
                ImmutableList.<Parcelable>of(b)));
        bundle.putSerializable("ser", new TestSerializable("ser"));
        bundle.putShort("short", (short) 55);
        bundle.putShortArray("short_a", new short[]{1, 2, 3, 4});
        bundle.putStringArrayList("string_al", new ArrayList<>(ImmutableList.of("a", "b")));

        // And test

        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(bundle, 0);

        Bundle parsed = parcel.readParcelable(getClass().getClassLoader());

        assertThat(parsed, is(bundle));
        assertThat(parsed.getBundle("bundle"), is(inner));
        assertThat((TestSerializable) parsed.getSerializable("ser"), is(new TestSerializable("ser")));

        parsed.getBundle("bundle").putByte("byte", (byte) 22);
        assertThat(parsed, is(not(bundle)));

        assertThat((Bundle) parsed.clone(), is(parsed));
    }

    @Test
    public void testByte() {
        Bundle bundle = new Bundle();
        bundle.putByte("byte", (byte) 55);
        bundle.putByteArray("byte_a", new byte[]{1, 2, 3, 4});
        assertThat(bundle.getByte("byte"), is((byte) 55));
        assertThat(bundle.getByte("nope"), is((byte) 0));
        assertThat(bundle.getByte("byte", (byte)77), is((byte) 55));
        assertThat(bundle.getByte("nope", (byte) 77), is((byte) 77));
        assertThat(bundle.getByteArray("byte_a"), is(new byte[]{1, 2, 3, 4}));
        assertThat(bundle.getByteArray("nope"), is(nullValue()));
    }

    @Test
    public void testChar() {
        Bundle bundle = new Bundle();
        bundle.putChar("char", (char) 55);
        bundle.putCharArray("char_a", new char[]{1, 2, 3, 4});
        assertThat(bundle.getChar("char"), is((char) 55));
        assertThat(bundle.getChar("nope"), is((char) 0));
        assertThat(bundle.getChar("char", (char)77), is((char) 55));
        assertThat(bundle.getChar("nope", (char) 77), is((char) 77));
        assertThat(bundle.getCharArray("char_a"), is(new char[]{1, 2, 3, 4}));
        assertThat(bundle.getCharArray("nope"), is(nullValue()));
    }

    @Test
    public void testCharSequence() {
        Bundle bundle = new Bundle();
        bundle.putCharSequence("char_s", "string");
        bundle.putCharSequenceArray("char_sa", new CharSequence[]{"a", "b"});
        assertThat(bundle.getCharSequence("char_s"), is((CharSequence) "string"));
        assertThat(bundle.getCharSequence("char_s", "def"), is((CharSequence) "string"));
        assertThat(bundle.getCharSequence("nope"), is(nullValue()));
        assertThat(bundle.getCharSequence("nope", "def"), is((CharSequence) "def"));
        assertThat(bundle.getCharSequenceArray("char_sa"), is(new CharSequence[]{"a", "b"}));
        assertThat(bundle.getCharSequenceArray("nope"), is(nullValue()));
    }
    
    @Test
    public void testFloat() {
        Bundle bundle = new Bundle();
        bundle.putFloat("float", (float) 55);
        bundle.putFloatArray("float_a", new float[]{1, 2, 3, 4});
        assertThat(bundle.getFloat("float"), is((float) 55));
        assertThat(bundle.getFloat("nope"), is((float) 0));
        assertThat(bundle.getFloat("float", (float)77), is((float) 55));
        assertThat(bundle.getFloat("nope", (float) 77), is((float) 77));
        assertThat(bundle.getFloatArray("float_a"), is(new float[]{1, 2, 3, 4}));
        assertThat(bundle.getFloatArray("nope"), is(nullValue()));
    }
    
    @Test
    public void testIntegerArrayList() {
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList("int_a", list(1, 2, 3));
        assertThat(bundle.getIntegerArrayList("int_a"), is(list(1, 2, 3)));
        assertThat(bundle.getIntegerArrayList("nope"), is(nullValue()));
    }

    private ArrayList<Integer> list(int... ints) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : ints) {
            list.add(i);
        }
        return list;
    }

    @Test
    public void testParcelable() {
        Bundle bundle = new Bundle();
        ParcelUuid a = new ParcelUuid(UUID.randomUUID());
        ParcelUuid b = new ParcelUuid(UUID.randomUUID());

        bundle.putParcelable("test", a);
        bundle.putParcelableArray("test_a", new ParcelUuid[]{a, b});
        bundle.putParcelableArrayList("test_l", new ArrayList<>(ImmutableList.<Parcelable>of(b)));

        assertThat(bundle.getParcelable("test"), is((Parcelable) a));
        assertThat(bundle.getParcelable("nope"), is(nullValue()));
        assertThat(bundle.getParcelableArray("test_a"), is((Parcelable[]) new ParcelUuid[]{a, b}));
        assertThat(bundle.getParcelableArray("nope"), is(nullValue()));
        assertThat(bundle.getParcelableArrayList("test_l"), is(new ArrayList<>(ImmutableList.<Parcelable>of(b))));
        assertThat(bundle.getParcelableArrayList("nope"), is(nullValue()));
    }
    
    @Test
    public void testShort() {
        Bundle bundle = new Bundle();
        bundle.putShort("short", (short) 55);
        bundle.putShortArray("short_a", new short[]{1, 2, 3, 4});
        assertThat(bundle.getShort("short"), is((short) 55));
        assertThat(bundle.getShort("nope"), is((short) 0));
        assertThat(bundle.getShort("short", (short)77), is((short) 55));
        assertThat(bundle.getShort("nope", (short) 77), is((short) 77));
        assertThat(bundle.getShortArray("short_a"), is(new short[]{1, 2, 3, 4}));
        assertThat(bundle.getShortArray("nope"), is(nullValue()));
    }

    @Test
    public void testStringArrayList() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("test", new ArrayList<>(ImmutableList.of("a", "b")));
        assertThat(bundle.getStringArrayList("test"), is(new ArrayList<>(ImmutableList.of("a", "b"))));
        assertThat(bundle.getStringArrayList("nope"), is(nullValue()));
    }
}
