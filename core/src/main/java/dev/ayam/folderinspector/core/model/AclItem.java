package dev.ayam.folderinspector.core.model;

import java.util.Set;

/**
 * Record representing an Access Control List (ACL) entry.
 *
 * @param name         The name of the principal (user or group).
 * @param permissions  The set of permissions granted or denied.
 * @param inheritFlags The set of inheritance flags.
 */
public record AclItem(String name, Set<PermissionType> permissions, Set<InheritFlag> inheritFlags) {
}
