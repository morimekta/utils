package net.morimekta.config;

/**
 * Mutable configuration object. It does not enforce type mutability, and will
 * keep a reference to the base config if provided.
 *
 * NOTE: Changed in values on the base config does *not* propagate to the
 * mutable config after it's creation. The map is mutable as it is there to be
 * used for manipulating the config during parsing or generation.
 */
public interface ConfigBuilder<B extends ConfigBuilder> {
    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    B putValue(String key, Value value);

    /**
     * Remove entry with the given key.
     *
     * @param key The key to remove.
     * @return The config.
     */
    B remove(String key);

    /**
     * Clear the config.
     *
     * @return The config.
     */
    B clear();

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putBoolean(String key, boolean value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putInteger(String key, int value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putLong(String key, long value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putDouble(String key, double value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putString(String key, String value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a sequence value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putSequence(String key, Sequence value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a config value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putConfig(String key, Config value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }
}
