package dev.ayam.folderinspector.scanner.local.utility;

import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_scanner_local");

  public static final String FILE_TYPE_DIRECTORY = BUNDLE.getString("FILE_TYPE_DIRECTORY");
  public static final String FILE_TYPE_SYMLINK = BUNDLE.getString("FILE_TYPE_SYMLINK");
  public static final String FILE_TYPE_FILE = BUNDLE.getString("FILE_TYPE_FILE");
  public static final String NA = BUNDLE.getString("NA");
  public static final String NAME = BUNDLE.getString("NAME");
  public static final String ERROR_ACCESS_FAILED = BUNDLE.getString("ERROR_ACCESS_FAILED");
  public static final String ERROR_POSIX_FAILED = BUNDLE.getString("ERROR_POSIX_FAILED");
  public static final String ERROR_ACL_FAILED = BUNDLE.getString("ERROR_ACL_FAILED");
  public static final String STARTING_SCAN = BUNDLE.getString("STARTING_SCAN");
  public static final String SCAN_COMPLETED = BUNDLE.getString("SCAN_COMPLETED");
}
