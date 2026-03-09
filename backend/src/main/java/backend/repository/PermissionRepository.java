package backend.repository;

import backend.model.Permission;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface PermissionRepository extends CrudRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
