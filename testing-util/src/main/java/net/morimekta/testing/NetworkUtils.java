package net.morimekta.testing;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.fail;

/**
 * Networking utilities for testing.
 */
public class NetworkUtils {
    public static int findFreePort() {
        int port = -1;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        } catch (IOException e) {
            fail("Unable to locate free port.");
        }
        return port;
    }

    public static HttpRequestFactory httpFactory() {
        return httpTransport().createRequestFactory();
    }

    public static HttpRequestFactory httpFactory(HttpRequestInitializer initializer) {
        return httpTransport().createRequestFactory(initializer);
    }

    public static HttpTransport httpTransport() {
        return new ApacheHttpTransport();
    }

    // PRIVATE constructor defeats instantiation.
    private NetworkUtils() {}
}
