package net.morimekta.config;

import java.util.TreeMap;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look up more than one level into the map (if referencing config objects
 * within the config object). This way, if the config contains a config object
 * on key 'b', then getString('b.c') will look for key 'c' in the config 'b'.
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and type unsafe getters.
 */
public class Config extends TreeMap<String, Value> {
    public static final String UP = "up";

    public Config() {
        this.up = null;
    }

    public Config(Config up) {
        this.up = up;
    }

    public Config(Config up, Config copy) {
        super(copy);
        this.up = up;
    }

    /**
     * Presence sensitive type checker.
     * @param key
     * @return
     */
    public Value.Type typeOf(String key) {
        return getValue(key).type;
    }

    /**
     * Get the value contained in this config object.
     * @param key The simple key to look for.
     * @return The value instance.
     * @throws KeyNotFoundException
     */
    public Value getValue(String key) {
        if (!containsKey(key)) {
            throw new KeyNotFoundException("No such key " + key);
        }
        return get(key);
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance.
     *
     * E.g.
     *
     * <code>
     * Value v = deepGetValue("up.args");
     * </code>
     *
     * Will first navigate one step "up", then try to find the value "args".
     *
     * @param key The key to look for.
     * @return The value found.
     */
    public Value deepGetValue(String key) {
        try {
            return deepGetValueInternal(key);
        } catch (KeyNotFoundException e) {
            throw new KeyNotFoundException("No such entry " + key + " in config, " + e.getMessage());
        }
    }

    private Value deepGetValueInternal(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (super.containsKey(parts[0])) {
                return getConfig(parts[0]).deepGetValueInternal(parts[1]);
            } else if (UP.equals(parts[0])) {
                if (up == null) {
                    throw new KeyNotFoundException("No way to navigate \"up\", no context found.");
                } else {
                    return up.deepGetValueInternal(parts[1]);
                }
            }
            throw new KeyNotFoundException("Key not found ", parts[0]);
        }
        return getValue(parts[0]);
    }

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * navigation, unless the config instance also contains the key "up".
     *
     * @param key The key to deeply look for.
     * @return If the value is contained in the config including sub-configs.
     */
    public boolean deepContainsKey(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (super.containsKey(parts[0])) {
                return getConfig(parts[0]).deepContainsKey(parts[1]);
            } else {
                return UP.equals(parts[0]) && up != null && up.deepContainsKey(parts[1]);
            }
        }
        return containsKey(parts[0]);
    }

    // --- Type things.

    public String getString(String key) {
        return getValue(key).asString();
    }

    public String deepGetString(String key) {
        return deepGetValue(key).asString();
    }

    public String getString(String key, String def) {
        if (containsKey(key)) {
            return getValue(key).asString();
        }
        return def;
    }

    public String deepGetString(String key, String def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asString();
        }
        return def;
    }

    public boolean getBoolean(String key) {
        return getValue(key).asBoolean();
    }

    public boolean deepGetBoolean(String key) {
        return deepGetValue(key).asBoolean();
    }

    public boolean getBoolean(String key, boolean def) {
        if (containsKey(key)) {
            return getValue(key).asBoolean();
        }
        return def;
    }

    public boolean deepGetBoolean(String key, boolean def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asBoolean();
        }
        return def;
    }

    public int getInteger(String key) {
        return getValue(key).asInteger();
    }

    public int deepGetInteger(String key) {
        return deepGetValue(key).asInteger();
    }

    public int getInteger(String key, int def) {
        if (containsKey(key)) {
            return getValue(key).asInteger();
        }
        return def;
    }

    public int deepGetInteger(String key, int def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asInteger();
        }
        return def;
    }

    public long getLong(String key) {
        return getValue(key).asLong();
    }

    public long deepGetLong(String key) {
        return deepGetValue(key).asLong();
    }

    public long getLong(String key, long def) {
        if (containsKey(key)) {
            return getValue(key).asLong();
        }
        return def;
    }

    public long deepGetLong(String key, long def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asLong();
        }
        return def;
    }

    public double getDouble(String key) {
        return getValue(key).asDouble();
    }

    public double deepGetDouble(String key) {
        return deepGetValue(key).asDouble();
    }

    public double getDouble(String key, double def) {
        if (containsKey(key)) {
            return getValue(key).asDouble();
        }
        return def;
    }

    public double deepGetDouble(String key, double def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asDouble();
        }
        return def;
    }

    public Sequence getSequence(String key) {
        return getValue(key).asSequence();
    }

    public Config getConfig(String key) {
        return getValue(key).asConfig();
    }

    public Config mutableConfig(String key) {
        if (!containsKey(key)) {
            putConfig(key, new Config(this));
        }
        return getValue(key).asConfig();
    }

    public Config deepMutableConfig(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (!containsKey(parts[0])) {
                if (UP.equals(parts[0]) && up != null) {
                    return up.deepMutableConfig(parts[1]);
                } else {
                    putConfig(key, new Config(this));
                }
            }
            return getValue(parts[0]).asConfig().deepMutableConfig(parts[1]);
        }
        return mutableConfig(key);
    }

    public Config putBoolean(String key, boolean value) throws ConfigException {
        put(key, Value.create(value));
        return this;
    }

    public Config putInteger(String key, int value) {
        put(key, Value.create(value));
        return this;
    }

    public Config putLong(String key, long value) {
        put(key, Value.create(value));
        return this;
    }

    public Config putDouble(String key, double value) {
        put(key, Value.create(value));
        return this;
    }

    public Config putString(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
        return this;
    }

    public Config putSequence(String key, Sequence value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
        return this;
    }

    public Config putConfig(String key, Config value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
        return this;
    }

    public Config putValue(String key, Value value) {
        put(key, value);
        return this;
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
                   .append(get(key).value.toString());
        }

        builder.append(')');
        return builder.toString();
    }

    // --- private
    private final Config up;
}
