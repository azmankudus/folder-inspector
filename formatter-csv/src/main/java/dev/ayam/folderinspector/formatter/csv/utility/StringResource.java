package dev.ayam.folderinspector.formatter.csv.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the CSV
 * formatter.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_formatter_csv");

  /** Header label for absolute path. */
  public static final String HEADER_ABSOLUTE_PATH = BUNDLE.getString("HEADER_ABSOLUTE_PATH");
  /** Header label for relative path. */
  public static final String HEADER_RELATIVE_PATH = BUNDLE.getString("HEADER_RELATIVE_PATH");
  /** Header label for item type. */
  public static final String HEADER_TYPE = BUNDLE.getString("HEADER_TYPE");
  /** Header label for item size. */
  public static final String HEADER_SIZE = BUNDLE.getString("HEADER_SIZE");
  /** Header label for last modified timestamp. */
  public static final String HEADER_LAST_MODIFIED = BUNDLE.getString("HEADER_LAST_MODIFIED");
  /** Header label for creation timestamp. */
  public static final String HEADER_CREATED = BUNDLE.getString("HEADER_CREATED");
  /** Header label for owner. */
  public static final String HEADER_OWNER = BUNDLE.getString("HEADER_OWNER");
  /** Header label for group. */
  public static final String HEADER_GROUP = BUNDLE.getString("HEADER_GROUP");
  /** Header label for permissions. */
  public static final String HEADER_PERMISSIONS = BUNDLE.getString("HEADER_PERMISSIONS");
  /** Header label for Access Control Lists. */
  public static final String HEADER_ACLS = BUNDLE.getString("HEADER_ACLS");

  /** Error message when writing CSV fails. */
  public static final String ERROR_WRITING_CSV = BUNDLE.getString("ERROR_WRITING_CSV");
  /** Error message when generating output fails. */
  public static final String ERROR_GENERATING_OUTPUT = BUNDLE.getString("ERROR_GENERATING_OUTPUT");
}
