package net.morimekta.config;

import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base configuration container. Essentially a getType-safe map that group
 * values into a few basic types:
 *
 * <ul>
 *     <li>NUMBER: integer, longs and doubles</li>
 *     <li>STRING: literal string values</li>
 *     <li>BOOLEAN: boolean true or false</li>
 *     <li>SEQUENCE: a sequence ov values from this list</li>
 *     <li>CONFIG: a contained config (map-in-map)</li>
 * </ul>
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and getType unsafe getters.
 */
public abstract class Config {
    public interface Entry extends Comparable<Entry> {
        /**
         * Get the entry key.
         *
         * @return The key string.
         */
        String getKey();

        /**
         * Get the getType of value in the entry.
         *
         * @return The value getType.
         */
        default Value.Type getType() {
            return getValue().getType();
        }

        /**
         * Get the entry value..
         * @return The value.
         */
        Value getValue();

        /**
         * Get the entry value as string.
         * @return The value string.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default String asString() {
            return getValue().asString();
        }

        /**
         * Get the entry value as boolean.
         * @return The boolean value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default boolean asBoolean() {
            return getValue().asBoolean();
        }

        /**
         * Get the entry value as integer.
         * @return The integer value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default int asInteger() {
            return getValue().asInteger();
        }

        /**
         * Get the entry value as long.
         * @return The long value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default long asLong() {
            return getValue().asLong();
        }

        /**
         * Get the entry value as double.
         * @return The double value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default double asDouble() {
            return getValue().asDouble();
        }

        /**
         * Get the entry value as config.
         * @return The config value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default Config asConfig() {
            return getValue().asConfig();
        }

        /**
         * Get the entry value as sequence.
         * @return The sequence value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default Sequence asSequence() {
            return getValue().asSequence();
        }

        @Override
        default int compareTo(Entry other) {
            return getKey().compareTo(other.getKey());
        }
    }

    public final static class ImmutableEntry implements Entry {
        private final String key;
        private final Value value;

        protected ImmutableEntry(String key, Value value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Value getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return 44293 * getKey().hashCode() ^
                   36109 * getValue().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || !(o instanceof Entry)) return false;

            Entry other = (Entry) o;
            return other.getKey().equals(getKey()) &&
                   other.getValue().equals(getValue());
        }

        @Override
        public String toString() {
            return "ImmutableEntry(" +
                   getKey() +
                   ':' +
                   getValue().toString() +
                   ')';
        }
    }

    /**
     * @return True if the config set is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @return The number of entries in the config.
     */
    public abstract int size();

    public abstract Set<String> keySet();

    public abstract Set<Entry> entrySet();

    public Stream<String> keyStream() {
        return StreamSupport.stream(keySpliterator(), false);
    }

    protected int spliteratorCapabilities() {
        return Spliterator.DISTINCT |
               Spliterator.NONNULL |
               Spliterator.SIZED |
               Spliterator.SUBSIZED;
    }

    public Spliterator<String> keySpliterator() {
        return Spliterators.spliterator(keySet(), spliteratorCapabilities());
    }

    public Stream<Entry> entryStream() {
        return StreamSupport.stream(entrySpliterator(), false);
    }

    public Spliterator<Entry> entrySpliterator() {
        return Spliterators.spliterator(entrySet(), spliteratorCapabilities());
    }

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * and 'super' navigation, unless the config instance also contains the key
     * "up" or "super".
     *
     * @param key The prefix to look for.
     * @return The
     */
    public abstract boolean containsKey(String key);

    /**
     * Get the value getType of the value for the key.
     *
     * @param key The key to look up.
     * @return The value getType or null if not found.
     */
    public Value.Type typeOf(String key) {
        return getValue(key).getType();
    }

    /**
     * @param key The simple key to look for.
     * @return The string value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public String getString(String key) {
        return getValue(key).asString();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public String getString(String key, String def) {
        if (containsKey(key)) {
            return getValue(key).asString();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public boolean getBoolean(String key) {
        return getValue(key).asBoolean();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public boolean getBoolean(String key, boolean def) {
        if (containsKey(key)) {
            return getValue(key).asBoolean();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public int getInteger(String key) {
        return getValue(key).asInteger();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public int getInteger(String key, int def) {
        if (containsKey(key)) {
            return getValue(key).asInteger();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The long value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public long getLong(String key) {
        return getValue(key).asLong();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The long value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public long getLong(String key, long def) {
        if (containsKey(key)) {
            return getValue(key).asLong();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The double value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public double getDouble(String key) {
        return getValue(key).asDouble();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The double value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public double getDouble(String key, double def) {
        if (containsKey(key)) {
            return getValue(key).asDouble();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The sequence value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public Sequence getSequence(String key) {
        return getValue(key).asSequence();
    }

    /**
     * @param key The simple key to look for.
     * @return The config value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public Config getConfig(String key) {
        return getValue(key).asConfig();
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance. E.g.
     *
     * @param key The key to look up.
     * @return The value.
     * @throws KeyNotFoundException If not found.
     */
    public abstract Value getValue(String key);

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Config)) return false;

        Config other = (Config) o;
        if (size() != other.size() || !keySet().equals(other.keySet())) {
            return false;
        }
        for (Entry entry : other.entrySet()) {
            if (!entry.getValue().equals(getValue(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append('(');

        boolean first = true;
        for (Entry entry : entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(entry.getKey())
                   .append(":")
                   .append(entry.getValue().getValue().toString());
        }

        builder.append(')');
        return builder.toString();
    }

}
