package net.morimekta.util.json;

/**
 * Unchecked (runtime) JsonException wrapper that can be used in streams etc.
 */
public class UncheckedJsonException extends RuntimeException {
    public UncheckedJsonException(JsonException e) {
        super(e);
    }

    public JsonException getCause() {
        return (JsonException) super.getCause();
    }

    public String getLine() {
        return getCause().getLine();
    }

    public int getLineNo() {
        return getCause().getLineNo();
    }

    public int getLinePos() {
        return getCause().getLinePos();
    }

    public int getLen() {
        return getCause().getLen();
    }

    public String describe() {
        return getCause().describe();
    }

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }

    @Override
    public String toString() {
        return String.format("UncheckedJsonException(%s)", describe());
    }
}
