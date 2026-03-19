package backend.repository;

import java.util.Collection;

import backend.model.ItemAcl;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ItemAclRepository extends CrudRepository<ItemAcl, Long> {
    Iterable<ItemAcl> findByItemId(Long itemId);

    Iterable<ItemAcl> findByItemIdIn(Collection<Long> itemIds);
}
