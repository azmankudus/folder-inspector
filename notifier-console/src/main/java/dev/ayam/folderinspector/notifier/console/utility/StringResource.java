package dev.ayam.folderinspector.notifier.console.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the Console
 * notifier.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_notifier_console");

  /** The localized name of the notifier. */
  public static final String NAME = BUNDLE.getString("NAME");
}
