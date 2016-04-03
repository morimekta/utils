package net.morimekta.testing.time;

import org.junit.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Testing the fake clock.
 */
public class FakeClockTest {
    @Test
    public void testFakeClock() {
        long now = System.currentTimeMillis();
        FakeClock clock = new FakeClock();

        assertEquals(clock.millis(), now, 10D);
    }

    @Test
    public void testForCurrentTimeMillis() {
        long now = System.currentTimeMillis();
        FakeClock clock = FakeClock.forCurrentTimeMillis(now);

        assertEquals(clock.millis(), now);
    }

    @Test
    public void testForTimezone() {
        FakeClock clock = FakeClock.forCurrentTimeMillis(1234567890000L);

        assertEquals(1234567890000L, clock.millis());
        assertEquals(1234567890000L, clock.instant().toEpochMilli());
        // Z, aka Zulu time, aka UTC.
        assertEquals("Z", clock.getZone().getId());

        Clock oslo = clock.withZone(ZoneId.of("Europe/Oslo"));

        // The difference equals UTC -> CET (not CEST).
        assertEquals(3600000L, 1234567890000L - oslo.millis());
    }

    @Test
    public void testTick() {
        FakeClock clock = FakeClock.forCurrentTimeMillis(1234567890000L);

        clock.tick(1234);

        assertEquals(1234567891234L, clock.millis());
        assertEquals(1234567891234L, clock.instant().toEpochMilli());
        // Z, aka Zulu time, aka UTC.
        assertEquals("Z", clock.getZone().getId());

        Clock oslo = clock.withZone(ZoneId.of("Europe/Oslo"));

        // The difference equals UTC -> CET (not CEST).
        assertEquals(3600000L, 1234567891234L - oslo.millis());

        clock.tick(1234, TimeUnit.SECONDS);

        assertEquals(1234569125234L, clock.millis());
        assertEquals(1234569125234L, clock.instant().toEpochMilli());
        // Z, aka Zulu time, aka UTC.
        assertEquals("Z", clock.getZone().getId());

        // The difference equals UTC -> CET (not CEST).
        assertEquals(3600000L, 1234569125234L - oslo.millis());
    }
}
