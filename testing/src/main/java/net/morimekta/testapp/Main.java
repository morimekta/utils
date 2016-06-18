package net.morimekta.testapp;

import net.morimekta.console.Terminal;
import net.morimekta.util.io.IndentedPrintWriter;

import java.io.IOException;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        IndentedPrintWriter console = new IndentedPrintWriter(System.out, "    ", "\r\n");

        try (Terminal term = new Terminal()) {
            if (term.confirm("Do you o'Really?")) {
                term.println("No way!!!");
            }
        }
    }
}
