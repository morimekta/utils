package net.morimekta.config.impl;

import net.morimekta.config.Config;
import net.morimekta.config.LayeredConfig;
import net.morimekta.config.util.ConfigUtil;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static net.morimekta.config.util.ConfigUtil.getLayerName;

/**
 * The LayeredConfig is a non-thread-safe layered config, regarding editing.
 * Meaning each sub-config that is both modified and edited at the same time
 * needs it's own thread safety.
 * <p>
 *     <b style="color:red">NOTE:</b><em>
 *         There is a race condition that may happen if values are <b>removed</b>
 *         from the supplied config that may result in values that still exist
 *         in a lower layer for a given key to return <code>null</code> from
 *         {@link #get(String)}.</em>
 */
public class SimpleLayeredConfig implements Config, LayeredConfig {
    private final ArrayList<Supplier<Config>> layers;

    private int top;
    private int bottom;

    /**
     * Create a layered config with a predefined set of configs. The configs
     * supported are added as in-memory fixed configs in the two middle groups
     * (top and bottom).
     *
     * @param configs The configs.
     */
    public SimpleLayeredConfig(Config... configs) {
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
    public SimpleLayeredConfig(Collection<Supplier<Config>> suppliers) {
        this.layers = new ArrayList<>();
        this.layers.addAll(suppliers);

        this.top = 0;
        this.bottom = layers.size();
    }

    @Override
    public LayeredConfig addFixedTopLayer(Supplier<Config> supplier) {
        layers.add(0, supplier);
        ++top;
        ++bottom;
        return this;
    }

    @Override
    public LayeredConfig addTopLayer(Supplier<Config> supplier) {
        layers.add(top, supplier);
        ++bottom;
        return this;
    }

    @Override
    public LayeredConfig addBottomLayer(Supplier<Config> supplier) {
        layers.add(bottom, supplier);
        ++bottom;
        return this;
    }

    @Override
    public LayeredConfig addFixedBottomLayer(Supplier<Config> supplier) {
        layers.add(supplier);
        return this;
    }

    @Override
    public String getLayerFor(String key) {
        for (Supplier<Config> supplier : layers) {
            Config config = supplier.get();
            if (config.containsKey(key)) {
                return getLayerName(supplier);
            }
        }

        return null;
    }

    @Override
    public Object get(String key) {
        for (Supplier<Config> supplier : layers) {
            Config config = supplier.get();
            // TODO(morimekta): There may be a race condition if values are
            // **removed** from the supplied config. If that is not happening
            // this should be entirely thread-safe.
            if (config.containsKey(key)) {
                return config.get(key);
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        for (Supplier<Config> supplier : layers) {
            Config config = supplier.get();
            if (config.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> keySet() {
        TreeSet<String> set = new TreeSet<>();
        for (Supplier<Config> supplier : layers) {
            Config config = supplier.get();
            set.addAll(config.keySet());
        }
        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || !(o instanceof Config)) {
            return false;
        }
        return ConfigUtil.equals(this, (Config) o);
    }

    @Override
    public String toString() {
        return ConfigUtil.toString(this);
    }

    /**
     * In case sub-classes need access to the layers (in some order).
     *
     * @return The layer list, ordered from top to bottom.
     */
    protected List<Supplier<Config>> layers() {
        return ImmutableList.copyOf(layers);
    }
}
