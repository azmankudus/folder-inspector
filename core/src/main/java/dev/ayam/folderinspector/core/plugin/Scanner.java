package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for scanning a directory.
 */
public interface Scanner {
    /**
     * Scans the given root path.
     *
     * @param rootPath The root directory to scan.
     * @return A stream of {@link Item} objects representing the files and
     *         directories
     *         found.
     * @throws IOException If an I/O error occurs during scanning.
     */
    java.util.stream.Stream<Item> scan(Path rootPath) throws IOException;

    /**
     * @return The name of this scanner implementation (e.g., "local").
     */
    String getName();
}
