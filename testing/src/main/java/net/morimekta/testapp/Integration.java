package net.morimekta.testapp;

import net.morimekta.util.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Test application for testing out the the integration executor.
 */
public class Integration {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("len: " + args.length);

        int sleepIndex = Arrays.binarySearch(args, "sleep");
        if (sleepIndex >= 0) {
            // This is deliberately unsafe, so we can trigger an IndexOutOfBoundsException
            // by not supplying the sleep length value.
            long delay = Long.parseLong(args[sleepIndex + 1]);
            Thread.sleep(delay);
        }

        if (Arrays.binarySearch(args, "cat") >= 0) {
            ByteArrayOutputStream in = new ByteArrayOutputStream();
            IOUtils.copy(System.in, in);

            System.out.println(in.toString());
        }

        for (String arg : args) {
            if (arg.contains("e") || arg.contains("E")) {
                System.err.println("- " + arg);
            } else {
                System.out.println("+ " + arg);
            }
        }

        if (Arrays.binarySearch(args, "error") >= 0) {
            System.exit(1);
        }
    }
}
