package dev.ayam.folderinspector.launcher.console;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.Launcher;
import dev.ayam.folderinspector.core.Notifier;
import dev.ayam.folderinspector.core.Scanner;
import dev.ayam.folderinspector.core.Writer;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console implementation of the Launcher.
 * Orchestrates the scan, format, and notify loop.
 */
public class ConsoleLauncher implements Launcher {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLauncher.class);

    @Override
    public int launch(Scanner scanner, Writer writer, Notifier notifier, String path) {
        logger.info("Launching Console workflow for path: {}", path);

        Path rootPath = Paths.get(path);
        try {
            List<Item> items = scanner.scan(rootPath);
            String output = writer.write(items);
            notifier.notify(output);
            return 0;
        } catch (IOException e) {
            String error = "Error during scanning: " + e.getMessage();
            logger.error(error, e);
            notifier.notifyError(error);
            return 1;
        }
    }

    @Override
    public String getName() {
        return "console";
    }
}
