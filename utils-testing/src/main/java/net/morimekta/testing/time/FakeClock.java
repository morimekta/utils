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
        synchronized (currentTimeUTC) {
            currentTimeUTC.set(currentTimeUTC.get().plus(millis, ChronoUnit.MILLIS));
        }
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
