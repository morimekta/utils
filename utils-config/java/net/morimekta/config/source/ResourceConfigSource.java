package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigFormat;

import java.io.IOException;
import java.io.InputStream;

/**
 * File source for config objects.
 */
public class ResourceConfigSource implements ConfigSource {
    private Config config;

    private final String       resource;
    private final ConfigFormat configFormat;

    public ResourceConfigSource(String resource, ConfigFormat format) {
        this.resource = resource;
        this.configFormat = format;
    }

    @Override
    public Config load() throws IOException, ConfigException {
        if (config == null) {
            loadInternal();
        }
        return config;
    }

    private void loadInternal() throws IOException, ConfigException {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            this.config = configFormat.parse(in);
        }
    }
}
