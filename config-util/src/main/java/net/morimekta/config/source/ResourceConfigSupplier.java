package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class ResourceConfigSupplier implements Supplier<Config> {
    private final Config config;

    public ResourceConfigSupplier(String resource, ConfigParser parser) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            this.config = parser.parse(in);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    @Override
    public Config get() {
        return config;
    }
}
