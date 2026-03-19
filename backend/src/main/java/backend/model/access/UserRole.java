package backend.model.access;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("tb_access_user_role")
public record UserRole(
        @Id @GeneratedValue Long id,
        Long userId,
        Long roleId) {
}
