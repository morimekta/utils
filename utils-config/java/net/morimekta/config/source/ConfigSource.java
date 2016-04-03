package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;

import java.io.IOException;

/**
 * Config source interface.
 */
public interface ConfigSource {
    /**
     * Load config from the source. This is a blocking call, loading the config
     * from wherever it is, and parsing the data to provide the immutable config
     * object.
     *
     * @return The loaded config.
     */
    Config load() throws IOException, ConfigException;
}
