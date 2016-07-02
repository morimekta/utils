package net.morimekta.config.impl;

import net.morimekta.config.Config;
import net.morimekta.config.source.FileConfigSupplier;
import net.morimekta.config.source.RefreshingFileConfigSupplier;
import net.morimekta.config.source.ResourceConfigSupplier;
import net.morimekta.config.util.ConfigUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * A config based on a layered set of configs. The config has four layer
 * groups, two above and two below the <em>middle</em>.
 *
 * <ul>
 *     <li><b>fixed-top</b>: [w] Layers</li>
 *     <li>--top--</li>
 *     <li><b>top</b>: [x] Layers</li>
 *     <li>--middle--</li>
 *     <li><b>bottom</b>: [y] Layers</li>
 *     <li>--bottom--</li>
 *     <li><b>fixed-bottom</b>: [z] Layers</li>
 * </ul>
 *
 * When layers are added, they are added to one fo the four groups, and inserted
 * furthest <b>away</b> from the middle. The middle is actually just a theoretical
 * position, as the 'top' and 'bottom' groups insert away from the line.
 * <p>
 * The LayeredConfig is NOT thread-safe regarding editing. And each sub-config
 * that is both modified and edited at the same time needs it's own thread
 * safety.
 * </p>
 * <p>
 *     <b style="color:red">NOTE:</b><em>
 *         There is a race condition that may happen if values are <b>removed</b>
 *         from the supplied config that may result in values that still exist
 *         in a lower layer for a given key to return <code>null</code> from
 *         {@link #get(String)}.</em>
 * </p>
 */
public class LayeredConfig implements Config {
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
    public LayeredConfig(Config... configs) {
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
    public LayeredConfig(Collection<Supplier<Config>> suppliers) {
        this.layers = new ArrayList<>();
        this.layers.addAll(suppliers);

        this.top = 0;
        this.bottom = layers.size();
    }

    /**
     * Add a new fixed top layer. The new config will be handled before all
     * other configs added before this.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    public LayeredConfig addFixedTopLayer(Supplier<Config> supplier) {
        layers.add(0, supplier);
        ++top;
        ++bottom;
        return this;
    }

    /**
     * Add a new top layer. The new config will be handled before the
     * other top configs added before this, but after fixed-top configs.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    public LayeredConfig addTopLayer(Supplier<Config> supplier) {
        layers.add(top, supplier);
        ++bottom;
        return this;
    }

    /**
     * Add a new bottom layer. The new config will be handled after the
     * other bottom configs added before this, but before fixed-bottom
     * configs.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    public LayeredConfig addBottomLayer(Supplier<Config> supplier) {
        layers.add(bottom, supplier);
        ++bottom;
        return this;
    }

    /**
     * Add a new fixed bottom layer. The new config will be handled after all
     * other configs added before this.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    public LayeredConfig addFixedBottomLayer(Supplier<Config> supplier) {
        layers.add(supplier);
        return this;
    }

    /**
     * Get the 'toString()' value for the config supplier for the layer that
     * contains the wanted key. If the supplier is a lambda expression we
     * assume it is just providing the config from memory.
     *
     * See the toString entries for the different config sources (See:
     * {@link FileConfigSupplier#toString()},
     * {@link RefreshingFileConfigSupplier#toString()},
     * {@link ResourceConfigSupplier#toString()}).
     *
     * @param key The key to get layer for.
     * @return The layer number and name.
     */
    public String getLayerFor(String key) {
        for (Supplier<Config> supplier : layers) {
            Config config = supplier.get();
            if (config.containsKey(key)) {
                String str = supplier.toString();
                if (str.contains("$$Lambda$")) {
                    return String.format("InMemorySupplier{%s}",
                                         config.getClass().getSimpleName());
                }
                return str;
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
}
