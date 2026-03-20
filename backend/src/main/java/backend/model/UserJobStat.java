package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("tb_user_job_stats")
public record UserJobStat(
    @Id @GeneratedValue Long id,
    Long jobId,
    String username,
    Long totalFiles,
    Long totalFolders,
    Long ownedFiles,
    Long totalRead,
    Long totalWrite,
    Long totalExecute
) {}
