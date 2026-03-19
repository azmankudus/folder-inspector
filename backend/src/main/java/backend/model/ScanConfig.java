package backend.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Defines a scheduled scan job, including root path and scan mode.
 */
@Serdeable
@MappedEntity("tb_config_scan")
public record ScanConfig(
                @Id @GeneratedValue Long id,
                @Nullable Long serverConfigId,
                @Nullable String rootPath,
                @Nullable String schedulerCron) {
}
