package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base class for config formatters.
 */
public interface ConfigFormat {
    void format(OutputStream out, Config config) throws ConfigException, IOException;

    Config parse(InputStream in) throws ConfigException, IOException;
}
