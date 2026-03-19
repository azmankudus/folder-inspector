package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;

/**
 * Stores log messages generated during a scan job.
 */
@Serdeable
@MappedEntity("tb_job_log")
public record JobLog(
        @Id @GeneratedValue Long id,
        Long jobId,
        String logLevel,
        String message,
        OffsetDateTime timestamp) {
}
