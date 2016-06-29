package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * File source for config objects.
 */
public class FileConfigSupplier implements Supplier<Config> {
    private static final long UPDATE_CHECK_INTERVAL_MS = 1000;

    private final File         configFile;
    private final ConfigParser parser;
    private final AtomicReference<Config> config;

    public FileConfigSupplier(File configFile, ConfigParser format) {
        this.configFile = configFile;
        this.parser = format;
        this.config = new AtomicReference<>(loadInternal());
    }

    @Override
    public Config get() {
        return config.get();
    }

    /**
     * Reload the config value.
     */
    public void reload() {
        this.config.set(loadInternal());
    }

    private Config loadInternal() {
        try (FileInputStream fis = new FileInputStream(configFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            return parser.parse(bis);
        } catch (IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }
}
