package dev.ayam.folderinspector.core.model;

/**
 * Enum representing specific permission types for both Unix and NTFS/Windows
 * systems.
 */
public enum PermissionType {
  /* Unix Permissions */
  /** Unix read permission. */
  UNIX_READ(1, 'r', "Read"),
  /** Unix write permission. */
  UNIX_WRITE(2, 'w', "Write"),
  /** Unix execute permission. */
  UNIX_EXECUTE(3, 'x', "Execute"),

  /* NTFS Permissions */
  /** NTFS full control permission. */
  NTFS_FULLCONTROL(4, 'f', "Full Control"),
  /** NTFS modify permission. */
  NTFS_MODIFY(5, 'm', "Modify"),
  /** NTFS read and execute permission. */
  NTFS_READEXECUTE(6, 'x', "Read and Execute"),
  /** NTFS read permission. */
  NTFS_READ(7, 'r', "Read"),
  /** NTFS write permission. */
  NTFS_WRITE(8, 'w', "Write"),
  /** NTFS list folder contents permission. */
  NTFS_LIST(9, 'l', "List");

  /** Unique identifier for the permission type. */
  public final int id;
  /** Short character code representing the permission type. */
  public final char code;
  /** Human-readable name of the permission type. */
  public final String name;

  /**
   * Constructs a PermissionType.
   *
   * @param id   The unique identifier.
   * @param code The character code.
   * @param name The human-readable name.
   */
  PermissionType(int id, char code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }
}
