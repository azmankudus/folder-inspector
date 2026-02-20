package dev.ayam.folderinspector.core.model;

/**
 * Record representing a security principal (user or group).
 *
 * @param id   Unique identifier for the principal.
 * @param sid  Security Identifier (SID) for the principal (e.g., Windows SID).
 * @param name The name of the principal.
 * @param type The type of the principal (User, Group).
 */
public record Principal(Long id, String sid, String name, PrincipalType type) {

}
