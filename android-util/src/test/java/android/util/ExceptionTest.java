package android.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ExceptionTest {
    @Test
    public void testAndroidException() {
        AndroidException a = new AndroidException("message");
        AndroidException b = new AndroidException(a);
        AndroidException c = new AndroidException("message", a);
        AndroidException d = new AndroidException();

        assertThat(a.getMessage(), is("message"));
        assertThat(b.getMessage(), is("android.util.AndroidException: message"));
        assertThat(c.getMessage(), is("message"));
        assertThat(d.getMessage(), is(nullValue()));

        assertThat(a.getCause(), is(nullValue()));
        assertThat(b.getCause(), is((Throwable) a));
        assertThat(c.getCause(), is((Throwable) a));
        assertThat(d.getCause(), is(nullValue()));
    }
    @Test
    public void testAndroidRuntimeException() {
        AndroidRuntimeException a = new AndroidRuntimeException("message");
        AndroidRuntimeException b = new AndroidRuntimeException(a);
        AndroidRuntimeException c = new AndroidRuntimeException("message", a);
        AndroidRuntimeException d = new AndroidRuntimeException();

        assertThat(a.getMessage(), is("message"));
        assertThat(b.getMessage(), is("android.util.AndroidRuntimeException: message"));
        assertThat(c.getMessage(), is("message"));
        assertThat(d.getMessage(), is(nullValue()));

        assertThat(a.getCause(), is(nullValue()));
        assertThat(b.getCause(), is((Throwable) a));
        assertThat(c.getCause(), is((Throwable) a));
        assertThat(d.getCause(), is(nullValue()));
    }
}
