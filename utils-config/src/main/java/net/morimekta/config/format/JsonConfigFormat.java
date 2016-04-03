package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.IncompatibleValueException;
import net.morimekta.config.Sequence;
import net.morimekta.config.Value;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonToken;
import net.morimekta.util.json.JsonTokenizer;
import net.morimekta.util.json.JsonWriter;
import net.morimekta.util.json.PrettyJsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeSet;

/**
 * Config Formatter for JSON object syntax.
 */
public class JsonConfigFormat implements ConfigFormat {
    private final boolean pretty;

    public JsonConfigFormat() {
        this(false);
    }

    public JsonConfigFormat(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public void format(OutputStream out, Config config) {
        JsonWriter writer = pretty ? new PrettyJsonWriter(out) : new JsonWriter(out);
        try {
            formatTo(writer, config);
            writer.flush();
        } catch (ConfigException|JsonException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Config parse(InputStream in) throws ConfigException {
        try {
            JsonTokenizer tokenizer = new JsonTokenizer(in);
            JsonToken token = tokenizer.next();
            if (!token.isSymbol(JsonToken.kMapStart)) {
                throw new ConfigException("");
            }
            return parseConfig(tokenizer, token);
        } catch (JsonException|IOException e) {
            throw new ConfigException("", e);
        }
    }

    // --- INTERNAL ---

    protected Config parseConfig(JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
        Config.Builder builder = Config.builder();
        char sep = token.charAt(0);
        while (sep != JsonToken.kMapEnd) {
            JsonToken jkey = tokenizer.expect("Map key.");
            // No need to decode the key.
            String key = jkey.substring(1, -1).asString();
            tokenizer.expectSymbol("", JsonToken.kKeyValSep);

            token = tokenizer.expect("Map value.");
            switch (token.type) {
                case SYMBOL:
                    switch (token.charAt(0)) {
                        case JsonToken.kMapStart:
                            builder.putConfig(key, parseConfig(tokenizer, token));
                            break;
                        case JsonToken.kListStart:
                            builder.putSequence(key, parseSequence(tokenizer, token));
                            break;
                    }
                    break;
                case LITERAL:
                    builder.putString(key, token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (token.isInteger()) {
                        builder.putLong(key, token.longValue());
                    } else {
                        builder.putDouble(key, token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    builder.putBoolean(key, token.booleanValue());
                    break;
            }

            sep = tokenizer.expectSymbol("", JsonToken.kMapEnd, JsonToken.kListSep);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    protected Sequence parseSequence(JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
        Sequence.Builder builder = null;
        char sep = token.charAt(0);
        while (sep != JsonToken.kListEnd) {
            token = tokenizer.expect("Array value.");
            switch (token.type) {
                case SYMBOL:
                    switch (token.charAt(0)) {
                        case JsonToken.kMapStart:
                            if (builder == null) {
                                builder = Sequence.builder(Value.Type.CONFIG);
                            }
                            builder.add(parseConfig(tokenizer, token));
                            break;
                        case JsonToken.kListStart:
                            if (builder == null) {
                                builder = Sequence.builder(Value.Type.SEQUENCE);
                            }
                            builder.add(parseSequence(tokenizer, token));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case LITERAL:
                    if (builder == null) {
                        builder = Sequence.builder(Value.Type.STRING);
                    }
                    builder.add(token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (builder == null) {
                        builder = Sequence.builder(Value.Type.NUMBER);
                    }
                    if (token.isInteger()) {
                        builder.add(token.longValue());
                    } else {
                        builder.add(token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    if (builder == null) {
                        builder = Sequence.builder(Value.Type.BOOLEAN);
                    }
                    builder.add(token.booleanValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled JSON token: " + token);
            }

            sep = tokenizer.expectSymbol("", JsonToken.kListEnd, JsonToken.kListSep);
        }

        if (builder == null) {
            builder = Sequence.builder(Value.Type.STRING);
        }

        return builder.build();
    }

    protected void formatTo(JsonWriter writer, Config config)
            throws JsonException, ConfigException {
        writer.object();

        for (Config.Entry entry : new TreeSet<>(config.entrySet())) {
            writer.key(entry.key);
            switch (entry.type) {
                case STRING:
                    writer.value((String) entry.value);
                    break;
                case NUMBER:
                    if (entry.value instanceof Double) {
                        writer.value((Double) entry.value);
                    } else {
                        writer.value(((Number) entry.value).longValue());
                    }
                    break;
                case BOOLEAN:
                    writer.value((Boolean) entry.value);
                    break;
                case CONFIG:
                    formatTo(writer, (Config) entry.value);
                    break;
                case SEQUENCE:
                    formatTo(writer, (Sequence) entry.value);
                    break;
                default:
                    throw new ConfigException("Unhandled type in formatter: " + entry.type);
            }
        }
        writer.endObject();
    }

    protected void formatTo(JsonWriter writer, Sequence sequence)
            throws JsonException, ConfigException {
        writer.array();
        for (Value item : sequence.values()) {
            switch (item.type) {
                case STRING:
                    writer.value((String) item.value);
                    break;
                case NUMBER:
                    if (item.value instanceof Double) {
                        writer.value((Double) item.value);
                    } else {
                        writer.value(((Number) item.value).longValue());
                    }
                    break;
                case BOOLEAN:
                    writer.value((Boolean) item.value);
                    break;
                case SEQUENCE:
                    formatTo(writer, (Sequence) item.value);
                    break;
                case CONFIG:
                    formatTo(writer, (Config) item.value);
                    break;
            }
        }
        writer.endArray();
    }
}
