package dev.ayam.folderinspector.notifier.console;

import dev.ayam.folderinspector.core.plugin.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ayam.folderinspector.notifier.console.utility.StringResource;

/**
 * Console implementation of the {@link Notifier}.
 * Outputs messages to standard output and error output.
 */
public class ConsoleNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotifier.class);

    /**
     * Notifies the user with a general message by printing to standard output.
     *
     * @param message The message to notify.
     */
    @Override
    public void notify(String message) {
        System.out.println(message);
    }

    /**
     * Notifies the user of an error message by logging and printing to standard
     * error.
     *
     * @param message The error message to notify.
     */
    @Override
    public void notifyError(String message) {
        logger.error(message);
        System.err.println(message);
    }

    /**
     * Returns the name of this notifier.
     *
     * @return "console"
     */
    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
