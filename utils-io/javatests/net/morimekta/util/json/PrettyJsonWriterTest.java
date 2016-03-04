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
package net.morimekta.util.json;

import net.morimekta.util.Binary;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * Created by morimekta on 2/17/16.
 */
public class PrettyJsonWriterTest {
    @Test
    public void testEmpty() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrettyJsonWriter writer = new PrettyJsonWriter(baos);

        writer.object();
        writer.endObject();

        writer.flush();

        assertEquals("{}", new String(baos.toByteArray(), UTF_8));

        writer.reset();
        baos.reset();

        writer.array();
        writer.endArray();

        writer.flush();

        assertEquals("[]", new String(baos.toByteArray(), UTF_8));
    }

    @Test
    public void testSimple() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrettyJsonWriter writer = new PrettyJsonWriter(baos);

        writer.object();

        writer.key(true).value(false);
        writer.key((byte) 'a').value((byte) 'b');
        writer.key((short) 123).value((short) 321);
        writer.key(321).value(123);
        writer.key(1.234).value(4.321);
        writer.key(1234567890123456789L).value(987654321098765432L);
        writer.key("A").value("B");
        writer.key(Binary.fromHexString("cafebabe"))
              .value(Binary.fromHexString("1337"));
        writer.keyLiteral("LitA").valueLiteral("LitB");

        writer.endObject();

        writer.flush();

        assertEquals("{\n" +
                     "    \"true\": false,\n" +
                     "    \"97\": 98,\n" +
                     "    \"123\": 321,\n" +
                     "    \"321\": 123,\n" +
                     "    \"1.234\": 4.321,\n" +
                     "    \"1234567890123456789\": 987654321098765432,\n" +
                     "    \"A\": \"B\",\n" +
                     "    \"yv66vg\": \"Ezc\",\n" +
                     "    LitA: LitB\n" +
                     "}", new String(baos.toByteArray(), UTF_8));

        writer.reset();
        baos.reset();

        writer.array();

        writer.value(true);
        writer.value(1234);
        writer.valueLiteral("LitC");

        writer.endArray();

        writer.flush();

        assertEquals("[\n" +
                     "    true,\n" +
                     "    1234,\n" +
                     "    LitC\n" +
                     "]", new String(baos.toByteArray(), UTF_8));
    }

    @Test
    public void testLayered() throws JsonException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrettyJsonWriter writer = new PrettyJsonWriter(baos);

        writer.object();

        writer.key(123);
        writer.array();
        writer.value(2);
        writer.value(3);
        writer.endArray();

        writer.key(321);
        writer.array();
        writer.value(5);
        writer.value(8);
        writer.endArray();

        writer.endObject();

        writer.flush();

        assertEquals("{\n" +
                     "    \"123\": [\n" +
                     "        2,\n" +
                     "        3\n" +
                     "    ],\n" +
                     "    \"321\": [\n" +
                     "        5,\n" +
                     "        8\n" +
                     "    ]\n" +
                     "}", new String(baos.toByteArray(), UTF_8));
    }
}
