package net.morimekta.config;

/**
 * Immutable value that can only contain the true config types.
 */
public class ImmutableValue extends Value {
    private final Type   type;
    private final Object value;

    public ImmutableValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Value.Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public static ImmutableValue copyOf(Value value) {
        if (value instanceof ImmutableValue) {
            return (ImmutableValue) value;
        }
        return new ImmutableValue(value.getType(), value.getValue());
    }

    public static ImmutableValue create(boolean value) {
        return new ImmutableValue(Type.BOOLEAN, value);
    }

    public static ImmutableValue create(int value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(long value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(double value) {
        return new ImmutableValue(Type.NUMBER, value);
    }

    public static ImmutableValue create(String value) {
        return new ImmutableValue(Type.STRING, value);
    }

    public static ImmutableValue create(Config value) {
        return new ImmutableValue(Type.CONFIG, value);
    }

    public static ImmutableValue create(Sequence value) {
        return new ImmutableValue(Type.SEQUENCE, value);
    }
}
