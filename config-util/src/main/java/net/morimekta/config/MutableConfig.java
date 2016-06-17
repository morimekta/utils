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
    /**
     * Create an empty config instance.
     */
    public MutableConfig() {
        this(null, null);
    }

    /**
     * Create an empty config instance with parent.
     *
     * @param parent The parent (parent) config.
     */
    public MutableConfig(MutableConfig parent) {
        this(parent, null);
    }

    /**
     * Create an empty config instance with parent and base.
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
     * Get the parent config used for 'up' navigation.
     * @return The parent config.
     */
    public Config getParent() {
        return parent;
    }

    /**
     * Get the base config used for 'super' navigation.
     * @return The base config.
     */
    public Config getBase() {
        return base;
    }

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putBoolean(String key, boolean value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putInteger(String key, int value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putLong(String key, long value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putDouble(String key, double value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putString(String key, String value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put a sequence value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putSequence(String key, Sequence value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Put a config value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putConfig(String key, Config value) {
        map.put(key, Value.create(value));
        return this;
    }

    /**
     * Get a mutable config value. If a config does not exists for the given
     * key, one is created. If a value that is not a config exists for the key
     * an exception is thrown.
     *
     * @param key The recursive key to look parent.
     * @return The config value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public MutableConfig mutableConfig(String key) {
        MutableConfig cfg;
        if (!map.containsKey(key)) {
            if (base != null && base.containsKey(key)) {
                cfg = new MutableConfig(this, base.getConfig(key));
            } else {
                cfg = new MutableConfig(this);
            }
            map.put(key, Value.create(cfg));
        } else {
            Config existing = map.get(key).asConfig();
            if (existing instanceof MutableConfig) {
                cfg = (MutableConfig) existing;
            } else {
                cfg = new MutableConfig(this, existing);
                map.put(key, Value.create(cfg));
            }
        }
        return cfg;
    }

    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putValue(String key, Value value) {
        map.put(key, value);
        return this;
    }

    public MutableConfig clear() {
        map.clear();
        return this;
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
        if (!map.containsKey(key)) {
            if (base != null && base.containsKey(key)) {
                return base.getValue(key);
            }
            throw new KeyNotFoundException("No such key " + key);
        }
        return map.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key) ||
               (base != null && base.containsKey(key));
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

}
