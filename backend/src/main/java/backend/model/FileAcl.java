package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents an Access Control Entry (ACE) associated with a file node.
 */
@Serdeable
@MappedEntity
public record FileAcl(
    @Id @GeneratedValue Long id,
    Long fileNodeId,
    String userOrGroup,
    String inheritanceType,
    Boolean canView,
    Boolean canAdd,
    Boolean canEdit,
    Boolean canRemove,
    Long scanProfileId
) {}
