package net.morimekta.config.impl;

import net.morimekta.config.format.JsonConfigParser;
import net.morimekta.config.format.TomlConfigParser;
import net.morimekta.config.source.FileConfigSupplier;
import net.morimekta.config.source.RefreshingFileConfigSupplier;
import net.morimekta.config.source.ResourceConfigSupplier;
import net.morimekta.util.io.IOUtils;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Tests for the layered config.
 */
public class LayeredConfigTest {
    @Rule
    public TemporaryFolder temp;

    @Before
    public void setUp() throws IOException {
        temp = new TemporaryFolder();
        temp.create();
    }

    @Test
    public void testLayeredConfig() {
        LayeredConfig config = new LayeredConfig();
        config.addFixedTopLayer(() -> new SimpleConfig().putString("common", "fixed-top")
                                                        .putString("fixed-top", "fixed-top")
                                                        .putString("top-2", "fixed-top"));
        config.addFixedBottomLayer(() -> new SimpleConfig().putString("common", "fixed-bottom")
                                                           .putString("fixed-bottom", "fixed-bottom")
                                                           .putString("bottom-2", "fixed-bottom"));

        config.addBottomLayer(() -> new SimpleConfig().putString("common", "bottom")
                                                      .putString("bottom", "bottom")
                                                      .putString("middle-2", "bottom")
                                                      .putString("bottom-2", "bottom"));
        // Is added *after* the fixed top.
        config.addTopLayer(() -> new SimpleConfig().putString("common", "top")
                                                   .putString("top", "top")
                                                   .putString("top-2", "top")
                                                   .putString("middle-2", "top"));

        assertEquals("fixed-top", config.getString("common"));

        assertEquals("fixed-top", config.getString("top-2"));
        assertEquals("top", config.getString("middle-2"));
        assertEquals("bottom", config.getString("bottom-2"));
        assertEquals("fixed-top", config.getString("fixed-top"));
        assertEquals("top", config.getString("top"));
        assertEquals("bottom", config.getString("bottom"));

        assertEquals(ImmutableSet.of("bottom",
                                     "bottom-2",
                                     "common",
                                     "fixed-bottom",
                                     "fixed-top",
                                     "middle-2",
                                     "top",
                                     "top-2"), config.keySet());
    }

    @Test
    public void testGetLayerFor() throws IOException {
        File toml = temp.newFile("config.toml");
        try (FileOutputStream fos = new FileOutputStream(toml);
             InputStream in = getClass().getResourceAsStream("/net/morimekta/config/impl/config.toml")) {
            IOUtils.copy(in, fos);
        }
        File json = temp.newFile("config-3.json");
        try (FileOutputStream fos = new FileOutputStream(json);
             InputStream in = getClass().getResourceAsStream("/net/morimekta/config/impl/config-3.json")) {
            IOUtils.copy(in, fos);
        }

        LayeredConfig config = new LayeredConfig();
        config.addBottomLayer(new FileConfigSupplier(toml, new TomlConfigParser()));
        config.addBottomLayer(new ResourceConfigSupplier("/net/morimekta/config/impl/config-2.json", new JsonConfigParser()));
        config.addBottomLayer(new RefreshingFileConfigSupplier(json, new JsonConfigParser()));

        assertThat(config.getLayerFor("s"),
                   endsWith("/config.toml}"));
        assertThat(config.getLayerFor("s"),
                   startsWith("FileConfigSupplier{file=/"));
        assertEquals("ResourceConfigSupplier{resource=/net/morimekta/config/impl/config-2.json}",
                     config.getLayerFor("bottom-2"));
        assertThat(config.getLayerFor("bottom-2"),
                   startsWith("ResourceConfigSupplier{resource=/"));
        assertThat(config.getLayerFor("seq_i"),
                   endsWith("/config.toml}"));
        assertThat(config.getLayerFor("seq_i"),
                   startsWith("FileConfigSupplier{file=/"));
        assertEquals("ResourceConfigSupplier{resource=/net/morimekta/config/impl/config-2.json}",
                     config.getLayerFor("config-2"));
        assertThat(config.getLayerFor("config-3"),
                   endsWith("/config-3.json}"));
        assertThat(config.getLayerFor("config-3"),
                   startsWith("RefreshingFileConfigSupplier{file=/"));
        assertNull(config.getLayerFor("not.exists"));
    }
}
