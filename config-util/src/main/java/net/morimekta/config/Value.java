package net.morimekta.config;

import net.morimekta.util.Strings;

import java.util.Objects;

/**
 * Config value holder class. This is primarily an internal class, but may be
 * exposed so various iterators can iterate with both type and value from the
 * config entry.
 */
public class Value {
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

    public final Type   type;
    public final Object value;

    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public boolean asBoolean() throws ConfigException {
        switch (type) {
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
                        "Unable to convert type " + type + " to a boolean");
        }
    }

    public int asInteger() throws ConfigException {
        switch (type) {
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
                        "Unable to convert type " + type + " to an int");
        }
    }

    public long asLong() throws ConfigException {
        switch (type) {
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
                        "Unable to convert type " + type + " to a long");
        }
    }

    public double asDouble() throws ConfigException {
        switch (type) {
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
                        "Unable to convert type " + type + " to a double");
        }
    }

    public String asString() throws ConfigException {
        if (type == Type.SEQUENCE || type == Type.CONFIG) {
            throw new IncompatibleValueException(
                    "Unable to convert " + type + " to a string");
        }
        return value.toString();
    }

    public Sequence asSequence() throws ConfigException {
        if (type != Type.SEQUENCE) {
            throw new IncompatibleValueException(
                    "Unable to convert " + type + " to a sequence");
        }
        return (Sequence) value;
    }

    public Config asConfig() throws ConfigException {
        if (type != Type.CONFIG) {
            throw new IncompatibleValueException(
                    "Unable to convert " + type + " to a config");
        }
        return (Config) value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Value)) return false;

        Value other = (Value) o;
        return type == other.type &&
               type == Type.NUMBER ?
               ((Number) value).doubleValue() == ((Number) other.value).doubleValue() :
               Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return String.format("Value(%s,%s)",
                             type.toString().toLowerCase(),
                             Objects.toString(value));
    }

    public static Value create(boolean value) {
        return new Value(Type.BOOLEAN, value);
    }

    public static Value create(int value) {
        return new Value(Type.NUMBER, value);
    }

    public static Value create(long value) {
        return new Value(Type.NUMBER, value);
    }

    public static Value create(double value) {
        return new Value(Type.NUMBER, value);
    }

    public static Value create(String value) {
        return new Value(Type.STRING, value);
    }

    public static Value create(Config value) {
        return new Value(Type.CONFIG, value);
    }

    public static Value create(Sequence value) {
        return new Value(Type.SEQUENCE, value);
    }
}