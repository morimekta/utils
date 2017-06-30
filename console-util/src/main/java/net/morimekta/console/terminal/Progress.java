package net.morimekta.console.terminal;

import com.google.common.annotations.VisibleForTesting;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Color;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.util.Strings;

import javax.annotation.Nonnull;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.IntSupplier;

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
         * work in any terminal.
         */
        ASCII(new Unicode('#'),
              new Unicode('-'),
              new Unicode[] {
                      new Unicode('|'),
                      new Unicode('/'),
                      new Unicode('-'),
                      new Unicode('\\')
              }),

        /**
         * Using a block char that bounces up and down to show progress.
         * Not exactly <i>spinning</i>, but does the job. Using unicode
         * chars 0x2581 -&gt; 0x2588;
         * <p>
         * '▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'
         */
        BLOCKS(new Unicode('▓'),
               new Unicode('⋅'),
               new Unicode[] {
                       new Unicode('▁'),  // 1/8 block
                       new Unicode('▂'),  // 2/8 block
                       new Unicode('▃'),  // ...
                       new Unicode('▄'),  //
                       new Unicode('▅'),  //
                       new Unicode('▆'),  // ...
                       new Unicode('▇'),  // 7/8 block
                       new Unicode('█'),  // 8/8 (full) block
                       new Unicode('▇'),  // 7/8 block
                       new Unicode('▆'),  // ...
                       new Unicode('▅'),  //
                       new Unicode('▄'),  //
                       new Unicode('▂'),  // ...
                       new Unicode('▁'),  // 2/8 block
               }),

        /**
         * A spinning arrow. Using chars in range 0x2b60 -&gt; 0x2b69:
         * <p>
         * '⭢', '⭨', '⭣', '⭩', '⭠', '⭦', '⭡', '⭧'
         */
        ARROWS(new Unicode('⬛'),
               new Unicode('⋅'),
               new Unicode[] {
                       new Unicode('⭢'),
                       new Unicode('⭨'),
                       new Unicode('⭣'),
                       new Unicode('⭩'),
                       new Unicode('⭠'),
                       new Unicode('⭦'),
                       new Unicode('⭡'),
                       new Unicode('⭧'),
               }),

        /**
         * Use Unicode clock symbols, 0x1f550 -&gt; 0x1f55b:
         * <p>
         * '🕐', '🕑', '🕒', '🕓', '🕔', '🕕', '🕖', '🕗', '🕘', '🕙', '🕚', '🕛'
         */
        CLOCK(new Unicode('⬛'),
              new Unicode('⋅'),
              new Unicode[] {
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
              }),

        ;

        private Char   done;
        private Char   remain;
        private Char[] spinner;

        Spinner(Char done,
                Char remain,
                Char[] spinner) {
            this.done = done;
            this.spinner = spinner;
            this.remain = remain;
        }
    }

    private final Terminal    terminal;
    private final Char[]      spinner;
    private final Char        done_chr;
    private final Char        remain_chr;
    private final long        total;
    private final long        start;
    private final Clock       clock;
    private final String      title;
    private final LinePrinter updater;
    private final IntSupplier terminalWidthSupplier;

    private int spinner_pos;
    private int last_pct;
    private int last_pts;
    private long last_update;

    /**
     * Create a progress bar using the given terminal.
     *
     * @param terminal The terminal to use.
     * @param spinner The spinner to use.
     * @param title The title of the progress.
     * @param total The total progress value.
     */
    public Progress(@Nonnull Terminal terminal,
                    @Nonnull Spinner spinner,
                    @Nonnull String title,
                    long total) {
        this(terminal,
             null,
             () -> terminal.getTTY().getTerminalSize().cols,
             Clock.systemUTC(),
             spinner,
             title,
             total);
    }

    /**
     * Create a progress bar using the line printer and width supplier.
     *
     * @param updater The line printer used to update visible progress.
     * @param widthSupplier The width supplier to get terminal width from.
     * @param spinner The spinner to use.
     * @param title The title of the progress.
     * @param total The total progress value.
     */
    public Progress(@Nonnull LinePrinter updater,
                    @Nonnull IntSupplier widthSupplier,
                    @Nonnull Spinner spinner,
                    @Nonnull String title,
                    long total) {
        this(null, updater, widthSupplier, Clock.systemUTC(), spinner, title, total);
    }

    @Deprecated
    public Progress(LinePrinter updater,
                    Spinner spinner,
                    String title,
                    long total) {
        this(updater, () -> 128, spinner, title, total);
    }

    /**
     * Update the progress to reflect the current progress value.
     *
     * @param current The new current progress value.
     */
    public void update(long current) {
        long now = clock.millis();
        if (current > total) current = total;
        int pts_w = terminalWidthSupplier.getAsInt() - 23 - title.length();

        double fraction = ((double) current) / ((double) total);
        int pct = (int) (fraction * 100);
        int pts = (int) (fraction * pts_w);

        if (current < total) {
            if (now - last_update < 100 && pct == last_pct && pts == last_pts) {
                return;
            }

            int remaining_pts = pts_w - pts;

            long     duration_ms = clock.millis() - start;
            Duration remaining   = null;
            if (duration_ms > 3000) {
                long assumed_total = (long) (((double) duration_ms) / fraction);
                long remaining_ms  = assumed_total - duration_ms;
                remaining = Duration.of(remaining_ms, ChronoUnit.MILLIS);
            }

            spinner_pos = (spinner_pos + 1) % spinner.length;

            if (pts < pts_w) {
                updater.formatln("%s: [%s%s%s%s%s%s%s%s] %3d%%%s",
                                 title,
                                 Color.GREEN,
                                 Strings.times(done_chr.toString(), pts),
                                 new Color(Color.YELLOW, Color.BOLD),
                                 spinner[spinner_pos],
                                 Color.CLEAR, Color.YELLOW,
                                 Strings.times(remain_chr.toString(), remaining_pts - 1),
                                 Color.CLEAR,
                                 pct,
                                 remaining == null ? "" : " +(" + format(remaining) + ")");
            } else {
                updater.formatln("%s: [%s%s%s%s%s] 100%%%s",
                                 title,
                                 Color.GREEN,
                                 Strings.times(done_chr.toString(), pts_w - 1),
                                 new Color(Color.YELLOW, Color.BOLD),
                                 spinner[spinner_pos],
                                 Color.CLEAR,
                                 remaining == null ? "" : " +(" + format(remaining) + ")");
            }
            last_pct = pct;
            last_pts = pts;
            last_update = now;
        } else {
            if (now >= last_update) {
                updater.formatln("%s: [%s%s%s] 100%% @ %s",
                                 title,
                                 Color.GREEN,
                                 Strings.times(done_chr.toString(), pts),
                                 Color.CLEAR,
                                 format(Duration.of(now - start, ChronoUnit.MILLIS)));
            }
            last_update = Long.MAX_VALUE;
            last_pct = 100;
        }
    }

    /**
     * Create a progress updater. Note that <b>either</b> terminal or the
     * updater param must be set.
     *
     * @param terminal The terminal to print to.
     * @param updater The updater to write to.
     * @param widthSupplier The width supplier to get terminal width from.
     * @param clock The clock to use for timing.
     * @param spinner The spinner type.
     * @param title What progresses.
     * @param total The total value to be 'progressed'.
     */
    @VisibleForTesting
    protected Progress(Terminal terminal,
                       LinePrinter updater,
                       IntSupplier widthSupplier,
                       Clock clock,
                       Spinner spinner,
                       String title,
                       long total) {
        this.terminal = terminal;
        this.terminalWidthSupplier = widthSupplier;
        this.updater = updater != null ? updater : this::println;
        this.spinner = getSpinner(spinner);
        this.done_chr = getDoneChar(spinner);
        this.remain_chr = getRemainChar(spinner);
        this.title = title;
        this.spinner_pos = 0;
        this.total = total;
        this.start = clock.millis();
        this.clock = clock;
        this.last_pct = -1;
        this.last_pts = -1;

        if (terminal != null) {
            terminal.finish();
        }
        update(0);
    }

    private Char getRemainChar(Spinner spinner) {
        if (spinner == null) {
            spinner = Spinner.ASCII;
        }
        return spinner.remain;
    }

    private Char getDoneChar(Spinner spinner) {
        if (spinner == null) {
            spinner = Spinner.ASCII;
        }
        return spinner.done;
    }

    private Char[] getSpinner(Spinner spinner) {
        if (spinner == null) {
            spinner = Spinner.ASCII;
        }
        return spinner.spinner;
    }

    private void println(String line) {
        terminal.print("\r" + Control.CURSOR_ERASE + line);
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
