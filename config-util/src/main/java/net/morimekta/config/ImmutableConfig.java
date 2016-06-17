package net.morimekta.config;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

/**
 * Immutable config container. It enforces deep immutability of all values,
 * and does not keep track of "parent", "base" or similar relations. It is
 * simply a config map.
 */
public class ImmutableConfig extends Config {
    /**
     * Create an empty config instance. Handy for testing.
     */
    public ImmutableConfig() {
        map = ImmutableMap.of();
    }

    /**
     * Create an immutable copy of a config.
     * @param base the config to be based on.
     */
    private ImmutableConfig(Config base) {
        if (base != null) {
            ImmutableMap.Builder<String, Value> builder = ImmutableMap.builder();
            base.entrySet()
                .forEach(e -> {
                    switch (e.getType()) {
                        case CONFIG:
                            builder.put(e.getKey(), Value.create(copyOf(e.asConfig())));
                            break;
                        case SEQUENCE:
                            builder.put(e.getKey(), Value.create(ImmutableSequence.copyOf(e.asSequence())));
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

    private ImmutableConfig(Map<String, Value> values) {
        ImmutableMap.Builder<String, Value> builder = ImmutableMap.builder();
        values.forEach((k, v) -> {
            switch (v.type) {
                case CONFIG:
                    builder.put(k, Value.create(copyOf(v.asConfig())));
                    break;
                case SEQUENCE:
                    builder.put(k, Value.create(ImmutableSequence.copyOf(v.asSequence())));
                    break;
                default:
                    builder.put(k, v);
                    break;
            }
        });
        map = builder.build();
    }

    public static Config copyOf(Config config) {
        if (config instanceof ImmutableConfig) {
            return config;
        } else {
            return new ImmutableConfig(config);
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

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
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
        if (o == null || !(o instanceof Config)) {
            return false;
        }
        Config other = (Config) o;

        if (other.size() != map.size() || !other.keySet().equals(keySet())) {
            return false;
        }

        for (Entry entry : other.entrySet()) {
            if (!getValue(entry.getKey()).equals(entry.getValue())) {
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

        public ImmutableConfig build() {
            return new ImmutableConfig(map);
        }
    }

    // --- PRIVATE ---

    private final Map<String, Value> map;
}
