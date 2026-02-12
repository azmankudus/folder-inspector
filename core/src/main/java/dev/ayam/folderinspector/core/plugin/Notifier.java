package dev.ayam.folderinspector.core.plugin;

/**
 * Interface for providing notifications to the user.
 * Implementations can notify via console, desktop alerts, emails, etc.
 */
public interface Notifier {
    /**
     * Notifies the user with a general message.
     *
     * @param message The message to display.
     */
    void notify(String message);

    /**
     * Notifies the user with an error message.
     *
     * @param message The error message to display.
     */
    void notifyError(String message);

    /**
     * Returns the unique name of this notifier implementation.
     *
     * @return The notifier name (e.g., "console").
     */
    String getName();
}
