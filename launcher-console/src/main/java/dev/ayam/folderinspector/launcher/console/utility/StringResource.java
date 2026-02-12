package dev.ayam.folderinspector.launcher.console.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the Console
 * launcher.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_launcher_console");

  /** Message logged when launching a scan. */
  public static final String LAUNCH_MESSAGE = BUNDLE.getString("LAUNCH_MESSAGE");
  /** Error message shown when a scan fails. */
  public static final String ERROR_SCANNING = BUNDLE.getString("ERROR_SCANNING");
  /** The localized name of the launcher. */
  public static final String NAME = BUNDLE.getString("NAME");
}
