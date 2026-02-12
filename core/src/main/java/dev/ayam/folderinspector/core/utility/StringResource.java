package dev.ayam.folderinspector.core.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_core");

  /** Header for the list of available plugins. */
  public static final String AVAILABLE_PLUGINS_HEADER = BUNDLE.getString("AVAILABLE_PLUGINS_HEADER");
  /** Label for launchers. */
  public static final String LAUNCHERS_LABEL = BUNDLE.getString("LAUNCHERS_LABEL");
  /** Label for scanners. */
  public static final String SCANNERS_LABEL = BUNDLE.getString("SCANNERS_LABEL");
  /** Label for writers. */
  public static final String WRITERS_LABEL = BUNDLE.getString("WRITERS_LABEL");
  /** Label for notifiers. */
  public static final String NOTIFIERS_LABEL = BUNDLE.getString("NOTIFIERS_LABEL");
  /** Label for formatters. */
  public static final String FORMATTERS_LABEL = BUNDLE.getString("FORMATTERS_LABEL");
  /** Placeholder for an unknown plugin. */
  public static final String UNKNOWN_PLUGIN = BUNDLE.getString("UNKNOWN_PLUGIN");
  /** Placeholder for no plugins found. */
  public static final String NONE_PLUGIN = BUNDLE.getString("NONE_PLUGIN");
  /** Prefix for error messages. */
  public static final String ERROR_PREFIX = BUNDLE.getString("ERROR_PREFIX");
  /** Log message for application bootstrapping. */
  public static final String BOOTSTRAPPING_APP = BUNDLE.getString("BOOTSTRAPPING_APP");
  /** Log message for plugins loaded. */
  public static final String PLUGINS_LOADED = BUNDLE.getString("PLUGINS_LOADED");

  /** Error message when a notifier is not found. */
  public static final String ERR_NOTIFIER_NOT_FOUND = BUNDLE.getString("ERR_NOTIFIER_NOT_FOUND");
  /** Error message when a scanner is not found. */
  public static final String ERR_SCANNER_NOT_FOUND = BUNDLE.getString("ERR_SCANNER_NOT_FOUND");
  /** Error message when a writer is not found. */
  public static final String ERR_WRITER_NOT_FOUND = BUNDLE.getString("ERR_WRITER_NOT_FOUND");
  /** Error message when a formatter is not found. */
  public static final String ERR_FORMATTER_NOT_FOUND = BUNDLE.getString("ERR_FORMATTER_NOT_FOUND");
  /** Error message when a launcher is not found. */
  public static final String ERR_LAUNCHER_NOT_FOUND = BUNDLE.getString("ERR_LAUNCHER_NOT_FOUND");
  /** Error message when service name retrieval fails. */
  public static final String ERR_SERVICE_NAME_FAILED = BUNDLE.getString("ERR_SERVICE_NAME_FAILED");
  /** Error message when execution fails. */
  public static final String ERR_EXECUTION_FAILED = BUNDLE.getString("ERR_EXECUTION_FAILED");
}
