package backend.repository;

import backend.model.DirectoryProfile;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface DirectoryProfileRepository extends CrudRepository<DirectoryProfile, Long> {
}
