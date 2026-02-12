package dev.ayam.folderinspector.formatter.text.utility;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the Text
 * formatter.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_formatter_text");

  /** List of table headers as strings. */
  public static final List<String> TABLE_HEADERS = Arrays.asList(BUNDLE.getString("TABLE_HEADERS").split(","));
  /** Message shown when no items are found during a scan. */
  public static final String NO_ITEMS_FOUND = BUNDLE.getString("NO_ITEMS_FOUND");
  /** The localized name of the formatter. */
  public static final String NAME = BUNDLE.getString("NAME");
  /** Log message for formatting status. */
  public static final String LOG_FORMATTING = BUNDLE.getString("LOG_FORMATTING");
}
