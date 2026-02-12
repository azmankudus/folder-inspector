package dev.ayam.folderinspector.scanner.local.utility;

import java.util.ResourceBundle;

/**
 * Utility class providing access to localized string resources for the Local
 * scanner.
 */
public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_scanner_local");

  /** Label for directory items. */
  public static final String FILE_TYPE_DIRECTORY = BUNDLE.getString("FILE_TYPE_DIRECTORY");
  /** Label for symbolic link items. */
  public static final String FILE_TYPE_SYMLINK = BUNDLE.getString("FILE_TYPE_SYMLINK");
  /** Label for regular file items. */
  public static final String FILE_TYPE_FILE = BUNDLE.getString("FILE_TYPE_FILE");
  /** Label for unavailable information (N/A). */
  public static final String NA = BUNDLE.getString("NA");
  /** The localized name of the scanner. */
  public static final String NAME = BUNDLE.getString("NAME");
  /** Error message shown when a path cannot be accessed. */
  public static final String ERROR_ACCESS_FAILED = BUNDLE.getString("ERROR_ACCESS_FAILED");
  /** Trace message when POSIX attribute retrieval fails. */
  public static final String ERROR_POSIX_FAILED = BUNDLE.getString("ERROR_POSIX_FAILED");
  /** Trace message when ACL retrieval fails. */
  public static final String ERROR_ACL_FAILED = BUNDLE.getString("ERROR_ACL_FAILED");
  /** Debug message logged when starting a scan. */
  public static final String STARTING_SCAN = BUNDLE.getString("STARTING_SCAN");
  /** Debug message logged when a scan is completed. */
  public static final String SCAN_COMPLETED = BUNDLE.getString("SCAN_COMPLETED");
}
