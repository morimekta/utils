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
package net.morimekta.testapp;

import net.morimekta.testing.IntegrationExecutor;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * TODO(morimekta): Make a real class description.
 */
public class MainIT {
    private IntegrationExecutor executor;

    @Before
    public void setUp() throws IOException {
        executor = new IntegrationExecutor("testing", "testing.jar");
    }

    @Test
    public void testMain() throws IOException {
        assertEquals(0, executor.run());
        assertEquals("len: 0\n", executor.getOutput());
        assertEquals("", executor.getError());
    }

    @Test
    public void testMain_withArgs() throws IOException {
        assertEquals(0, executor.run("first", "second", "third", "fourth"));

        assertEquals("len: 4\n" +
                     "+ first\n" +
                     "+ third\n" +
                     "+ fourth\n", executor.getOutput());
        assertEquals("- second\n", executor.getError());
    }
}
