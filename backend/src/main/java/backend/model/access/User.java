package backend.model.access;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a user account within the system.
 */
@Serdeable
@MappedEntity("tb_access_user")
public record User(
                @Id @GeneratedValue Long id,
                String username,
                String password) {
}
