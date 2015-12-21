package android.os;

/**
 * Exception that happens while unmarshaling a parcel back to objects.
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
