package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.OffsetDateTime;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

/**
* Represents the execution history of a scan profile.
* Tracks progress, timing, and completion status.
*/
@Serdeable
@MappedEntity("tb_job")
public record Job(
    @Id @GeneratedValue Long id,
    @Nullable Long scanConfigId,
    @Nullable Integer progress,
    @Nullable OffsetDateTime startTime,
    @Nullable OffsetDateTime finishTime,
    @Nullable String status,
    @Nullable Long processId,
    @Nullable String logPath,
    @Nullable Long totalFiles,
    @Nullable Long totalFolders,
    @Nullable Long totalExceptions) {
}
