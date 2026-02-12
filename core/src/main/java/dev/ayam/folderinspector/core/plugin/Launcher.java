package dev.ayam.folderinspector.core.plugin;

/**
 * Interface for launching the application logic.
 * Different launchers can provide different interfaces (e.g., CLI, REST, Web).
 */
public interface Launcher {
    /**
     * Launches the scanning process with the specified components and path.
     *
     * @param database  The database plugin to use for snapshot operations.
     * @param scanner   The scanner to use for gathering file system data.
     * @param formatter The formatter to use for converting items to strings.
     * @param writer    The writer to use for outputting the formatted data.
     * @param notifier  The notifier to use for sending status updates or errors.
     * @param scheduler The scheduler plugin to use for background scans.
     * @param path      The root path to start the scan from.
     * @return An exit code (0 for success, non-zero for failure).
     */
    int launch(Database database, Scanner scanner, Formatter formatter, Writer writer, Notifier notifier,
            Scheduler scheduler, String path);

    /**
     * Returns the unique name of this launcher implementation.
     *
     * @return The launcher name (e.g., "console", "rest").
     */
    String getName();
}
