package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigParser;

import com.google.common.base.MoreObjects;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class ResourceConfigSupplier implements Supplier<Config> {
    private final Config config;
    private final String resource;

    public ResourceConfigSupplier(String resource, ConfigParser parser) {
        this.resource = resource;
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            this.config = parser.parse(in);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("resource", resource)
                          .toString();
    }

    @Override
    public Config get() {
        return config;
    }
}
