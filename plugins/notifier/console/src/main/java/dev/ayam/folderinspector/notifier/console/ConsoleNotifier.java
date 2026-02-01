package dev.ayam.folderinspector.notifier.console;

import dev.ayam.folderinspector.core.plugin.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ayam.folderinspector.notifier.console.utility.StringResource;

public class ConsoleNotifier implements Notifier {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotifier.class);

    @Override
    public void notify(String message) {
        System.out.println(message);
    }

    @Override
    public void notifyError(String message) {
        logger.error(message);
        System.err.println(message);
    }

    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
