package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for scanning a file system or simulated environment.
 */
public interface Scanner {
    /**
     * Scans the specified root path and returns a stream of found items.
     *
     * @param rootPath The root directory path to scan.
     * @return A stream of {@link Item} objects.
     * @throws IOException If an I/O error occurs during the scan.
     */
    java.util.stream.Stream<Item> scan(Path rootPath) throws IOException;

    /**
     * Returns the unique name of this scanner implementation.
     *
     * @return The scanner name (e.g., "local", "dummy").
     */
    String getName();
}
