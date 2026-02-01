package dev.ayam.folderinspector.core.utility;

import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_core");

  public static final String AVAILABLE_PLUGINS_HEADER = BUNDLE.getString("AVAILABLE_PLUGINS_HEADER");
  public static final String LAUNCHERS_LABEL = BUNDLE.getString("LAUNCHERS_LABEL");
  public static final String SCANNERS_LABEL = BUNDLE.getString("SCANNERS_LABEL");
  public static final String WRITERS_LABEL = BUNDLE.getString("WRITERS_LABEL");
  public static final String NOTIFIERS_LABEL = BUNDLE.getString("NOTIFIERS_LABEL");
  public static final String UNKNOWN_PLUGIN = BUNDLE.getString("UNKNOWN_PLUGIN");
  public static final String NONE_PLUGIN = BUNDLE.getString("NONE_PLUGIN");
  public static final String ERROR_PREFIX = BUNDLE.getString("ERROR_PREFIX");
  public static final String BOOTSTRAPPING_APP = BUNDLE.getString("BOOTSTRAPPING_APP");
  public static final String PLUGINS_LOADED = BUNDLE.getString("PLUGINS_LOADED");

  public static final String ERR_NOTIFIER_NOT_FOUND = BUNDLE.getString("ERR_NOTIFIER_NOT_FOUND");
  public static final String ERR_SCANNER_NOT_FOUND = BUNDLE.getString("ERR_SCANNER_NOT_FOUND");
  public static final String ERR_WRITER_NOT_FOUND = BUNDLE.getString("ERR_WRITER_NOT_FOUND");
  public static final String ERR_LAUNCHER_NOT_FOUND = BUNDLE.getString("ERR_LAUNCHER_NOT_FOUND");
  public static final String ERR_SERVICE_NAME_FAILED = BUNDLE.getString("ERR_SERVICE_NAME_FAILED");
  public static final String ERR_EXECUTION_FAILED = BUNDLE.getString("ERR_EXECUTION_FAILED");
}
