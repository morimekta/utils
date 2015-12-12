package android.os;

import android.util.AndroidRuntimeException;

/**
 * @author Stein Eldar Johnsen
 * @since 11.12.15.
 */
public class BadParcelableException
        extends AndroidRuntimeException {
    public BadParcelableException(String message) {
        super(message);
    }

    public BadParcelableException(Exception cause) {
        super(cause);
    }
}
