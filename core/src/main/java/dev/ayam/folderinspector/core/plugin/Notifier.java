package dev.ayam.folderinspector.core.plugin;

/**
 * Interface for notifying users.
 */
public interface Notifier {
    /**
     * Notify the user with a message.
     *
     * @param message The message to display.
     */
    void notify(String message);

    /**
     * Notify the user with an error message.
     * 
     * @param message The error message.
     */
    void notifyError(String message);

    /**
     * @return The name of this notifier implementation (e.g., "console").
     */
    String getName();
}
