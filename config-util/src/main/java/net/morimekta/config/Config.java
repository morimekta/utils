package net.morimekta.config;

import java.util.TreeMap;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look up more than one level into the map (if referencing config objects
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
public class Config extends TreeMap<String, Value> {
    public static final String UP = "up";
    public static final String SUPER = "super";

    /**
     * Create a config instance.
     */
    public Config() {
        this.up = null;
        this.sup = null;
    }

    /**
     * Create a config instance.
     *
     * @param up The parent (up) config.
     */
    public Config(Config up) {
        this.up = up;
        this.sup = null;
    }

    /**
     * Create a config instance.
     *
     * @param up The parent (up) config.
     * @param sup The base config (or super-config).
     */
    public Config(Config up, Config sup) {
        super();
        this.up = up;
        this.sup = sup;
    }

    /**
     * Presence sensitive type checker.
     * @param key The simple key to look for.
     * @return The type of the value.
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
            if (sup != null && sup.containsKey(key)) {
                return sup.getValue(key);
            }
            throw new KeyNotFoundException("No such key " + key);
        }
        return get(key);
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance. E.g.
     *
     * <code>
     * Value v = deepGetValue("up.args");
     * </code>
     *
     * Will first navigate one step "up", then try to find the value "args".
     * Otherwise looking up a key is equivalent to a series of
     * {@link #getConfig(String)} and ${@link #getValue(String)} at the end.
     * E.g. these two calls are equivalent.
     *
     * <code>
     * Value v = deepGetValue("arg1.arg2.arg3");
     * Value v = getConfig("arg1").getConfig("arg2").getValue("arg3");
     * </code>
     *
     * @param key The key to look for.
     * @return The value found.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Value deepGetValue(String key) {
        try {
            return deepGetValueInternal(key);
        } catch (KeyNotFoundException e) {
            throw new KeyNotFoundException("No such entry " + key + " in config, " + e.getMessage());
        }
    }

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * navigation, unless the config instance also contains the key "up".
     *
     * @param key The key to deeply look for.
     * @return If the value is contained in the config including sub-configs.
     */
    public boolean containsKey(Object key) {
        return super.containsKey(key) || sup != null && sup.containsKey(key);
    }

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * and 'super' navigation, unless the config instance also contains the key
     * "up" or "super".
     *
     * @param key The key to deeply look for.
     * @return If the value is contained in the config including sub-configs.
     */
    public boolean deepContainsKey(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (super.containsKey(parts[0])) {
                return getConfig(parts[0]).deepContainsKey(parts[1]);
            } else if (UP.equals(parts[0])) {
                return up != null && up.deepContainsKey(parts[1]);
            } else {
                return SUPER.equals(parts[0]) && sup != null && sup.deepContainsKey(key);
            }
        }
        return containsKey(parts[0]);
    }

    // --- Type things.

    /**
     * @param key The simple key to look for.
     * @return The string value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String getString(String key) {
        return getValue(key).asString();
    }

    /**
     * @param key The recursive key to look for.
     * @return The string value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String deepGetString(String key) {
        return deepGetValue(key).asString();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String getString(String key, String def) {
        if (containsKey(key)) {
            return getValue(key).asString();
        }
        return def;
    }

    /**
     * @param key The recursive key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String deepGetString(String key, String def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asString();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean getBoolean(String key) {
        return getValue(key).asBoolean();
    }

    /**
     * @param key The recursive key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean deepGetBoolean(String key) {
        return deepGetValue(key).asBoolean();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean getBoolean(String key, boolean def) {
        if (containsKey(key)) {
            return getValue(key).asBoolean();
        }
        return def;
    }

    /**
     * @param key The recursive key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean deepGetBoolean(String key, boolean def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asBoolean();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int getInteger(String key) {
        return getValue(key).asInteger();
    }

    /**
     * @param key The recursive key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int deepGetInteger(String key) {
        return deepGetValue(key).asInteger();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int getInteger(String key, int def) {
        if (containsKey(key)) {
            return getValue(key).asInteger();
        }
        return def;
    }

    /**
     * @param key The recursive key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int deepGetInteger(String key, int def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asInteger();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The long value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long getLong(String key) {
        return getValue(key).asLong();
    }

    /**
     * @param key The recursive key to look for.
     * @return The long value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long deepGetLong(String key) {
        return deepGetValue(key).asLong();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The long value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long getLong(String key, long def) {
        if (containsKey(key)) {
            return getValue(key).asLong();
        }
        return def;
    }

    /**
     * @param key The recursive key to look for.
     * @param def The default value if not found.
     * @return The long value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long deepGetLong(String key, long def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asLong();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The double value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double getDouble(String key) {
        return getValue(key).asDouble();
    }

    /**
     * @param key The recursive key to look for.
     * @return The double value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double deepGetDouble(String key) {
        return deepGetValue(key).asDouble();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The double value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double getDouble(String key, double def) {
        if (containsKey(key)) {
            return getValue(key).asDouble();
        }
        return def;
    }

    /**
     * @param key The recursive key to look for.
     * @param def The default value if not found.
     * @return The double value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double deepGetDouble(String key, double def) {
        if (deepContainsKey(key)) {
            return deepGetValue(key).asDouble();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The sequence value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Sequence getSequence(String key) {
        return getValue(key).asSequence();
    }

    /**
     * @param key The recursive key to look for.
     * @return The sequence value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Sequence deepGetSequence(String key) {
        return deepGetValue(key).asSequence();
    }

    /**
     * @param key The simple key to look for.
     * @return The config value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Config getConfig(String key) {
        return getValue(key).asConfig();
    }


    /**
     * @param key The recursive key to look for.
     * @return The config value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Config deepGetConfig(String key) {
        return deepGetValue(key).asConfig();
    }

    /**
     * Get a config value at the simple key point. If the config exists, it
     * will be created.
     *
     * @param key The recursive key to look for.
     * @return The config value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Config mutableConfig(String key) {
        if (!containsKey(key)) {
            Config cfg;
            if (sup != null && sup.containsKey(key)) {
                cfg = new Config(this, sup.getConfig(key));
            } else {
                cfg = new Config(this);
            }
            putConfig(key, cfg);
        }
        return getValue(key).asConfig();
    }

    /**
     * Get a config value with deep lookup. If any of the keys are missing,
     * the config will be created.
     *
     * @param key The recursive key to look up.
     * @return The config value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Config deepMutableConfig(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (!containsKey(parts[0])) {
                if (UP.equals(parts[0]) && up != null) {
                    return up.deepMutableConfig(parts[1]);
                } else {
                    Config cfg;
                    if (sup != null && sup.containsKey(parts[0])) {
                        cfg = new Config(this, sup.getConfig(parts[0]));
                    } else {
                        cfg = new Config(this);
                    }
                    putConfig(parts[0], cfg);
                }
            }
            return getValue(parts[0]).asConfig()
                                     .deepMutableConfig(parts[1]);
        }
        return mutableConfig(key);
    }

    /**
     * Put a boolean value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putBoolean(String key, boolean value) {
        put(key, Value.create(value));
        return this;
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
    public Config deepPutBoolean(String key, boolean value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutBoolean(parts[1], value);
        } else {
            put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putInteger(String key, int value) {
        put(key, Value.create(value));
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
    public Config deepPutInteger(String key, int value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutInteger(parts[1], value);
        } else {
            put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putLong(String key, long value) {
        put(key, Value.create(value));
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
    public Config deepPutLong(String key, long value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutLong(parts[1], value);
        } else {
            put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putDouble(String key, double value) {
        put(key, Value.create(value));
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
    public Config deepPutDouble(String key, double value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutDouble(parts[1], value);
        } else {
            put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putString(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
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
    public Config deepPutString(String key, String value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutString(parts[1], value);
        } else {
            put(key, Value.create(value));
        }
        return this;
    }

    /**
     * Put a sequence value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public Config putSequence(String key, Sequence value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
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
    public Config deepPutSequence(String key, Sequence value) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            mutableConfig(parts[0]).deepPutSequence(parts[1], value);
        } else {
            put(key, Value.create(value));
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
    public Config putConfig(String key, Config value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        put(key, Value.create(value));
        return this;
    }

    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
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
    // The 'super' config. Can be referenced in 'deep' lookups by 'super'.
    private final Config sup;

    private Value deepGetValueInternal(String key) {
        String[] parts = key.split("[.]", 2);
        if (parts.length == 2) {
            if (super.containsKey(parts[0])) {
                return getConfig(parts[0]).deepGetValueInternal(parts[1]);
            } else if (UP.equals(parts[0])) {
                if (up == null) {
                    throw new KeyNotFoundException("No way to navigate \"up\".");
                } else {
                    return up.deepGetValueInternal(parts[1]);
                }
            } else if (SUPER.equals(parts[0])) {
                if (sup == null) {
                    throw new KeyNotFoundException("No way to navigate to \"super\".");
                } else {
                    return sup.deepGetValueInternal(parts[1]);
                }
            }
            throw new KeyNotFoundException("Key not found ", parts[0]);
        }
        return getValue(parts[0]);
    }
}
