package backend.repository;

import backend.model.UserRole;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
}
