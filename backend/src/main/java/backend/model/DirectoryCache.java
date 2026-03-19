package backend.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("tb_directory_cache")
public record DirectoryCache(
                @Id String sid,
                String resolutionName) {
}
