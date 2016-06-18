package net.morimekta.testapp;

import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import net.morimekta.util.io.IndentedPrintWriter;

import java.io.IOException;

/**
 * Test application for testing out the console functionality.
 * Since much of the console IO is dependent on a unix-like
 * terminal to work, it cannot be truly tested in a unit.
 */
public class Main {
    public static void main(String[] args) {
        IndentedPrintWriter console = new IndentedPrintWriter(System.out, "    ", "\r\n");

        try (STTYModeSwitcher ms = new STTYModeSwitcher(STTYMode.RAW)) {
            console.append("Test 1");
            console.begin();
            console.appendln("Test 2\rA");
            console.appendln("Test 3");
            console.end();
            console.newline();
            console.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
