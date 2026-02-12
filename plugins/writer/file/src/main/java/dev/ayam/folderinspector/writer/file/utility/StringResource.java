package dev.ayam.folderinspector.writer.file.utility;

import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_writer_file");

  public static final String NAME = BUNDLE.getString("NAME");
}
