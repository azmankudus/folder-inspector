package dev.ayam.folderinspector.formatter.text.utility;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_formatter_text");

  public static final List<String> TABLE_HEADERS = Arrays.asList(BUNDLE.getString("TABLE_HEADERS").split(","));
  public static final String NO_ITEMS_FOUND = BUNDLE.getString("NO_ITEMS_FOUND");
  public static final String NAME = BUNDLE.getString("NAME");
  public static final String LOG_FORMATTING = BUNDLE.getString("LOG_FORMATTING");
}
