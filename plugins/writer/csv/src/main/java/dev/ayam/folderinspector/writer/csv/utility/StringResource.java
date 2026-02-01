package dev.ayam.folderinspector.writer.csv.utility;

import java.util.ResourceBundle;

public class StringResource {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("strings_writer_csv");

  public static final String HEADER_ABSOLUTE_PATH = BUNDLE.getString("HEADER_ABSOLUTE_PATH");
  public static final String HEADER_RELATIVE_PATH = BUNDLE.getString("HEADER_RELATIVE_PATH");
  public static final String HEADER_TYPE = BUNDLE.getString("HEADER_TYPE");
  public static final String HEADER_SIZE = BUNDLE.getString("HEADER_SIZE");
  public static final String HEADER_LAST_MODIFIED = BUNDLE.getString("HEADER_LAST_MODIFIED");
  public static final String HEADER_CREATED = BUNDLE.getString("HEADER_CREATED");
  public static final String HEADER_OWNER = BUNDLE.getString("HEADER_OWNER");
  public static final String HEADER_GROUP = BUNDLE.getString("HEADER_GROUP");
  public static final String HEADER_PERMISSIONS = BUNDLE.getString("HEADER_PERMISSIONS");
  public static final String HEADER_ACLS = BUNDLE.getString("HEADER_ACLS");
  public static final String ERROR_WRITING_CSV = BUNDLE.getString("ERROR_WRITING_CSV");
  public static final String ERROR_GENERATING_OUTPUT = BUNDLE.getString("ERROR_GENERATING_OUTPUT");
}
