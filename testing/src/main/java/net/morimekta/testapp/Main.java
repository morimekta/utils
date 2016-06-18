package net.morimekta.testapp;

import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import net.morimekta.console.util.TerminalSize;
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

        try (STTYModeSwitcher ms = new STTYModeSwitcher(STTYMode.RAW)) {
            console.append("Term: ")
                   .append(TerminalSize.get()
                                       .toString())
                   .begin()
                   .appendln("Test 2\rA")
                   .appendln("Test 3")
                   .end()
                   .newline()
                   .flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
