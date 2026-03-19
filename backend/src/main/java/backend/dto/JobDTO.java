package backend.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;

@Serdeable
@Introspected
public record JobDTO(
    Long id,
    Long scanProfileId,
    Integer progress,
    OffsetDateTime startTime,
    OffsetDateTime finishTime,
    String status,
    Long totalFiles,
    Long totalFolders,
    Long totalExceptions,
    String hostname,
    String rootPath,
    String username,
    String logPath) {
}
