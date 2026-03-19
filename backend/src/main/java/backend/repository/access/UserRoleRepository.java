package backend.repository.access;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

import backend.model.access.UserRole;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
}
