package dev.ayam.folderinspector.launcher.console.utility;

import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_launcher_console");

  public static final String LAUNCH_MESSAGE = BUNDLE.getString("LAUNCH_MESSAGE");
  public static final String ERROR_SCANNING = BUNDLE.getString("ERROR_SCANNING");
  public static final String NAME = BUNDLE.getString("NAME");
}
