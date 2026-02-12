package dev.ayam.folderinspector.core.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Record representing a file system item (file or directory) with its metadata.
 *
 * @param id           Unique identifier for the item.
 * @param name         The name of the item.
 * @param parent       The parent path of the item.
 * @param type         The type of the item (File, Directory).
 * @param size         The size in bytes (usually 0 for directories).
 * @param lastModified The last modified timestamp.
 * @param created      The creation timestamp.
 * @param owner        The owner of the item.
 * @param group        The group of the item.
 * @param permissions  The set of permissions applied to the item.
 * @param acls         The list of Access Control List (ACL) entries, if
 *                     available.
 */
public record Item(
        Long id,
        String name,
        String parent,
        ItemType type,
        long size,
        Instant lastModified,
        Instant created,
        Principal owner,
        Principal group,
        Set<PermissionType> permissions,
        List<AclItem> acls) {
}
