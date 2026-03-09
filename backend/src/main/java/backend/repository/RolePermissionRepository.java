package backend.repository;

import backend.model.RolePermission;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface RolePermissionRepository extends CrudRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
}
