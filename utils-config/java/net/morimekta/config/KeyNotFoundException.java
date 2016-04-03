package net.morimekta.config;

/**
 * Created by morimekta on 2/25/16.
 */
public class KeyNotFoundException extends ConfigException {
    public KeyNotFoundException(String message, Object... params) {
        super(message, params);
    }
}
