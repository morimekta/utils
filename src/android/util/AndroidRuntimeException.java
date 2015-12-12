package android.util;

/**
 * @author Stein Eldar Johnsen
 * @since 11.12.15.
 */
public class AndroidRuntimeException
        extends RuntimeException {
    public AndroidRuntimeException() {
    }

    public AndroidRuntimeException(String name) {
        super(name);
    }

    public AndroidRuntimeException(String name, Throwable cause) {
        super(name, cause);
    }

    public AndroidRuntimeException(Exception cause) {
        super(cause);
    }
}
