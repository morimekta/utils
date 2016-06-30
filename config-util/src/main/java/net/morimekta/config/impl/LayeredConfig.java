package net.morimekta.config.impl;

import net.morimekta.config.Config;
import net.morimekta.config.util.ConfigUtil;

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * A config based on a layered set of configs.
 */
public class LayeredConfig implements Config {
    private final LinkedList<Supplier<Config>> layers;

    private int top;

    public LayeredConfig(Config... configs) {
        this.top = 0;
        this.layers = new LinkedList<>();
        for (Config config : configs) {
            this.layers.add(() -> config);
        }
    }

    public LayeredConfig addFixedTopLayer(Supplier<Config> supplier) {
        layers.add(0, supplier);
        ++top;
        return this;
    }

    public LayeredConfig addTopLayer(Supplier<Config> supplier) {
        layers.add(top, supplier);
        return this;
    }

    public LayeredConfig addBottomLayer(Supplier<Config> supplier) {
        layers.add(supplier);
        return this;
    }

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
            if (config.containsKey(key)) {
                return config.get(key);
            }
        }
        return false;
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
