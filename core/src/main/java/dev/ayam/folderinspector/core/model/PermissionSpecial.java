package dev.ayam.folderinspector.core.model;

/**
 * Enum representing special file system permissions (e.g., setuid, setgid,
 * sticky).
 */
public enum PermissionSpecial {
  /** Set User ID on execution. */
  SETUID(1, 's', "SetUID"),
  /** Set Group ID on execution. */
  SETGID(2, 's', "SetGID"),
  /** Sticky bit. */
  STICKY(3, 't', "Sticky");

  /** Unique identifier for the special permission. */
  public final int id;
  /** Short character code representing the special permission. */
  public final char code;
  /** Human-readable name of the special permission. */
  public final String name;

  /**
   * Constructs a PermissionSpecial.
   *
   * @param id   The unique identifier.
   * @param code The character code.
   * @param name The human-readable name.
   */
  PermissionSpecial(int id, char code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }
}
