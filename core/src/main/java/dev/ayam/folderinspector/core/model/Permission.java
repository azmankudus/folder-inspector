package dev.ayam.folderinspector.core.model;

import java.util.Set;

/**
 * Record representing file system permissions.
 *
 * @param owner   The set of permissions for the owner.
 * @param group   The set of permissions for the group.
 * @param other   The set of permissions for others.
 * @param special The set of special permissions.
 */
public record Permission(
        Set<PermissionType> owner,
        Set<PermissionType> group,
        Set<PermissionType> other,
        Set<PermissionSpecial> special) {

}
