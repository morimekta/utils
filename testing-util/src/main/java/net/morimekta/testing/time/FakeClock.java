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
package net.morimekta.testing.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;

/**
 * Fake clock implementation for testing.
 */
public class FakeClock extends Clock {
    private static final Clock systemUTC = Clock.systemUTC();

    private final AtomicReference<LocalDateTime> currentTimeUTC;
    private final ZoneId zoneId;

    public FakeClock() {
        currentTimeUTC = new AtomicReference<>(LocalDateTime.now(systemUTC));
        zoneId = systemUTC.getZone();
    }

    private FakeClock(AtomicReference<LocalDateTime> currentTimeUTC,
                      ZoneId zoneId) {
        this.currentTimeUTC = currentTimeUTC;
        this.zoneId = zoneId;
    }

    public static FakeClock forCurrentTimeMillis(long millis) {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(millis), systemUTC.getZone());
        return new FakeClock(
                new AtomicReference<>(LocalDateTime.now(clock)),
                Clock.systemUTC().getZone());
    }

    /**
     * Tick the fake clock the given number of milliseconds.
     *
     * @param millis Milliseconds to move the clock.
     */
    public void tick(long millis) {
        currentTimeUTC.updateAndGet(d -> d.plus(millis, ChronoUnit.MILLIS));
    }

    public void tick(long time, TimeUnit unit) {
        tick(max(unit.toMillis(time), 1));
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public FakeClock withZone(ZoneId zoneId) {
        return new FakeClock(currentTimeUTC, zoneId);
    }

    @Override
    public Instant instant() {
        return currentTimeUTC.get().atZone(zoneId).toInstant();
    }
}
