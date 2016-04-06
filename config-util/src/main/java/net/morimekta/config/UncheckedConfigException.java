package net.morimekta.config;

/**
 * Created by steineldar on 3/1/16.
 */
public class UncheckedConfigException extends RuntimeException {
    public UncheckedConfigException(ConfigException e) {
        super(e);
    }

    public ConfigException getCause() {
        return (ConfigException) super.getCause();
    }
}
