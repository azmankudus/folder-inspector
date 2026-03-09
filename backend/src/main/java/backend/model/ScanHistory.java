package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.time.LocalDateTime;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents the execution history of a scan profile.
 * Tracks progress, timing, and completion status.
 */
@Serdeable
@MappedEntity
public record ScanHistory(
    @Id @GeneratedValue Long id,
    Long scanProfileId,
    Integer progress,
    LocalDateTime startTime,
    @Nullable LocalDateTime finishTime,
    String status,
    @Nullable Long processId,
    @Nullable String logPath
) {}
