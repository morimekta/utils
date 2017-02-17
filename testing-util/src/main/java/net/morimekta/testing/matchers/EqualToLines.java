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
package net.morimekta.testing.matchers;

import org.hamcrest.core.IsEqual;

/**
 * Equality matcher that that ignores changes in line separators.
 */
public class EqualToLines extends IsEqual<String> {
    public EqualToLines(String expected) {
        // replace all '\r\n' with '\n' so windows & *nix line output is normalized to the same.
        super(normalize(expected));
    }

    @Override
    public boolean matches(Object o) {
        if (o == null || !(CharSequence.class.isAssignableFrom(o.getClass()))) return false;

        // replace all '\r\n' with '\n' so windows & *nix line output is normalized to the same.
        return super.matches(normalize(o.toString()));
    }

    private static String normalize(Object str) {
        if (str == null) {
            return null;
        }
        return str.toString().replaceAll("\\r?\\n", System.lineSeparator());
    }
}
