package net.morimekta.testapp;

import net.morimekta.console.terminal.Progress;
import net.morimekta.console.terminal.ProgressManager;
import net.morimekta.console.terminal.Terminal;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Interactive {
    public static void main(String[] args) {
        try (Terminal term = new Terminal();
             ProgressManager prog = new ProgressManager(term, Progress.Spinner.BLOCKS, 5)) {
            for (int a = 0; a < 10; ++a) {
                prog.addTask("First", 1_000, p -> {
                    for (int i = 0; i < 1_000; ++i) {
                        Thread.sleep((long) (100 + (Math.random() * 10)));
                        p.accept(i);
                    }
                    return null;
                });
            }

            prog.waitAbortable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
