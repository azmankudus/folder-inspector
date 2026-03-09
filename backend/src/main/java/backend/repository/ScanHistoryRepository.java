package backend.repository;

import backend.model.ScanHistory;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

/**
 * Repository for managing {@link ScanHistory} entities.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ScanHistoryRepository extends CrudRepository<ScanHistory, Long> {
    boolean existsByScanProfileIdAndStatus(Long scanProfileId, String status);

    @io.micronaut.data.annotation.Query("SELECT count(*) > 0 FROM scan_history sh " +
            "JOIN scan_profile sp ON sh.scan_profile_id = sp.id " +
            "WHERE sp.connection_profile_id = :connectionProfileId " +
            "AND sh.status = :status")
    boolean existsByConnectionProfileIdAndStatus(Long connectionProfileId, String status);
}
