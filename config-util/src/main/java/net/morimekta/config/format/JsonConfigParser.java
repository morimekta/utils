package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.IncompatibleValueException;
import net.morimekta.config.SimpleConfig;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonToken;
import net.morimekta.util.json.JsonTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Config parser for JSON object syntax.
 */
public class JsonConfigParser implements ConfigParser {
    @Override
    public Config parse(InputStream in) {
        try {
            JsonTokenizer tokenizer = new JsonTokenizer(in);
            JsonToken token = tokenizer.next();
            if (!token.isSymbol(JsonToken.kMapStart)) {
                throw new ConfigException("Illegal json start token: %s", token);
            }
            return parseConfig(tokenizer, token);
        } catch (JsonException | IOException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    private Config parseConfig(JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
        Config config = new SimpleConfig();
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
                        case JsonToken.kListStart:
                            config.putSequence(key, parseSequence(tokenizer, token));
                            break;
                        default:
                            throw new IncompatibleValueException("No supported value type for " + token);
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
    private <T> Collection<T> parseSequence(JsonTokenizer tokenizer, JsonToken token)
            throws ConfigException, IOException, JsonException {
        List<T> builder = new LinkedList<>();
        char sep = token.charAt(0);
        while (sep != JsonToken.kListEnd) {
            token = tokenizer.expect("Array value.");
            switch (token.type) {
                case LITERAL:
                    builder.add((T) token.decodeJsonLiteral());
                    break;
                case NUMBER:
                    if (token.isInteger()) {
                        builder.add((T) (Object) token.longValue());
                    } else {
                        builder.add((T) (Object) token.doubleValue());
                    }
                    break;
                case TOKEN:
                    if (!token.isBoolean()) {
                        throw new IncompatibleValueException("Unrecognized value token " + token.asString());
                    }
                    builder.add((T) (Object) token.booleanValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled JSON value token: " + token);
            }

            sep = tokenizer.expectSymbol("List sep or end", JsonToken.kListEnd, JsonToken.kListSep);
        }

        return builder;
    }
}
