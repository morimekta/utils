/*
 * Copyright (c) 2017, Stein Eldar Johnsen
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

import java.util.stream.IntStream;

/**
 * Extra stream utilities.
 */
public class ExtraStreams {
    /**
     * Make a range stream from 0 to (but not including) N.
     *
     * @param N the number of iterations.
     * @return The stream.
     */
    public static IntStream times(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("Invalid recurrence count: " + N);
        }
        return IntStream.range(0, N);
    }

    // PRIVATE constructor to defeat instantiation.
    private ExtraStreams() {}
}
