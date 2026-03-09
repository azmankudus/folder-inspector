package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Configuration for connecting to an Active Directory (LDAP) system for user/group resolution.
 */
@Serdeable
@MappedEntity
public record DirectoryProfile(
    @Id @GeneratedValue Long id,
    String adHost,
    Integer adPort,
    String adBackupHost,
    Integer adBackupPort,
    String adUsername,
    String adPassword
) {}
