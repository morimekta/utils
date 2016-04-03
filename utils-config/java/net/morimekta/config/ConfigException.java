package net.morimekta.config;

/**
 * Created by morimekta on 2/25/16.
 */
public class ConfigException extends Exception {
    public ConfigException(String message, Object... params) {
        super(String.format(message, params));
    }

    public ConfigException(Throwable throwable, String message, Object... params) {
        super(String.format(message, params), throwable);
    }
}
