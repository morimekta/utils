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

import java.io.Serializable;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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

        assertThat(bundle.getSerializable("a"), is(equalTo((Serializable) new TestSerializable("b"))));
    }
}
