package backend.repository;

import java.util.stream.Stream;

import backend.model.Item;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

/**
 * Repository for managing {@link Item} entities in the database.
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ItemRepository extends CrudRepository<Item, Long> {
  Page<Item> findByJobId(Long jobId, Pageable pageable);

  Stream<Item> findByJobId(Long jobId);

  @Query(value = """
      SELECT DISTINCT i.*
        FROM tb_item i
        LEFT JOIN tb_item_acl a ON a.item_id = i.id
       WHERE i.job_id = :jobId
         AND (i.owner_name = :username OR a.principal = :username)
      """, countQuery = """
      SELECT count(DISTINCT i.id)
        FROM tb_item i
        LEFT JOIN tb_item_acl a ON a.item_id = i.id
       WHERE i.job_id = :jobId
         AND (i.owner_name = :username OR a.principal = :username)
      """)
  Page<Item> findByJobIdWithPermissionOptions(Long jobId, String username, Pageable pageable);

  @Query("""
      SELECT DISTINCT i.*
        FROM tb_item i
        LEFT JOIN tb_item_acl a ON a.item_id = i.id
        WHERE i.job_id = :jobId
          AND (i.owner_name = :username OR a.principal = :username)
      """)
  Stream<Item> findByJobIdWithPermissionOptions(Long jobId, String username);
}
