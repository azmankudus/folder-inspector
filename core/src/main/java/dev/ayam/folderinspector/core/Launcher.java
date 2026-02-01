package dev.ayam.folderinspector.core;

/**
 * Interface for launching the application logic.
 */
public interface Launcher {
    /**
     * Arguments for launching.
     * 
     * @param scanner  The scanner to use.
     * @param writer   The writer to use.
     * @param notifier The notifier to use.
     * @param path     The path to scan.
     * @return Exit code.
     */
    int launch(Scanner scanner, Writer writer, Notifier notifier, String path);

    /**
     * @return The name of this launcher implementation (e.g., "cli").
     */
    String getName();
}
