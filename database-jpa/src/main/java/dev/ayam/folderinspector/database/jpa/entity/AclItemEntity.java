package dev.ayam.folderinspector.database.jpa.entity;

import dev.ayam.folderinspector.core.model.InheritFlag;
import dev.ayam.folderinspector.core.model.PermissionType;
import jakarta.persistence.*;
import java.util.Set;

/**
 * JPA Entity representing an ACL entry as a record.
 */
@Entity
@Table(name = "acl_items")
public record AclItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,

    String name,

    @ElementCollection(targetClass = PermissionType.class) @CollectionTable(name = "acl_item_permissions", joinColumns = @JoinColumn(name = "acl_item_id")) @Enumerated(EnumType.STRING) Set<PermissionType> permissions,

    @ElementCollection(targetClass = InheritFlag.class) @CollectionTable(name = "acl_item_inherit_flags", joinColumns = @JoinColumn(name = "acl_item_id")) @Enumerated(EnumType.STRING) Set<InheritFlag> inheritFlags) {
  public AclItemEntity() {
    this(null, null, null, null);
  }
}
