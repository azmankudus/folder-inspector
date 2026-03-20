package backend.repository;

import backend.model.UserJobStat;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface UserJobStatRepository extends CrudRepository<UserJobStat, Long> {
    Optional<UserJobStat> findByJobIdAndUsername(Long jobId, String username);
    List<UserJobStat> findByJobIdInAndUsername(List<Long> jobIds, String username);
}
