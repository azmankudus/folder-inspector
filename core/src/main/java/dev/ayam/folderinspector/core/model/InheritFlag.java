package dev.ayam.folderinspector.core.model;

/**
 * Enum representing inheritance flags for Access Control List (ACL) entries.
 */
public enum InheritFlag {
  /** Non-container child objects inherit the entry. */
  OBJECT_INHERIT(1, 'o', "Object Inherit"),
  /** Child containers inherit the entry. */
  CONTAINER_INHERIT(2, 'c', "Container Inherit"),
  /** The entry does not apply to the object itself, only to its children. */
  INHERIT_ONLY(3, 'i', "Inherit Only"),
  /** The entry is not propagated to child objects. */
  NO_PROPAGATE(4, 'n', "No Propagate");

  /** Unique identifier for the inheritance flag. */
  public final int id;
  /** Short character code representing the inheritance flag. */
  public final char code;
  /** Human-readable name of the inheritance flag. */
  public final String name;

  /**
   * Constructs an InheritFlag.
   *
   * @param id   The unique identifier.
   * @param code The character code.
   * @param name The human-readable name.
   */
  InheritFlag(int id, char code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }
}
