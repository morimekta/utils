package net.morimekta.config;

/**
 * Created by morimekta on 2/25/16.
 */
public class IncompatibleValueException extends ConfigException {
    public IncompatibleValueException(String message, Object... params) {
        super(message, params);
    }

    public IncompatibleValueException(Throwable throwable, String message, Object... params) {
        super(throwable, message, params);
    }
}
