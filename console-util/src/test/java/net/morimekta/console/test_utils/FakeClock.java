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

import javax.annotation.Nonnull;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Fake clock implementation for testing.
 */
public class FakeClock extends Clock {
    public interface TimeListener {
        void newCurrentTimeUTC(long now);
    }

    public FakeClock() {
        this(systemUTC.millis());
    }

    /**
     * Tick the fake clock the given number of milliseconds.
     *
     * @param tickMs Milliseconds to move the clock.
     */
    public void tick(final long tickMs) {
        untilTimeUTC.updateAndGet(d -> d.plus(max(1, tickMs), ChronoUnit.MILLIS));
        if (inTick.get()) {
            // avoid recursion. Just let the other call (currently in the
            // block below) take care of the extra time.
            return;
        }
        inTick.set(true);
        try {
            // Tick the clock along in 100 millis blocks. This is to be able to
            // spread out the 'now' timestamps seen while ticking along.
            while (untilTimeUTC.get().isAfter(currentTimeUTC.get())) {
                final long now   = currentTimeUTC.get().toInstant(ZoneOffset.UTC).toEpochMilli();
                final long until = untilTimeUTC.get().toInstant(ZoneOffset.UTC).toEpochMilli();
                final long skip  = min(100, until - now);

                currentTimeUTC.updateAndGet(d -> d.plus(skip, ChronoUnit.MILLIS));
                listeners.forEach(l -> l.newCurrentTimeUTC(now + skip));
            }
        } finally {
            inTick.set(false);
        }
    }

    public void addListener(@Nonnull TimeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override @Nonnull
    public ZoneId getZone() {
        return zoneId;
    }

    @Override @Nonnull
    public FakeClock withZone(ZoneId zoneId) {
        return new FakeClock(currentTimeUTC, untilTimeUTC, zoneId, listeners, inTick);
    }

    @Override @Nonnull
    public Instant instant() {
        return currentTimeUTC.get().atZone(zoneId).toInstant();
    }

    // -----------------------

    private static final Clock systemUTC = Clock.systemUTC();

    private final AtomicReference<LocalDateTime> currentTimeUTC;
    private final AtomicReference<LocalDateTime> untilTimeUTC;
    private final ZoneId                         zoneId;
    private final List<TimeListener>             listeners;
    private final AtomicBoolean                  inTick;

    private FakeClock(long millis) {
        this(Clock.fixed(Instant.ofEpochMilli(millis), systemUTC.getZone()));
    }

    private FakeClock(Clock clock) {
        this(LocalDateTime.now(clock));
    }

    private FakeClock(LocalDateTime now) {
        this(new AtomicReference<>(now),
             new AtomicReference<>(now),
             systemUTC.getZone(),
             new LinkedList<>(),
             new AtomicBoolean());
    }

    private FakeClock(@Nonnull AtomicReference<LocalDateTime> currentTimeUTC,
                      @Nonnull AtomicReference<LocalDateTime> untilTimeUTC,
                      @Nonnull ZoneId zoneId,
                      @Nonnull List<TimeListener> listeners,
                      @Nonnull AtomicBoolean inTick) {
        this.currentTimeUTC = currentTimeUTC;
        this.untilTimeUTC = untilTimeUTC;
        this.zoneId = zoneId;
        this.listeners = listeners;
        this.inTick = inTick;
    }
}
