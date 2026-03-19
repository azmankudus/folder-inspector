package backend.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;
import java.util.List;
import backend.model.ItemAcl;

@Serdeable
@Introspected
public record ItemReportDTO(
    Long id,
    String name,
    String path,
    String nodeType,
    Long sizeBytes,
    OffsetDateTime lastModified,
    String ownerName,
    String groupName,
    List<ItemAcl> acls) {
}
