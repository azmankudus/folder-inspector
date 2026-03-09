package backend.repository;

import backend.model.FileNode;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

/**
 * Repository for managing {@link FileNode} entities in the database.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface FileNodeRepository extends CrudRepository<FileNode, Long> {
    @io.micronaut.data.annotation.Query(value = "SELECT * FROM file_node WHERE scan_history_id = :scanHistoryId",
            countQuery = "SELECT count(*) FROM file_node WHERE scan_history_id = :scanHistoryId")
    Page<FileNode> findByScanHistoryId(Long scanHistoryId, Pageable pageable);
    
    java.util.stream.Stream<FileNode> findByScanHistoryId(Long scanHistoryId);

    @io.micronaut.data.annotation.Query(value = "SELECT DISTINCT fn.* FROM file_node fn " +
            "LEFT JOIN file_acl acl ON acl.file_node_id = fn.id AND acl.scan_profile_id = fn.scan_profile_id " +
            "WHERE fn.scan_history_id = :scanHistoryId " +
            "AND (fn.owner_name = :username OR acl.user_or_group = :username)",
            countQuery = "SELECT count(DISTINCT fn.id) FROM file_node fn " +
            "LEFT JOIN file_acl acl ON acl.file_node_id = fn.id AND acl.scan_profile_id = fn.scan_profile_id " +
            "WHERE fn.scan_history_id = :scanHistoryId " +
            "AND (fn.owner_name = :username OR acl.user_or_group = :username)")
    Page<FileNode> findByScanHistoryIdWithPermissionOptions(Long scanHistoryId, String username, Pageable pageable);

    @io.micronaut.data.annotation.Query(value = "SELECT DISTINCT fn.* FROM file_node fn " +
            "LEFT JOIN file_acl acl ON acl.file_node_id = fn.id " +
            "WHERE fn.scan_history_id = :scanHistoryId " +
            "AND (fn.owner_name = :username OR acl.user_or_group = :username)")
    java.util.stream.Stream<FileNode> findByScanHistoryIdWithPermissionOptions(Long scanHistoryId, String username);
}
