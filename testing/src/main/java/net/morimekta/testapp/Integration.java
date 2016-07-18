package net.morimekta.testapp;

import java.io.IOException;

/**
 * Test application for testing out the the integration executor.
 */
public class Integration {
    public static void main(String[] args) throws IOException {
        System.out.println("len: " + args.length);

        for (String arg : args) {
            if (arg.contains("e") || arg.contains("E")) {
                System.err.println("- " + arg);
            } else {
                System.out.println("+ " + arg);
            }
        }
    }
}
