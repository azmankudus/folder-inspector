package dev.ayam.folderinspector.core.model;

import java.time.Instant;
import java.util.List;

/**
 * Record representing a file system item (file, directory, link) with its
 * metadata.
 *
 * @param absolutePath The absolute path of the item.
 * @param relativePath The relative path from the scan root.
 * @param type         The type of the item (File, Directory, Symbolic Link).
 * @param size         The size in bytes (0 for directories).
 * @param lastModified The last modified timestamp.
 * @param created      The creation timestamp.
 * @param owner        The owner name.
 * @param group        The group name.
 * @param permissions  The POSIX permissions string (e.g., rwxr-xr-x).
 * @param acls         The list of ACL entries, if available.
 */
public record Item(
                String absolutePath,
                String relativePath,
                String type,
                long size,
                Instant lastModified,
                Instant created,
                String owner,
                String group,
                String permissions,
                List<AclItem> acls) {
}
