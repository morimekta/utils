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
 * Show progress on a single task in how many percent (with spinner and
 * progress-bar). Spinner type is configurable.
 */
public class Progress {
    /**
     * Which spinner to show. Some may require extended unicode font to
     * be used in the console without just showing '?'.
     */
    public enum Spinner {
        /**
         * Simple ASCII spinner using '|', '/', '-', '\'. This variant will
         * <b>always</b> work.
         */
        ASCII,
        /**
         * Using a block
         */
        BLOCKS,
        /**
         * Use Unicode clock symbols, 0x1f550 -&gt; 0x1f55b:
         * '🕐', '🕑', '🕒', '🕓', '🕔', '🕕', '🕖', '🕗', '🕘', '🕙', '🕚', '🕛'
         */
        CLOCK,
    }

    private static final Char[] ascii_spinner = {
            new Unicode('|'),
            new Unicode('/'),
            new Unicode('-'),
            new Unicode('\\')
    };
    private static final Char[] block_spinner = {
            new Unicode(0x2581),  // 1/8 block
            new Unicode(0x2582),  // 2/8 block
            new Unicode(0x2583),  // ...
            new Unicode(0x2584),  //
            new Unicode(0x2585),  //
            new Unicode(0x2586),  // ...
            new Unicode(0x2587),  // 7/8 block
            new Unicode(0x2588),  // 8/8 (full) block
            new Unicode(0x2587),  // 7/8 block
            new Unicode(0x2586),  // ...
            new Unicode(0x2585),  //
            new Unicode(0x2584),  //
            new Unicode(0x2583),  // ...
            new Unicode(0x2582)   // 2/8 block
    };
    private static final Char[] clock_spinner = {
            new Unicode(0x1f550),  // 1 o'clock
            new Unicode(0x1f551),  // ...
            new Unicode(0x1f552),
            new Unicode(0x1f553),
            new Unicode(0x1f554),
            new Unicode(0x1f555),
            new Unicode(0x1f556),
            new Unicode(0x1f557),
            new Unicode(0x1f558),
            new Unicode(0x1f559),
            new Unicode(0x1f55a),  // ...
            new Unicode(0x1f55b)   // 12 o'clock
    };

    private final Terminal terminal;
    private final Char[]   spinner;
    private final long     total;
    private final long     start;
    private final Clock    clock;
    private final String   what;

    private int spinner_pos;
    private long last_update;

    public Progress(Terminal terminal,
                    String what,
                    long total) {
        this(terminal, Clock.systemUTC(), Spinner.ASCII, what, total);
    }

    public Progress(Terminal terminal,
                    Clock clock,
                    Spinner spinner,
                    String what,
                    long total) {
        this.terminal = terminal;
        if (spinner == null) {
            this.spinner = ascii_spinner;
        } else {
            switch (spinner) {
                case ASCII:
                    this.spinner = ascii_spinner;
                    break;
                case BLOCKS:
                    this.spinner = block_spinner;
                    break;
                case CLOCK:
                    this.spinner = clock_spinner;
                    break;
                default:
                    this.spinner = ascii_spinner;
                    break;
            }
        }

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
