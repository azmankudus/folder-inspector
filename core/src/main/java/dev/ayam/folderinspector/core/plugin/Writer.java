package dev.ayam.folderinspector.core.plugin;

/**
 * Interface for writing formatted item content to a destination.
 * Implementations can write to the console, a file, a database, etc.
 */
public interface Writer {
    /**
     * Writes the specified content stream to the configured destination.
     *
     * @param content A stream of formatted strings to write.
     * @return A status message or identifier related to the write operation (e.g.,
     *         file path).
     */
    String write(java.util.stream.Stream<String> content);

    /**
     * Returns the unique name of this writer implementation.
     *
     * @return The writer name (e.g., "console", "file").
     */
    String getName();
}
