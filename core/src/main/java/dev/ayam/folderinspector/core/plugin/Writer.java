package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;
import java.util.List;

/**
 * Interface for formatting a list of {@link Item}s into a string
 * representation.
 */
public interface Writer {
    /**
     * Formats a list of items.
     *
     * @param items The items to format.
     * @return The formatted output string.
     */
    String write(List<Item> items);

    /**
     * @return The name of this writer implementation (e.g., "console", "csv").
     */
    String getName();
}
