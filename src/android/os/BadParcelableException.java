package android.os;

import android.util.AndroidRuntimeException;

/**
 * Exception thrown then the parcelable contains data unsuited for parcelling.
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
