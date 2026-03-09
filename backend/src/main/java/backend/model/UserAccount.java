package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a user account within the system.
 */
@Serdeable
@MappedEntity
public record UserAccount(
    @Id @GeneratedValue Long id,
    String username,
    String password
) {}
