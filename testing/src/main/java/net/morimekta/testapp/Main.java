package net.morimekta.testapp;

import net.morimekta.console.LineInput;
import net.morimekta.console.Terminal;
import net.morimekta.util.Strings;

import java.io.IOException;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (Terminal term = new Terminal()) {
            LineInput input = new LineInput(term, "Test Input", null, null, null);
            String line = input.readLine();
            if (line != null) {
                term.formatln(" -- Got: \"%s\"", Strings.escape(line));
            } else if (term.confirm("Do you o'Really?")) {
                term.println("No way!!!");
            }
        }
    }
}
