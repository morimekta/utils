package net.morimekta.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look up more than one level into the map (if referencing config objects
 * within the config object). This way, if the config contains a config object
 * on key 'b', then getString('b.c') will look for key 'c' in the config 'b'.
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and type unsafe getters.
 */
public class Config {
    public static class Entry implements Comparable<Entry> {
        public final String key;
        public final Value.Type type;
        public final Object value;

        private Entry(String key, Value.Type type, Object value) {
            this.key = key;
            this.type = type;
            this.value = value;
        }

        @Override
        public int compareTo(Entry entry) {
            return key.compareTo(entry.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    private Config() {
        map = new LinkedHashMap<>();
    }

    public Set<String> keySet() {
        return new HashSet<>(map.keySet());
    }

    public Set<String> keySet(String prefix) {
        return map.keySet()
                  .stream()
                  .filter(e -> e.equals(prefix) || e.startsWith(prefix + '.'))
                  .collect(Collectors.toSet());
    }

    public Stream<String> keyStream() {
        return StreamSupport.stream(keySpliterator(), false);
    }

    public Spliterator<String> keySpliterator() {
        return Spliterators.spliterator(map.keySet(),
                                        Spliterator.IMMUTABLE |
                                        Spliterator.DISTINCT |
                                        Spliterator.NONNULL |
                                        Spliterator.SIZED |
                                        Spliterator.SUBSIZED);
    }

    public Set<Entry> entrySet() {
        return map.entrySet()
                  .stream()
                  .map(e -> new Entry(e.getKey(), e.getValue().type, e.getValue().value))
                  .collect(Collectors.toSet());
    }

    public Stream<Entry> entryStream() {
        return StreamSupport.stream(entrySpliterator(), false);
    }

    public Spliterator<Entry> entrySpliterator() {
        return Spliterators.spliterator(entrySet(),
                                        Spliterator.IMMUTABLE |
                                        Spliterator.DISTINCT |
                                        Spliterator.NONNULL |
                                        Spliterator.SIZED |
                                        Spliterator.SUBSIZED);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean containsPrefix(String prefix) throws ConfigException {
        String prefixed = prefix + '.';
        for (String key : keySet()) {
            if (key.equals(prefix) || key.startsWith(prefixed)) {
                return true;
            }
        }
        return false;
    }

    public Value.Type typeOf(String key) throws ConfigException {
        return getValue(key).type;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    // --- Type things.

    public String getString(String key) throws ConfigException {
        return getValue(key).asString();
    }

    public String getString(String key, String def) throws ConfigException {
        if (containsKey(key)) {
            return getValue(key).asString();
        }
        return def;
    }

    public boolean getBoolean(String key) throws ConfigException {
        return getValue(key).asBoolean();
    }

    public boolean getBoolean(String key, boolean def) throws ConfigException {
        if (containsKey(key)) {
            return getValue(key).asBoolean();
        }
        return def;
    }

    public int getInteger(String key) throws ConfigException {
        return getValue(key).asInteger();
    }

    public int getInteger(String key, int def) throws ConfigException {
        if (containsKey(key)) {
            return getValue(key).asInteger();
        }
        return def;
    }

    public long getLong(String key) throws ConfigException {
        return getValue(key).asLong();
    }

    public long getLong(String key, long def) throws ConfigException {
        if (containsKey(key)) {
            return getValue(key).asLong();
        }
        return def;
    }

    public double getDouble(String key) throws ConfigException {
        return getValue(key).asDouble();
    }

    public double getDouble(String key, double def) throws ConfigException {
        if (containsKey(key)) {
            return getValue(key).asDouble();
        }
        return def;
    }

    public Sequence getSequence(String key) throws ConfigException {
        return getValue(key).asSequence();
    }

    public Config getConfig(String key) throws ConfigException {
        return getValue(key).asConfig();
    }

    public Value getValue(String key) throws KeyNotFoundException {
        if (!map.containsKey(key)) {
            throw new KeyNotFoundException("No such key " + key);
        }
        return map.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Config)) {
            return false;
        }
        Config other = (Config) o;
        if (other.map.size() != map.size() ||
            !other.map.keySet().equals(map.keySet())) {
            return false;
        }

        for (String key : map.keySet()) {
            if (!map.get(key).equals(other.map.get(key))) {
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
        for (String key : keySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(key).append(":").append(map.get(key).value.toString());
        }

        builder.append(')');
        return builder.toString();
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String,Value> map;

        public Builder() {
            map = new HashMap<>();
        }

        public Builder(Config base) {
            map = new HashMap<>();
            map.putAll(base.map);
        }

        public void putAll(Config other) {
            map.putAll(other.map);
        }

        public Builder putBoolean(String key, boolean value) throws ConfigException {
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putInteger(String key, int value) {
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putLong(String key, long value) {
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putDouble(String key, double value) {
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putString(String key, String value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putSequence(String key, Sequence value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putConfig(String key, Config value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
            map.put(key, Value.create(value));
            return this;
        }

        public Builder putValue(String key, Value value) {
            map.put(key, value);
            return this;
        }

        public Value get(String key) {
            return map.get(key);
        }

        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        public Builder clear(String key) {
            map.remove(key);
            return this;
        }

        public Builder clear() {
            map.clear();
            return this;
        }

        public Config build() {
            Config cfg = new Config();
            cfg.map.putAll(map);
            return cfg;
        }
    }

    // --- PRIVATE ---

    private final Map<String,Value> map;
}
