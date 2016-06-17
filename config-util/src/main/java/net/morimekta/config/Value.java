package net.morimekta.config;

import net.morimekta.util.Strings;

import java.util.Objects;

/**
 * Config value holder class. This is primarily an internal class, but may be
 * exposed so various iterators can iterate with both getType and value from the
 * config entry.
 */
public abstract class Value {
    /**
     * Type of value stored in the config entry.
     */
    public enum Type {
        STRING,
        NUMBER,
        BOOLEAN,

        CONFIG,
        SEQUENCE,
    }

    public abstract Type getType();

    public abstract Object getValue();

    public boolean asBoolean() {
        Object value = getValue();
        switch (getType()) {
            case BOOLEAN:
                return (Boolean) value;
            case NUMBER:
                if (value instanceof Double) {
                    throw new IncompatibleValueException("Unable to convert double value to boolean");
                }
                long l = ((Number) value).longValue();
                if (l == 0L) return false;
                if (l == 1L) return true;
                throw new IncompatibleValueException("Unable to convert number " + l + " to boolean");
            case STRING:
                switch (value.toString().toLowerCase()) {
                    case "0":
                    case "n":
                    case "f":
                    case "no":
                    case "false":
                        return false;
                    case "1":
                    case "y":
                    case "t":
                    case "yes":
                    case "true":
                        return true;
                    default:
                        throw new IncompatibleValueException(String.format(
                                "Unable to parse the string \"%s\" to boolean",
                                Strings.escape(value.toString())));
                }
            default:
                throw new IncompatibleValueException(
                        "Unable to convert getType " + getType() + " to a boolean");
        }
    }

    public int asInteger() {
        Object value = getValue();
        switch (getType()) {
            case NUMBER:
                return ((Number) value).intValue();
            case STRING:
                try {
                    return Integer.parseInt(value.toString());
                } catch (NumberFormatException nfe) {
                    throw new IncompatibleValueException(
                            "Unable to parse string \"" + Strings.escape(value.toString()) +
                            "\" to an int", nfe);
                }
            default:
                throw new IncompatibleValueException(
                        "Unable to convert getType " + getType() + " to an int");
        }
    }

    public long asLong() {
        Object value = getValue();
        switch (getType()) {
            case NUMBER:
                return ((Number) value).longValue();
            case STRING:
                try {
                    return Long.parseLong(value.toString());
                } catch (NumberFormatException nfe) {
                    throw new IncompatibleValueException(
                            "Unable to parse string \"" + Strings.escape(value.toString()) +
                            "\" to a long", nfe);
                }
            default:
                throw new IncompatibleValueException(
                        "Unable to convert getType " + getType() + " to a long");
        }
    }

    public double asDouble() {
        Object value = getValue();
        switch (getType()) {
            case NUMBER:
                return ((Number) value).doubleValue();
            case STRING:
                try {
                    return Double.parseDouble(value.toString());
                } catch (NumberFormatException nfe) {
                    throw new IncompatibleValueException(
                            "Unable to parse string \"" + Strings.escape(value.toString()) +
                            "\" to a double", nfe);
                }
            default:
                throw new IncompatibleValueException(
                        "Unable to convert getType " + getType() + " to a double");
        }
    }

    public String asString() {
        if (getType() == Type.SEQUENCE || getType() == Type.CONFIG) {
            throw new IncompatibleValueException(
                    "Unable to convert " + getType() + " to a string");
        }
        return getValue().toString();
    }

    public Sequence asSequence() {
        if (getType() != Type.SEQUENCE) {
            throw new IncompatibleValueException(
                    "Unable to convert " + getType() + " to a sequence");
        }
        return (Sequence) getValue();
    }

    public Config asConfig() {
        if (getType() != Type.CONFIG) {
            throw new IncompatibleValueException(
                    "Unable to convert " + getType() + " to a config");
        }
        return (Config) getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Value)) return false;

        Object value = getValue();
        Type type = getType();
        Value other = (Value) o;
        return type == other.getType() &&
               type == Type.NUMBER ?
               ((Number) value).doubleValue() == ((Number) other.getValue()).doubleValue() :
               Objects.equals(value, other.getValue());
    }

    @Override
    public String toString() {
        return String.format("Value(%s,%s)",
                             getType().toString().toLowerCase(),
                             Objects.toString(getValue()));
    }

    // package local value helpers.

    static Object fromObject(Type type, Object elem) {
        switch (type) {
            case STRING:
                if ((elem instanceof ImmutableSequence) || (elem instanceof Config)) {
                    throw new IllegalArgumentException("Not a string value: " + elem.getClass()
                                                                                    .getSimpleName());
                }
                // Cast everything into string.
                return elem.toString();
            case BOOLEAN:
                if (elem instanceof Boolean) {
                    return elem;
                } else {
                    switch (elem.toString().toLowerCase()) {
                        case "0":
                        case "f":
                        case "false":
                        case "n":
                        case "no":
                            return false;
                        case "1":
                        case "t":
                        case "true":
                        case "y":
                        case "yes":
                            return true;
                        default:
                            throw new IllegalArgumentException("Not a boolean value: " + elem.toString());
                    }
                }
            case NUMBER:
                if (elem instanceof Number) {
                    return elem;
                } else if (elem instanceof CharSequence) {
                    String val = elem.toString().toLowerCase();
                    if (val.contains(".") || val.contains("e")) {
                        return Double.parseDouble(val);
                    } else {
                        return Long.parseLong(val);
                    }
                } else if (elem.toString().startsWith(elem.getClass().getName() + "@")) {
                    throw new IllegalArgumentException("Not a number getType: " + elem.getClass().getName());
                } else {
                    throw new IllegalArgumentException("Not a number value: " + elem.toString());
                }
            case SEQUENCE:
                if (!(elem instanceof Sequence)) {
                    throw new IllegalArgumentException("Not a sequence getType: " + elem.getClass()
                                                                                     .getSimpleName());
                }
                return elem;
            case CONFIG:
                if (!(elem instanceof Config)) {
                    throw new IllegalArgumentException("Not a config getType: " + elem.getClass()
                                                                                   .getSimpleName());
                }
                return elem;
            default:
                // TODO: Maybe support more element types in sequences?
                throw new IllegalArgumentException("Not supported sequence value getType for " + type + ": " + elem.getClass());
        }
    }

    static Object fromValue(Type type, Value value) {
        switch (type) {
            case STRING:
                return value.asString();
            case BOOLEAN:
                return value.asBoolean();
            case NUMBER: {
                if (value.asDouble() == (double) value.asLong()) {
                    return value.asLong();
                }
                return value.asDouble();
            }
            case SEQUENCE:
                return value.asSequence();
            case CONFIG:
                return value.asConfig();
        }

        throw new ConfigException("Unhandled value getType " + type);
    }

}
