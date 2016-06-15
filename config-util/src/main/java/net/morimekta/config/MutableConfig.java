package net.morimekta.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look parent more than one level into the map (if referencing config objects
 * within the config object). This way, if the config contains a config object
 * on key 'b', then deepGetString('b.c') will look for key 'c' in the config 'b'.
 *
 * <ul>
 *   <li>The get* and put* methods work as on the map itself.
 *   <li>The deepGet* and deepPut methods work deep into the config.
 *   <li>The mutableConfig and deepMutableConfig replaces put and
 *       deepPut for config. Note: The putConfig method is still there
 *       in case a soft reference is desired.
 * </ul>
 *
 * The map is mutable as it is there to be used for manipulating the config during
 * parsing and
 */
public class MutableConfig extends Config {
    public static final String SUPER = "super";

    /**
     * Create a config instance.
     */
    public MutableConfig() {
        this(null, null);
    }

    /**
     * Create a config instance.
     *
     * @param parent The parent (parent) config.
     */
    public MutableConfig(MutableConfig parent) {
        this(parent, null);
    }

    /**
     * Create a config instance.
     *
     * @param parent The parent (parent) config.
     * @param base The base config (or super-config).
     */
    public MutableConfig(MutableConfig parent, Config base) {
        this.map = new TreeMap<>();
        this.parent = parent;
        this.base = base;
    }

    /**
     * Get the base config.
     * @return The base config.
     */
    public Config getBase() {
        return base;
    }

    /**
     * Get a config value with deep lookup. If any of the keys are missing,
     * the config will be created.
     *
     * @param key The recursive key to look parent.
     * @return The config value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public MutableConfig mutableConfig(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            return localMutableConfig(parts[0]).mutableConfig(parts[1]);
        }
        return localMutableConfig(key);
    }

    /**
     * Put a boolean value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putBoolean(String key, boolean value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putBoolean(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put an integer value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putInteger(String key, int value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putInteger(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a long value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putLong(String key, long value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putLong(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a double value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putDouble(String key, double value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putDouble(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a string value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putString(String key, String value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putString(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a sequence value deep into the config.
     *
     * @param key The recursive key to put at.
     * @param value The value to put.
     * @return The config.
     * @throws IncompatibleValueException If an intermediate step was not a
     *         config.
     */
    public MutableConfig putSequence(String key, Sequence value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putSequence(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a config value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    private MutableConfig putConfig(String key, Config value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putConfig(parts[1], value);
        } else {
            map.put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putValue(String key, Value value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            localMutableConfig(parts[0]).putValue(parts[1], value);
        } else {
            map.put(key, value);
        }
        return this;
    }

    @Override
    public MutableConfig getParent() {
        return parent;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Set<String> keySet() {
        Set<String> ks = new HashSet<>(map.keySet());
        if (base != null) {
            ks.addAll(base.keySet());
        }
        return ks;
    }

    @Override
    public Set<Entry> entrySet() {
        return keySet().stream()
                       .map(k -> new ImmutableEntry(k, getValue(k)))
                       .collect(Collectors.toSet());
    }

    @Override
    public Value getValue(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            return localGetConfig(parts[0]).getValue(parts[1]);
        }
        return localGetValue(key);
    }

    @Override
    public boolean containsKey(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            return localContainsKey(parts[0]) && localGetConfig(parts[0]).containsKey(parts[1]);
        }
        return localContainsKey(key);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append('(');

        boolean first = true;
        for (String key : keySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(key)
                   .append(":")
                   .append(map.get(key).value.toString());
        }

        builder.append(')');
        return builder.toString();
    }

    // --- private
    private final Map<String, Value> map;
    private final MutableConfig parent;
    private final Config base;

    private Value localGetValue(String key) {
        if (UP.equals(key)) {
            if (parent == null) {
                throw new KeyNotFoundException("");
            }
            return Value.create(parent);
        } else if (SUPER.equals(key)) {
            if (base == null) {
                throw new KeyNotFoundException("");
            }
            return Value.create(base);
        } else if (!map.containsKey(key)) {
            if (base != null && base.containsKey(key)) {
                return base.getValue(key);
            }
            throw new KeyNotFoundException("No such key " + key);
        }
        return map.get(key);
    }

    private Config localGetConfig(String key) {
        return localGetValue(key).asConfig();
    }

    private boolean localContainsKey(String key) {
        if (map.containsKey(key)) {
            return true;
        } else if (UP.equals(key) && parent != null) {
            return true;
        } else if (SUPER.equals(key) && base != null) {
            return true;
        }
        return false;
    }

    private MutableConfig localMutableConfig(String key) {
        MutableConfig cfg;
        if (!map.containsKey(key)) {
            if (base != null && base.containsKey(key)) {
                cfg = new MutableConfig(this, base.getConfig(key));
            } else {
                cfg = new MutableConfig(this);
            }
            putConfig(key, cfg);
        } else {
            Config existing = map.get(key).asConfig();
            if (existing instanceof MutableConfig) {
                cfg = (MutableConfig) existing;
            } else {
                cfg = new MutableConfig(this, existing);
                putConfig(key, cfg);
            }
        }
        return cfg;
    }
}
