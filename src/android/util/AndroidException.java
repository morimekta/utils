package android.util;

/**
 * @author Stein Eldar Johnsen
 * @since 11.12.15.
 */
public class AndroidException
        extends Exception {
    public AndroidException() {
    }

    public AndroidException(String name) {
        super(name);
    }

    public AndroidException(String name, Throwable cause) {
        super(name, cause);
    }

    public AndroidException(Exception cause) {
        super(cause);
    }
}
