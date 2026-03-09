package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Defines a scheduled scan job, including root path and scan mode.
 */
@Serdeable
@MappedEntity
public record ScanProfile(
    @Id @GeneratedValue Long id,
    Long connectionProfileId,
    String rootPath,
    String schedulerCron,
    ScanMode scanMode
) {}
