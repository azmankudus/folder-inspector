package dev.ayam.folderinspector.database.jpa.entity;

import dev.ayam.folderinspector.core.model.ItemType;
import dev.ayam.folderinspector.core.model.PermissionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * JPA Entity representing a file system item as a record.
 */
@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_parent", columnList = "parent"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_snapshot", columnList = "snapshotName")
})
public record ItemEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,

        String name,
        String parent,

        @Enumerated(EnumType.STRING) ItemType type,

        long size,
        Instant lastModified,
        Instant created,

        @ManyToOne(cascade = CascadeType.ALL) @JoinColumn(name = "owner_id") PrincipalEntity owner,

        @ManyToOne(cascade = CascadeType.ALL) @JoinColumn(name = "group_id") PrincipalEntity group,

        @ElementCollection(targetClass = PermissionType.class) @CollectionTable(name = "item_permissions", joinColumns = @JoinColumn(name = "item_id")) @Enumerated(EnumType.STRING) Set<PermissionType> permissions,

        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) @JoinColumn(name = "item_id") List<AclItemEntity> acls,

        String snapshotName) {
    public ItemEntity() {
        this(null, null, null, null, 0, null, null, null, null, null, null, null);
    }
}
