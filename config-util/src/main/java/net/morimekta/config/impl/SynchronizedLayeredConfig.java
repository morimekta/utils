package net.morimekta.config.impl;

import net.morimekta.config.Config;
import net.morimekta.config.KeyNotFoundException;
import net.morimekta.config.LayeredConfig;
import net.morimekta.config.util.ConfigUtil;
import net.morimekta.util.concurrent.ReadWriteMutex;
import net.morimekta.util.concurrent.ReentrantReadWriteMutex;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static net.morimekta.config.util.ConfigUtil.getLayerName;

/**
 * The SynchronizedLayeredConfig is a thread-safe layered config. It has an
 * internal synchronization mechanism using a read-write lock to avoid
 * tampering with data during read operations, and to ensure writes (layer
 * insertions) are atomic. Though it does not ensure that it's contained
 * configs are thread-safe, it has made a core part of the synchronization
 * available for callers.
 */
public class SynchronizedLayeredConfig implements Config, LayeredConfig, ReadWriteMutex {
    private final ArrayList<Supplier<Config>> layers;
    private final ReadWriteMutex mutex;

    private volatile int top;
    private volatile int bottom;

    /**
     * Create a layered config with a predefined set of configs. The configs
     * supported are added as in-memory fixed configs in the two middle groups
     * (top and bottom).
     *
     * @param configs The configs.
     */
    public SynchronizedLayeredConfig(Config... configs) {
        this.mutex = new ReentrantReadWriteMutex();
        this.layers = new ArrayList<>();
        for (Config config : configs) {
            this.layers.add(() -> config);
        }
        this.top = 0;
        this.bottom = layers.size();
    }

    /**
     * Create a layered config with the given suppliers as the initial middle
     * two groups of layers.
     *
     * @param suppliers The config suppliers.
     */
    public SynchronizedLayeredConfig(Collection<Supplier<Config>> suppliers) {
        this.mutex = new ReentrantReadWriteMutex();
        this.layers = new ArrayList<>();
        this.layers.addAll(suppliers);

        this.top = 0;
        this.bottom = layers.size();
    }

    @Override
    public <V> V lockForReading(Supplier<V> callable) {
        return mutex.lockForReading(callable);
    }

    @Override
    public void lockForWriting(Runnable runnable) {
        mutex.lockForWriting(runnable);
    }

    @Override
    public SynchronizedLayeredConfig addFixedTopLayer(Supplier<Config> supplier) {
        lockForWriting(() -> {
            layers.add(0, supplier);
            ++top;
            ++bottom;
        });
        return this;
    }

    @Override
    public SynchronizedLayeredConfig addTopLayer(Supplier<Config> supplier) {
        lockForWriting(() -> {
            layers.add(top, supplier);
            ++bottom;
        });
        return this;
    }

    @Override
    public SynchronizedLayeredConfig addBottomLayer(Supplier<Config> supplier) {
        lockForWriting(() -> {
            layers.add(bottom, supplier);
            ++bottom;
        });
        return this;
    }

    @Override
    public SynchronizedLayeredConfig addFixedBottomLayer(Supplier<Config> supplier) {
        lockForWriting(() -> layers.add(supplier));
        return this;
    }

    @Override
    public String getLayerFor(String key) {
        return lockForReading(() -> {
            for (Supplier<Config> supplier : layers) {
                Config config = supplier.get();
                if (config.containsKey(key)) {
                    return getLayerName(supplier);
                }
            }

            return null;
        });
    }

    @Override
    public Object get(String key) {
        return getWithDefault(key, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return lockForReading(() -> {
            if (!containsKey(key)) {
                throw new KeyNotFoundException("No such config entry \"" + key + "\"");
            }
            return (T) get(key);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getWithDefault(String key, T def) {
        return lockForReading(() -> {
            for (Supplier<Config> supplier : layers) {
                Config config = supplier.get();
                if (config.containsKey(key)) {
                    return (T) config.get(key);
                }
            }
            return def;
        });
    }

    @Override
    public boolean containsKey(String key) {
        return lockForReading(() -> {
            for (Supplier<Config> supplier : layers) {
                Config config = supplier.get();
                if (config.containsKey(key)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public Set<String> keySet() {
        return lockForReading(() -> {
            TreeSet<String> set = new TreeSet<>();
            for (Supplier<Config> supplier : layers) {
                Config config = supplier.get();
                set.addAll(config.keySet());
            }
            return set;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || !(o instanceof Config)) {
            return false;
        }
        return lockForReading(() -> ConfigUtil.equals(this, (Config) o));
    }

    @Override
    public String toString() {
        return lockForReading(() -> ConfigUtil.toString(this));
    }

    /**
     * In case sub-classes need access to the layers (in some order).
     *
     * @return The layer list, ordered from top to bottom.
     */
    protected List<Supplier<Config>> layers() {
        return lockForReading(() -> ImmutableList.copyOf(layers));
    }
}
