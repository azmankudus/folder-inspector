package dev.ayam.folderinspector.writer.file.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the File
 * writer.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_writer_file");

  /** The localized name of the writer. */
  public static final String NAME = BUNDLE.getString("NAME");
}
