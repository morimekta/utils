package net.morimekta.testapp;

import net.morimekta.console.InputSelection;
import net.morimekta.console.Terminal;
import net.morimekta.console.chr.Char;
import net.morimekta.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Interactive {
    public static void main(String[] args) throws IOException {
        try (Terminal term = new Terminal()) {
            List<String> entries = new ArrayList<>();
            for (int i = 0; i < 100; ++i) {
                entries.add("line number --- " + (i + 1) + "...");
            }

            List<InputSelection.Command<String>> commands = new ArrayList<>();

            commands.add(new InputSelection.Command<>(Char.CR, "select", (e, p) -> InputSelection.Reaction.SELECT, true));
            commands.add(new InputSelection.Command<>('r', "reverse", (e, p) -> {
                Collections.reverse(entries);
                return InputSelection.Reaction.UPDATE_KEEP_ITEM;
            }));
            commands.add(new InputSelection.Command<>('q', "quit", (e, p) -> InputSelection.Reaction.EXIT));

            InputSelection.EntryPrinter<String> printer = (e, c) -> e;

            InputSelection<String> input = new InputSelection<>(term, "Select a line...", entries, commands, printer);
            String line = input.select();
            if (line != null) {
                term.formatln(" -- Got: \"%s\"", Strings.escape(line));
            }
        }
    }
}
