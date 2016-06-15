package net.morimekta.config;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look up more than one level into the map (if referencing config objects
 * within the config object). This way, if the config contains a config object
 * on key 'b', then getString('b.c') will look for key 'c' in the config 'b'.
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and type unsafe getters.
 */
public class ImmutableConfig implements Config {
    public ImmutableConfig() {
        this(null, null);
    }

    public ImmutableConfig(Config parent) {

    }

    public ImmutableConfig(Config parent, Config base) {
        this.parent = parent;
        if (base != null) {
            ImmutableMap.Builder<String, Value> builder = ImmutableMap.builder();
            base.entrySet()
                .forEach(e -> {
                    switch (e.getType()) {
                        case CONFIG:
                            if (e.getValue().value instanceof ImmutableConfig) {
                                builder.put(e.getKey(), e.getValue());
                            } else {
                                builder.put(e.getKey(), Value.create(new ImmutableConfig(this, e.asConfig())));
                            }
                            break;
                        case SEQUENCE:
                            if (e.getValue().value instanceof ImmutableSequence) {
                                builder.put(e.getKey(), e.getValue());
                            } else {
                                Sequence bs = e.asSequence();
                                builder.put(e.getKey(), Value.create(ImmutableSequence.builder(this, bs.type(), bs)));
                            }
                            break;
                        default:
                            builder.put(e.getKey(), e.getValue());
                            break;
                    }
                });
            map = builder.build();
        } else {
            map = ImmutableMap.of();
        }
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(map.keySet());
    }

    @Override
    public Set<Entry> entrySet() {
        return map.entrySet()
                  .stream()
                  .map(e -> new ImmutableEntry(e.getKey(), e.getValue()))
                  .collect(Collectors.toSet());
    }

    protected int spliteratorCapabilities() {
        return Spliterator.IMMUTABLE |
               Spliterator.DISTINCT |
               Spliterator.NONNULL |
               Spliterator.SIZED |
               Spliterator.SUBSIZED;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * @return The number of entries in the config.
     */
    public int size() {
        return map.size();
    }

    /**
     * Get the config value spec for the key.
     *
     * @param key The key to look up.
     * @return The value.
     * @throws KeyNotFoundException If not found.
     */
    public Value getValue(String key) throws KeyNotFoundException{
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
        if (o == null || !(o instanceof ImmutableConfig)) {
            return false;
        }
        ImmutableConfig other = (ImmutableConfig) o;
        if (other.map.size() != map.size() || !other.map.keySet()
                                                        .equals(map.keySet())) {
            return false;
        }

        for (String key : map.keySet()) {
            if (!map.get(key)
                    .equals(other.map.get(key))) {
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
            builder.append(key)
                   .append(":")
                   .append(map.get(key).value.toString());
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
        private final Map<String, Value> map;

        public Builder() {
            map = new HashMap<>();
        }

        public Builder(ImmutableConfig base) {
            map = new HashMap<>();
            map.putAll(base.map);
        }

        public void putAll(ImmutableConfig other) {
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

        public Builder putConfig(String key, ImmutableConfig value) {
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

        public ImmutableConfig build() {
            ImmutableConfig cfg = new ImmutableConfig();
            cfg.map.putAll(map);
            return cfg;
        }
    }

    // --- PRIVATE ---

    private final Config parent;
    private final Map<String, Value> map;
}
