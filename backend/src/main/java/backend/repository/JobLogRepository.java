package backend.repository;

import backend.model.JobLog;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface JobLogRepository extends CrudRepository<JobLog, Long> {
    List<JobLog> findByJobIdOrderByTimestampAsc(Long jobId);
}
