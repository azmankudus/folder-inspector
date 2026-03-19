package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents an Access Control Entry (ACE) associated with a file node.
 */
@Serdeable
@MappedEntity("tb_item_acl")
public record ItemAcl(
        @Id @GeneratedValue Long id,
        Long itemId,
        String principal,
        String inheritanceType,
        Boolean canView,
        Boolean canAdd,
        Boolean canEdit,
        Boolean canRemove) {
}
