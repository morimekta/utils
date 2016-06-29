package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.util.json.JsonException;
import net.morimekta.util.json.JsonWriter;
import net.morimekta.util.json.PrettyJsonWriter;

import java.io.OutputStream;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Config formatter for JSON object syntax.
 */
public class JsonConfigFormatter implements ConfigFormatter {
    private final boolean pretty;

    public JsonConfigFormatter() {
        this(false);
    }

    public JsonConfigFormatter(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public void format(Config config, OutputStream out) {
        try {
            JsonWriter writer = pretty ? new PrettyJsonWriter(out) : new JsonWriter(out);
            writer.object();
            // Make sure entries are ordered (makes the output consistent).
            for (String key : new TreeSet<>(config.keySet())) {
                writer.key(key);
                writeValue(writer, config.get(key));
            }
            writer.endObject();
            writer.flush();
        } catch (JsonException e) {
            throw new ConfigException(e, e.getMessage());
        }
    }

    private void writeValue(JsonWriter writer, Object value)
            throws JsonException, ConfigException {
        if (value instanceof Boolean) {
            writer.value((Boolean) value);
        } else if (value instanceof Double) {
            writer.value((Double) value);
        } else if (value instanceof Number) {
            writer.value(((Number) value).longValue());
        } else if (value instanceof CharSequence) {
            writer.value((CharSequence) value);
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            writer.array();
            for (Object o : collection) {
                writeValue(writer, o);
            }
            writer.endArray();
        } else {
            throw new ConfigException("Unknown value class: " + value.getClass().getSimpleName());
        }
    }
}
