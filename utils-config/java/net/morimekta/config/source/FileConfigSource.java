package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.ConfigFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;

/**
 * File source for config objects.
 */
public class FileConfigSource implements ConfigSource {
    private static final long UPDATE_CHECK_INTERVAL_MS = 1000;

    private final File         configFile;
    private final ConfigFormat configFormat;
    private final Clock        clock;

    private long   lastCheckTimestamp;
    private long   lastModified;
    private Config config;

    public FileConfigSource(File configFile, ConfigFormat format) {
        this(configFile, format, Clock.systemUTC());
    }

    public FileConfigSource(File configFile, ConfigFormat format, Clock clock) {
        this.clock = clock;
        this.configFile = configFile;
        this.configFormat = format;
    }

    @Override
    public Config load() throws IOException, ConfigException {
        long now = clock.millis();
        if (config == null) {
            loadInternal();
            this.lastModified = Files.getLastModifiedTime(configFile.toPath()).toMillis();
            this.lastCheckTimestamp = now;
        } else if (now > (lastCheckTimestamp + UPDATE_CHECK_INTERVAL_MS)) {
            long modified = Files.getLastModifiedTime(configFile.toPath()).toMillis();
            if (modified > this.lastModified) {
                loadInternal();
                this.lastModified = modified;
            }
            this.lastCheckTimestamp = now;
        }

        return config;
    }

    private void loadInternal() throws IOException, ConfigException {
        try (FileInputStream fis = new FileInputStream(configFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            this.config = configFormat.parse(bis);
        }
    }
}
