package dev.ayam.folderinspector.database.jpa;

import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.Principal;
import dev.ayam.folderinspector.core.model.PrincipalType;
import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.database.jpa.entity.AclItemEntity;
import dev.ayam.folderinspector.database.jpa.entity.ItemEntity;
import dev.ayam.folderinspector.database.jpa.entity.PrincipalEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA implementation of the {@link Database} plugin using Hibernate.
 */
public class JpaDatabase implements Database {

  private static final Logger logger = LoggerFactory.getLogger(JpaDatabase.class);
  private final EntityManagerFactory emf;

  public JpaDatabase() {
    this.emf = Persistence.createEntityManagerFactory("folder-inspector-jpa");
  }

  @Override
  public void save(Stream<Item> items, String name) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      // Remove previously saved items for this snapshot name if any
      em.createQuery("DELETE FROM ItemEntity i WHERE i.snapshotName = :name")
          .setParameter("name", name)
          .executeUpdate();

      items.forEach(item -> {
        ItemEntity entity = toEntity(item, name);
        em.persist(entity);
      });
      tx.commit();
      logger.info("Saved snapshot '{}' using JPA", name);
    } catch (Exception e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public Stream<Item> load(String name, String query) {
    EntityManager em = emf.createEntityManager();
    String jpql = "SELECT i FROM ItemEntity i WHERE i.snapshotName = :name";
    if (query != null && !query.isBlank()) {
      // Basic translation: assuming query is simple field comparisons
      // This is a simplification for the demonstration.
      jpql += " AND " + query;
    }

    TypedQuery<ItemEntity> typedQuery = em.createQuery(jpql, ItemEntity.class);
    typedQuery.setParameter("name", name);

    return typedQuery.getResultStream()
        .map(this::toModel)
        .onClose(em::close);
  }

  @Override
  public List<SnapshotInfo> list() {
    EntityManager em = emf.createEntityManager();
    try {
      List<Object[]> results = em.createQuery(
          "SELECT i.snapshotName, COUNT(i), SUM(i.size), MAX(i.created) FROM ItemEntity i GROUP BY i.snapshotName",
          Object[].class).getResultList();

      return results.stream()
          .map(r -> new SnapshotInfo(
              (String) r[0],
              (Long) r[1],
              r[2] != null ? (Long) r[2] : 0L,
              (Instant) r[3]))
          .collect(Collectors.toList());
    } finally {
      em.close();
    }
  }

  @Override
  public void delete(String name) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      em.createQuery("DELETE FROM ItemEntity i WHERE i.snapshotName = :name")
          .setParameter("name", name)
          .executeUpdate();
      tx.commit();
    } catch (Exception e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public String getName() {
    return "jpa";
  }

  private ItemEntity toEntity(Item item, String snapshotName) {
    return new ItemEntity(
        null,
        item.name(),
        item.parent(),
        item.type(),
        item.size(),
        item.lastModified(),
        item.created(),
        toPrincipalEntity(item.owner()),
        toPrincipalEntity(item.group()),
        item.permissions(),
        toAclEntities(item.acls()),
        snapshotName);
  }

  private Item toModel(ItemEntity entity) {
    return new Item(
        entity.id(),
        entity.name(),
        entity.parent(),
        entity.type(),
        entity.size(),
        entity.lastModified(),
        entity.created(),
        toPrincipal(entity.owner()),
        toPrincipal(entity.group()),
        entity.permissions(),
        toAclItems(entity.acls()));
  }

  private PrincipalEntity toPrincipalEntity(Principal principal) {
    if (principal == null)
      return null;
    return new PrincipalEntity(null, principal.sid(), principal.name(),
        principal.type() != null ? principal.type().name() : null);
  }

  private Principal toPrincipal(PrincipalEntity entity) {
    if (entity == null)
      return null;
    return new Principal(entity.id(), entity.sid(), entity.name(), parsePrincipalType(entity.type()));
  }

  private List<AclItemEntity> toAclEntities(List<AclItem> acls) {
    if (acls == null)
      return new ArrayList<>();
    return acls.stream().map(acl -> new AclItemEntity(
        null,
        acl.name(),
        acl.permissions(),
        acl.inheritFlags()))
        .collect(Collectors.toList());
  }

  private List<AclItem> toAclItems(List<AclItemEntity> entities) {
    if (entities == null)
      return Collections.emptyList();
    return entities.stream().map(entity -> new AclItem(
        entity.name(),
        entity.permissions(),
        entity.inheritFlags())).collect(Collectors.toList());
  }

  private PrincipalType parsePrincipalType(String str) {
    if (str == null)
      return null;
    try {
      return PrincipalType.valueOf(str);
    } catch (Exception e) {
      return null;
    }
  }
}
