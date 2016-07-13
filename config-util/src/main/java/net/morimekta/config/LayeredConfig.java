package net.morimekta.config;

import net.morimekta.config.source.FileConfigSupplier;
import net.morimekta.config.source.RefreshingFileConfigSupplier;
import net.morimekta.config.source.ResourceConfigSupplier;

import java.util.function.Supplier;

/**
 * A config based on a layered set of configs. The config has four layer
 * groups, two above and two below the <em>middle</em>.
 *
 * <ul>
 *     <li>--fixed-top--</li>
 *     <li><b>[w] Layers</b>: Lifo</li>
 *     <li>--top--</li>
 *     <li><b>[x] Layers</b>: Lifo</li>
 *     <li>--middle--</li>
 *     <li><b>[y] Layers</b>: Fifo</li>
 *     <li>--bottom--</li>
 *     <li><b>[z] Layers</b>: Fifo</li>
 *     <li>--fixed-bottom--</li>
 * </ul>
 *
 * When layers are added, they are added to one of the four groups, and inserted
 * furthest <b>away</b> from the middle. The middle is actually just a theoretical
 * position, as the 'top' and 'bottom' groups insert away from that line.
 */
public interface LayeredConfig extends Config {
    /**
     * Add a new fixed top layer. The new config will be handled before all
     * other configs added before this.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    LayeredConfig addFixedTopLayer(Supplier<Config> supplier);

    /**
     * Add a new top layer. The new config will be handled before the
     * other top configs added before this, but after fixed-top configs.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    LayeredConfig addTopLayer(Supplier<Config> supplier);

    /**
     * Add a new bottom layer. The new config will be handled after the
     * other bottom configs added before this, but before fixed-bottom
     * configs.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    LayeredConfig addBottomLayer(Supplier<Config> supplier);

    /**
     * Add a new fixed bottom layer. The new config will be handled after all
     * other configs added before this.
     *
     * @param supplier The config supplier.
     * @return The config.
     */
    LayeredConfig addFixedBottomLayer(Supplier<Config> supplier);

    /**
     * Get the 'toString()' value for the config supplier for the layer that
     * contains the wanted key. If the supplier is a lambda expression we
     * assume it is just providing the config from memory.
     *
     * See the toString entries for the different config sources (See:
     * {@link FileConfigSupplier#toString()},
     * {@link RefreshingFileConfigSupplier#toString()},
     * {@link ResourceConfigSupplier#toString()}).
     *
     * @param key The key to get layer for.
     * @return The layer number and name.
     */
    String getLayerFor(String key);
}
