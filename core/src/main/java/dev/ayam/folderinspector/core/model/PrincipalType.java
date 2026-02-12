package dev.ayam.folderinspector.core.model;

/**
 * Enum representing the type of a security principal (user or group).
 */
public enum PrincipalType {
  /** A user principal. */
  USER(1, 'u', "User"),
  /** A group principal. */
  GROUP(2, 'g', "Group");

  /** Unique identifier for the principal type. */
  public final int id;
  /** Short character code representing the principal type. */
  public final char code;
  /** Human-readable name of the principal type. */
  public final String name;

  /**
   * Constructs a PrincipalType.
   *
   * @param id   The unique identifier.
   * @param code The character code.
   * @param name The human-readable name.
   */
  PrincipalType(int id, char code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }
}
