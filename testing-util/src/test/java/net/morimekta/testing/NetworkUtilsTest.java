package net.morimekta.testing;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class NetworkUtilsTest {
    @Test
    public void testFindFreePort() {
        int port = NetworkUtils.findFreePort();

        try (ServerSocket socket = new ServerSocket(port)) {
            assertEquals(port, socket.getLocalPort());
        } catch (IOException e) {
            fail("Unable to locate free port.");
        }
    }

    @Test
    public void testHttpFactory() throws IOException {
        HttpRequestFactory factory = NetworkUtils.httpFactory();

        assertEquals(ApacheHttpTransport.class, factory.getTransport().getClass());
        assertNull(factory.buildGetRequest(new GenericUrl("http://localhost/")).getInterceptor());

        factory = NetworkUtils.httpFactory(rq -> rq.setInterceptor(rq2 -> {}));
        assertNotNull(factory.buildGetRequest(new GenericUrl("http://localhost/")).getInterceptor());
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = NetworkUtils.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }
}
