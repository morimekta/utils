package net.morimekta.console.terminal;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Color;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.util.Strings;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Created by morimekta on 28.04.17.
 */
public class Progress {
    private final Terminal terminal;
    private final Char[]   spinner;
    private final long     total;
    private final long     start;
    private final Clock    clock;
    private final String   what;

    private int spinner_pos;
    private long last_update;

    public Progress(Terminal terminal,
                    Clock clock,
                    String what,
                    long total) {
        this.terminal = terminal;
        this.spinner = new Char[]{
                new Unicode('○'),
                new Unicode('◔'),
                new Unicode('◑'),
                new Unicode('◕'),
                new Unicode('●'),
                new Unicode('◕'),
                new Unicode('◑'),
                new Unicode('◔')
        };
        this.what = what;
        this.spinner_pos = 0;
        this.total = total;
        this.start = clock.millis();
        this.clock = clock;

        terminal.finish();
        update(0);
    }

    public void update(long current) {
        long now = clock.millis();
        if (current > total) current = total;

        double fraction = ((double) current) / ((double) total);
        int pct = (int) (fraction * 100.0);

        if (current < total) {
            if (now - last_update < 100) {
                return;
            }

            int remaining_pct = 100 - pct;

            long duration_ms = clock.millis() - start;
            Duration remaining = null;
            if (duration_ms > 3000) {
                long assumed_total = (long) (((double) duration_ms) / fraction);
                long remaining_ms = assumed_total - duration_ms;
                remaining = Duration.of(remaining_ms, ChronoUnit.MILLIS);
            }

            spinner_pos = (spinner_pos + 1) % spinner.length;

            if (pct < 100) {
                terminal.format("\r%s%s: [%s%s%s%s%s%s%s%s] %3d%%%s",
                                Control.CURSOR_ERASE,
                                what,
                                Color.GREEN,
                                Strings.times("#", pct),
                                new Color(Color.YELLOW, Color.BOLD),
                                spinner[spinner_pos],
                                Color.CLEAR, Color.YELLOW,
                                Strings.times("-", remaining_pct - 1),
                                Color.CLEAR,
                                pct,
                                remaining == null ? "" : " +(" + format(remaining) + ")");
            } else {
                terminal.format("\r%s%s: [%s%s%s%s%s] 100%%%s",
                                Control.CURSOR_ERASE,
                                what,
                                Color.GREEN,
                                Strings.times("#", 99),
                                new Color(Color.YELLOW, Color.BOLD),
                                spinner[spinner_pos],
                                Color.CLEAR,
                                remaining == null ? "" : " +(" + format(remaining) + ")");
            }
            last_update = now;
        } else {
            if (now >= last_update) {
                terminal.format("\r%s%s: [%s%s%s] 100%% @ %s",
                                Control.CURSOR_ERASE,
                                what,
                                Color.GREEN,
                                Strings.times("#", 100),
                                Color.CLEAR,
                                format(Duration.of(now - start, ChronoUnit.MILLIS)));
            }
            last_update = Long.MAX_VALUE;
        }
    }

    private String format(Duration duration) {
        long h = duration.toHours();
        long m = duration.minusHours(h).toMinutes();
        if (h > 0) {
            return String.format("%2d:%02d H  ", h, m);
        }
        long s = duration.minusHours(h).minusMinutes(m).getSeconds();
        if (m > 0) {
            return String.format("%2d:%02d min", m, s);
        }
        long ms = duration.minusHours(h).minusMinutes(m).minusSeconds(s).toMillis();
            return String.format("%3d.%1d s  ", s, ms / 100);
    }
}
