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

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class ParcelUuidTest {
    @Test
    public void testConstructor() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        ParcelUuid pu1 = new ParcelUuid(uuid1);
        ParcelUuid pu2 = ParcelUuid.fromString(uuid1.toString());
        ParcelUuid ne = new ParcelUuid(uuid2);

        assertThat(pu1.getUuid(), is(sameInstance(uuid1)));
        assertThat(pu1, is(pu2));
        assertThat(pu1.toString(), is(uuid1.toString()));
        assertThat(pu1.hashCode(), is(uuid1.hashCode()));
        assertThat(pu1.describeContents(), is(0));

        assertThat(pu1, is(not(ne)));
        assertThat(pu1.hashCode(), is(not(ne.hashCode())));
    }

    @Test
    public void testParcelable() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid uuid1 = new ParcelUuid(UUID.randomUUID());
        ParcelUuid uuid2 = ParcelUuid.fromString(UUID.randomUUID().toString());

        uuid1.writeToParcel(parcel, 0);
        parcel.writeParcelable(uuid2, 0);

        ParcelUuid parsed1 = parcel.readTypedObject(ParcelUuid.CREATOR);
        ParcelUuid parsed2 = parcel.readParcelable(getClass().getClassLoader());

        assertThat(parsed1, is(uuid1));
        assertThat(parsed2, is(uuid2));
    }
}
