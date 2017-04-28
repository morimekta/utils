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
package net.morimekta.console.test_utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;

/**
 * Fake clock implementation for testing.
 */
public class FakeClock extends Clock {
    public FakeClock() {
        this(systemUTC.millis());
    }

    /**
     * Tick the fake clock the given number of milliseconds.
     *
     * @param tickMs Milliseconds to move the clock.
     */
    public void tick(final long tickMs) {
        currentTimeUTC.updateAndGet(d -> d.plus(max(1, tickMs), ChronoUnit.MILLIS));
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

    // -----------------------

    private static final Clock systemUTC = Clock.systemUTC();

    private final AtomicReference<LocalDateTime> currentTimeUTC;
    private final ZoneId                         zoneId;

    private FakeClock(long millis) {
        this(Clock.fixed(Instant.ofEpochMilli(millis), systemUTC.getZone()));
    }

    private FakeClock(Clock clock) {
        this(LocalDateTime.now(clock));
    }

    private FakeClock(LocalDateTime now) {
        this(new AtomicReference<>(now),
             systemUTC.getZone());
    }

    private FakeClock(AtomicReference<LocalDateTime> currentTimeUTC,
                      ZoneId zoneId) {
        this.currentTimeUTC = currentTimeUTC;
        this.zoneId = zoneId;
    }
}
