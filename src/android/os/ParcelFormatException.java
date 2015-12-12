package android.os;

/**
 * @author Stein Eldar Johnsen
 * @since 11.12.15.
 */
public class ParcelFormatException
        extends RuntimeException {
    public ParcelFormatException() {
        super();
    }

    public ParcelFormatException(String reason) {
        super(reason);
    }
}
