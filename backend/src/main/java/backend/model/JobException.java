package backend.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;

/**
 * Represents a specialized exception or warning captured during a scan.
 * Stores detailed path and RCA (Root Cause Analysis) information.
 */
@Serdeable
@MappedEntity("tb_job_exception")
public record JobException(
                @Id @GeneratedValue Long id,
                Long jobId,
                String level, // WARN, ERROR
                String path,
                String message,
                String reason,
                OffsetDateTime timestamp,
                @Nullable String note) {
}
