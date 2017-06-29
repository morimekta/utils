package net.morimekta.testapp;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.chr.Unicode;
import net.morimekta.console.terminal.InputSelection;
import net.morimekta.console.terminal.Progress;
import net.morimekta.console.terminal.Terminal;
import net.morimekta.util.ExtraStreams;
import net.morimekta.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Interactive {
    public static void main(String[] args) {
        try (Terminal term = new Terminal()) {
            term.println("Press any char to continue... After done.");
            term.println();
            ExecutorService exec = Executors.newSingleThreadExecutor();
            term.executeAbortable(exec, () -> {
                try {
                    Progress progress = new Progress(term, Progress.Spinner.ARROWS, "Progress", 1234567890);
                    for (int i = 1; i <= 1234567890; i += 1234567) {
                        Thread.sleep(5L);
                        progress.update(i);
                    }
                    progress.update(1234567890);
                    term.println();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });

            term.println();

            List<String> entries = ExtraStreams.range(0x0000, 0x4000, 4)
                                               .mapToObj(i -> {
                                                   // [ 0xd000 -> 0xe000 > is reserved for utf-16 funkyness.
                                                   if (0xd000 <= i && i < 0xe000) return "";
                                                   StringBuilder builder = new StringBuilder();
                                                   for (int c = i; c < i + 4; ++c) {
                                                       Unicode u  = new Unicode(c);
                                                       String  cs = u.toString();

                                                       // Actual control chars.
                                                       if (u.asInteger() < 0x100 && u.printableWidth() == 0) {
                                                           cs = "<*>";
                                                       }

                                                       builder.append("| ");
                                                       builder.append(CharUtil.leftJust(format(
                                                               "0x%04x:  %-8s  \"%s\"",
                                                               u.asInteger(),
                                                               "'" + u.asString() + "'",
                                                               cs), 26));
                                                   }
                                                   builder.append('|');
                                                   return builder.toString();
                                               }).filter(f -> !f.isEmpty()).collect(Collectors.toList());
            List<InputSelection.Command<String>> commands = new ArrayList<>();

            commands.add(new InputSelection.Command<>(Char.CR,
                                                      "select",
                                                      (e, p) -> InputSelection.Reaction.SELECT,
                                                      true));
            commands.add(new InputSelection.Command<>('r', "reverse", (e, p) -> {
                Collections.reverse(entries);
                return InputSelection.Reaction.UPDATE_KEEP_ITEM;
            }));
            commands.add(new InputSelection.Command<>('q', "quit", (e, p) -> InputSelection.Reaction.EXIT));

            InputSelection.EntryPrinter<String> printer = (e, c) -> e;

            InputSelection<String> input = new InputSelection<>(term, "Select a line...", entries, commands, printer);
            String                 line  = input.select();
            if (line != null) {
                term.formatln(" -- Got: \"%s\"", Strings.escape(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
