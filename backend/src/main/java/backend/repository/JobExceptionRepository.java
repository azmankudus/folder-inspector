package backend.repository;

import backend.model.JobException;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface JobExceptionRepository extends CrudRepository<JobException, Long> {
    List<JobException> findByJobId(Long jobId);
}
