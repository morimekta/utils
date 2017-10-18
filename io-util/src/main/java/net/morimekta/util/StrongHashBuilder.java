package net.morimekta.util;

import java.util.Collection;
import java.util.Map;

/**
 * Helper for making strong hash values. The hash output is 64bit int
 * calculated from a base of large prime numbers. Note that this may be
 * significantly more process intensive than ordinary hash.
 */
public class StrongHashBuilder {
    private static final long NULL  = 4283;
    private static final long FALSE = 42683;
    private static final long TRUE  = 427283;

    private final long mul;

    private long cur;
    private long i;

    /**
     * Create a strong hash builder.
     */
    public StrongHashBuilder() {
        this(99173L, 6165678739293946997L);
    }

    /**
     * Create a strong hash builder.
     *
     * @param initialValue The initial value. Recommended to be at least a
     *                     5-digit prime. This is the hash value if no data
     *                     is inserted.
     * @param valueMultiplier The value multiplier. This should be a prime
     *                        number no less than 2^60.
     */
    public StrongHashBuilder(long initialValue, long valueMultiplier) {
        cur = initialValue;
        mul = valueMultiplier;
        i = 0;
    }

    public StrongHashBuilder add(boolean b) {
        cur = (cur ^ (b ? TRUE : FALSE) ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(byte b) {
        cur = (cur ^ b ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(short b) {
        cur = (cur ^ b ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(int b) {
        cur = (cur ^ b ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(long b) {
        cur = (cur ^ b ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(char b) {
        cur = (cur ^ b ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(double b) {
        cur = (cur ^ Double.doubleToLongBits(b) ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(float b) {
        cur = (cur ^ Float.floatToIntBits(b) ^ ++i) * mul;
        return this;
    }

    public StrongHashBuilder add(boolean[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (boolean b : arr) {
                cur = (cur ^ (b ? TRUE : FALSE) ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(byte[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (byte b : arr) {
                cur = (cur ^ b ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(short[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (short b : arr) {
                cur = (cur ^ b ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(int[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (int b : arr) {
                cur = (cur ^ b ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(long[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (long b : arr) {
                cur = (cur ^ b ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(char[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (char b : arr) {
                cur = (cur ^ b ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(float[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (float b : arr) {
                cur = (cur ^ Float.floatToIntBits(b) ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(double[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (double b : arr) {
                cur = (cur ^ Double.doubleToLongBits(b) ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(Object[] arr) {
        if (arr == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ arr.length ^ ++i) * mul;
            for (Object b : arr) {
                add(b);
            }
        }
        return this;
    }

    public StrongHashBuilder add(CharSequence c) {
        if (c == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            final int l = c.length();
            cur = (cur ^ l ^ ++i) * mul;
            for (int j = 0; j < l; ++j) {
                cur = (cur ^ c.charAt(j) ^ ++i) * mul;
            }
        }
        return this;
    }

    public StrongHashBuilder add(StrongHashable o) {
        if (o == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ o.strongHash() ^ ++i) * mul;
        }
        return this;
    }

    public StrongHashBuilder add(Collection c) {
        if (c == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ c.size() ^ ++i) * mul;
            for (Object b : c) {
                add(b);
            }
        }
        return this;
    }

    public StrongHashBuilder add(Map map) {
        if (map == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else {
            cur = (cur ^ map.size() ^ ++i) * mul;
            for (Map.Entry b : ((Map<?,?>) map).entrySet()) {
                add(b.getKey());
                add(b.getValue());
            }
        }
        return this;
    }

    public StrongHashBuilder add(Object o) {
        if (o == null) {
            cur = (cur ^ NULL ^ ++i) * mul;
        } else if (o instanceof StrongHashable) {
            add((StrongHashable) o);
        } else if (o instanceof CharSequence) {
            add((CharSequence) o);
        } else if (o instanceof Boolean) {
            add(((Boolean) o).booleanValue());
        } else if (o instanceof Byte) {
            add(((Byte) o).byteValue());
        } else if (o instanceof Short) {
            add(((Short) o).shortValue());
        } else if (o instanceof Integer) {
            add(((Integer) o).intValue());
        } else if (o instanceof Long) {
            add(((Long) o).longValue());
        } else if (o instanceof Character) {
            add(((Character) o).charValue());
        } else if (o instanceof Float) {
            add(((Float) o).floatValue());
        } else if (o instanceof Double) {
            add(((Double) o).doubleValue());
        } else if (o instanceof boolean[]) {
            add((boolean[]) o);
        } else if (o instanceof byte[]) {
            add((byte[]) o);
        } else if (o instanceof short[]) {
            add((short[]) o);
        } else if (o instanceof int[]) {
            add((int[]) o);
        } else if (o instanceof long[]) {
            add((long[]) o);
        } else if (o instanceof char[]) {
            add((char[]) o);
        } else if (o instanceof float[]) {
            add((float[]) o);
        } else if (o instanceof double[]) {
            add((double[]) o);
        } else if (o instanceof Object[]) {
            add((Object[]) o);
        } else if (o instanceof Collection) {
            add((Collection) o);
        } else if (o instanceof Map) {
            add((Map) o);
        } else {
            cur = (cur ^ o.hashCode() ^ ++i) * mul;
        }
        return this;
    }

    public long strongHash() {
        return cur;
    }
}
