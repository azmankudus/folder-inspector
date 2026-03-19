package backend.repository;

import backend.model.Job;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

/**
 * Repository for managing {@link Job} entities.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface JobRepository extends CrudRepository<Job, Long> {
    boolean existsByScanConfigIdAndStatus(Long scanConfigId, String status);

    @io.micronaut.data.annotation.Query("""
            SELECT count(*) > 0
              FROM tb_job j
              JOIN tb_scan_config sc ON j.scan_config_id = sc.id
             WHERE sc.server_config_id = :serverConfigId
               AND j.status = :status
            """)
    boolean existsByServerConfigIdAndStatus(Long serverConfigId, String status);

    java.util.List<Job> findByStatus(String status);

    java.util.List<Job> findByScanConfigId(Long scanConfigId);
}
