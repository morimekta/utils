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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(BlockJUnit4ClassRunner.class)
public class PersistableBundleTest {
    @Test
    public void testConstructor() {

    }

    @Test
    public void testSimple() {
        PersistableBundle bundle = new PersistableBundle(100);

        bundle.putBoolean("bool", true);
        bundle.putBooleanArray("boolA", new boolean[]{true, false});
        bundle.putDouble("double", 4.1234d);
        bundle.putDoubleArray("doubleA", new double[]{1.44, 2.71, 3.14, 6.674});
        bundle.putInt("int", 1234567890);
        bundle.putIntArray("intA", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
        bundle.putLong("long", 1234567890123456789L);
        bundle.putLongArray("longA", new long[]{12L, 34L, 56L, 78L, 90L, 123L, 456L, 789L});
        bundle.putString("string", "String");
        bundle.putStringArray("stringA", new String[]{"S", "t", "r", "i", "ng"});

        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);

        PersistableBundle persisted = PersistableBundle.CREATOR.createFromParcel(parcel);

        assertTrue(persisted.getBoolean("bool"));
        assertArrayEquals(new boolean[]{true, false}, persisted.getBooleanArray("boolA"));

        assertEquals(bundle, persisted);
    }

}
