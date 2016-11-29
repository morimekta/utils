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

import javax.annotation.Nonnull;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;

/**
 * Fake clock implementation for testing.
 */
public class FakeClock extends Clock {
    public interface TimeListener {
        void newCurrentTimeUTC(long now);
    }

    private static final Clock systemUTC = Clock.systemUTC();

    private final AtomicReference<LocalDateTime> currentTimeUTC;
    private final ZoneId                         zoneId;
    private final List<TimeListener>             listeners;

    public FakeClock() {
        currentTimeUTC = new AtomicReference<>(LocalDateTime.now(systemUTC));
        zoneId = systemUTC.getZone();
        listeners = new LinkedList<>();
    }

    private FakeClock(@Nonnull AtomicReference<LocalDateTime> currentTimeUTC,
                      @Nonnull ZoneId zoneId,
                      @Nonnull List<TimeListener> listeners) {
        this.currentTimeUTC = currentTimeUTC;
        this.zoneId = zoneId;
        this.listeners = listeners;
    }

    public static FakeClock forCurrentTimeMillis(long millis) {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(millis), systemUTC.getZone());
        return new FakeClock(
                new AtomicReference<>(LocalDateTime.now(clock)),
                Clock.systemUTC().getZone(),
                new LinkedList<>());
    }

    /**
     * Tick the fake clock the given number of milliseconds.
     *
     * @param millis Milliseconds to move the clock.
     */
    public void tick(long millis) {
        currentTimeUTC.updateAndGet(d -> d.plus(millis, ChronoUnit.MILLIS));
        long now = withZone(systemUTC.getZone()).millis();
        listeners.forEach(l -> l.newCurrentTimeUTC(now));
    }

    public void tick(long time, @Nonnull TimeUnit unit) {
        tick(max(unit.toMillis(time), 1));
    }

    public void addListener(@Nonnull TimeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(@Nonnull TimeListener listener) {
        listeners.remove(listener);
    }

    @Override @Nonnull
    public ZoneId getZone() {
        return zoneId;
    }

    @Override @Nonnull
    public FakeClock withZone(ZoneId zoneId) {
        return new FakeClock(currentTimeUTC, zoneId, listeners);
    }

    @Override @Nonnull
    public Instant instant() {
        return currentTimeUTC.get().atZone(zoneId).toInstant();
    }
}
