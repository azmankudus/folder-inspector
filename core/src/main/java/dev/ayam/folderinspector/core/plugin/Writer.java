package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;

/**
 * Interface for formatting a list of {@link Item}s into a string
 * representation.
 */
public interface Writer {
    /**
     * Writes the given content to the destination.
     *
     * @param content The content stream to write.
     * @return The output string (e.g. for console or file path), or null/empty if
     *         not applicable.
     */
    String write(java.util.stream.Stream<String> content);

    /**
     * @return The name of this writer implementation (e.g., "console", "csv").
     */
    String getName();
}
