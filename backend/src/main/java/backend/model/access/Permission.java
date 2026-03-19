package backend.model.access;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("tb_access_permission")
public record Permission(
        @Id @GeneratedValue Long id,
        String name) {
}
