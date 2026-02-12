package dev.ayam.folderinspector.database.jpa.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a security principal (user or group) as a record.
 */
@Entity
@Table(name = "principals")
public record PrincipalEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,
    String sid,
    String name,
    String type) {
  public PrincipalEntity() {
    this(null, null, null, null);
  }
}
