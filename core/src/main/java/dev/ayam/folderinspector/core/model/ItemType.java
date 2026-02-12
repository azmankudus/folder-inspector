package dev.ayam.folderinspector.core.model;

/**
 * Enum representing the type of a file system item.
 */
public enum ItemType {
  /** A regular file. */
  FILE(1, 'f', "File"),
  /** A directory. */
  DIRECTORY(2, 'd', "Directory");

  /** Unique identifier for the item type. */
  public final int id;
  /** Short character code representing the item type. */
  public final char code;
  /** Human-readable name of the item type. */
  public final String name;

  /**
   * Constructs an ItemType.
   *
   * @param id   The unique identifier.
   * @param code The character code.
   * @param name The human-readable name.
   */
  ItemType(int id, char code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }
}
