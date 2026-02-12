package dev.ayam.folderinspector.launcher.console;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.plugin.Launcher;
import dev.ayam.folderinspector.core.plugin.Notifier;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.plugin.Scheduler;
import dev.ayam.folderinspector.core.plugin.Writer;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.ayam.folderinspector.launcher.console.utility.StringResource;

/**
 * Console implementation of the {@link Launcher}.
 * Orchestrates the scan, format, and notify loop.
 */
public class ConsoleLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLauncher.class);

    /**
     * Launches the application logic: scanning, formatting, and notifying.
     *
     * @param database  The database plugin to use.
     * @param scanner   The scanner to use.
     * @param formatter The formatter to use.
     * @param writer    The writer to use.
     * @param notifier  The notifier to use.
     * @param path      The path to scan.
     * @return The exit code (0 for success, 1 for error).
     */
    @Override
    public int launch(Database database, Scanner scanner, Formatter formatter, Writer writer, Notifier notifier,
            Scheduler scheduler, String path) {
        logger.info(StringResource.LAUNCH_MESSAGE, path);

        Path rootPath = Paths.get(path);
        try (java.util.stream.Stream<Item> items = scanner.scan(rootPath);
                java.util.stream.Stream<String> formattedContent = formatter.format(items)) {

            String output = writer.write(formattedContent);
            notifier.notify(output);
            return 0;
        } catch (IOException e) {
            String error = StringResource.ERROR_SCANNING + e.getMessage();
            logger.error(error, e);
            notifier.notifyError(error);
            return 1;
        }
    }

    /**
     * Returns the name of this launcher.
     *
     * @return "console"
     */
    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
