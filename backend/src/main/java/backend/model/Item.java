package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.OffsetDateTime;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

/**
* Represents a file or folder discovered during a scan.
* Includes metadata such as size, type, and ownership information.
*/
@Serdeable
@MappedEntity("tb_item")
public record Item(
        @Id @GeneratedValue Long id,
        Long jobId,
        @Nullable Long parentId,
        String name,
        String path,
        String nodeType, // FILE or FOLDER
        @Nullable Long sizeBytes,
        OffsetDateTime lastModified,
        @Nullable String ownerSid,
        @Nullable String ownerName,
        @Nullable String groupSid,
        @Nullable String groupName,
        Long scanProfileId) {
}
