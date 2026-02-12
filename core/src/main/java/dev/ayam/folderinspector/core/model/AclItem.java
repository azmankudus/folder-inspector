package dev.ayam.folderinspector.core.model;

import java.util.List;

/**
 * Record representing an Access Control List (ACL) entry.
 *
 * @param name        The name of the principal (user or group).
 * @param permissions The list of permissions granted or denied.
 * @param flags       The list of inheritance flags.
 */
public record AclItem(String name, List<String> permissions, List<String> flags) {
}
