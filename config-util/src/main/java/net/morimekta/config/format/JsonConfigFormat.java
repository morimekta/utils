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
import java.util.Map;

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
            return parseConfig(new Config(), tokenizer, token);
        } catch (JsonException|IOException e) {
            throw new ConfigException("", e);
        }
    }

    // --- INTERNAL ---

    protected Config parseConfig(Config config, JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
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
                            parseConfig(config.mutableConfig(key), tokenizer, token);
                            break;
                        case JsonToken.kListStart:
                            config.putSequence(key, parseSequence(config, tokenizer, token));
                            break;
                    }
                    break;
                case LITERAL:
                    config.putString(key, token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (token.isInteger()) {
                        config.putLong(key, token.longValue());
                    } else {
                        config.putDouble(key, token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    config.putBoolean(key, token.booleanValue());
                    break;
            }

            sep = tokenizer.expectSymbol("", JsonToken.kMapEnd, JsonToken.kListSep);
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    protected Sequence parseSequence(Config context, JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
        Sequence builder = null;
        char sep = token.charAt(0);
        while (sep != JsonToken.kListEnd) {
            token = tokenizer.expect("Array value.");
            switch (token.type) {
                case SYMBOL:
                    switch (token.charAt(0)) {
                        case JsonToken.kMapStart:
                            if (builder == null) {
                                builder = new Sequence(Value.Type.CONFIG);
                            }
                            builder.add(parseConfig(new Config(context), tokenizer, token));
                            break;
                        case JsonToken.kListStart:
                            if (builder == null) {
                                builder = new Sequence(Value.Type.SEQUENCE);
                            }
                            // Configs contained within sequences will have it's "up" context
                            // always set to the config that contains the "root" sequence.
                            builder.add(parseSequence(context, tokenizer, token));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case LITERAL:
                    if (builder == null) {
                        builder = new Sequence(Value.Type.STRING);
                    }
                    builder.add(token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (builder == null) {
                        builder = new Sequence(Value.Type.NUMBER);
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
                        builder = new Sequence(Value.Type.BOOLEAN);
                    }
                    builder.add(token.booleanValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled JSON token: " + token);
            }

            sep = tokenizer.expectSymbol("", JsonToken.kListEnd, JsonToken.kListSep);
        }

        if (builder == null) {
            builder = new Sequence(Value.Type.STRING);
        }

        return builder;
    }

    protected void formatTo(JsonWriter writer, Config config)
            throws JsonException, ConfigException {
        writer.object();

        for (Map.Entry<String, Value > entry : config.entrySet()) {
            writer.key(entry.getKey());
            switch (entry.getValue().type) {
                case STRING:
                    writer.value(entry.getValue().asString());
                    break;
                case NUMBER:
                    if (entry.getValue().value instanceof Double) {
                        writer.value(entry.getValue().asDouble());
                    } else {
                        writer.value(entry.getValue().asLong());
                    }
                    break;
                case BOOLEAN:
                    writer.value(entry.getValue().asBoolean());
                    break;
                case CONFIG:
                    formatTo(writer, entry.getValue().asConfig());
                    break;
                case SEQUENCE:
                    formatTo(writer, entry.getValue().asSequence());
                    break;
                default:
                    throw new ConfigException("Unhandled type in formatter: " + entry.getValue().type);
            }
        }
        writer.endObject();
    }

    protected void formatTo(JsonWriter writer, Sequence sequence)
            throws JsonException, ConfigException {
        writer.array();
        for (Value item : sequence.asValueArray()) {
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
