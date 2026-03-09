package backend.repository;

import backend.model.FileAcl;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface FileAclRepository extends CrudRepository<FileAcl, Long> {
    Iterable<FileAcl> findByFileNodeId(Long fileNodeId);
    Iterable<FileAcl> findByFileNodeIdIn(java.util.Collection<Long> fileNodeIds);
}
