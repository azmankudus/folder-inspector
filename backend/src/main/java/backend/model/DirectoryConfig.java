package backend.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Configuration for connecting to an Active Directory (LDAP) system for user/group resolution.
 */
@Serdeable
@MappedEntity("tb_config_directory")
public record DirectoryConfig(
        @Id @GeneratedValue Long id,
        String adHost,
        Integer adPort,
        @Nullable String adBackupHost,
        @Nullable Integer adBackupPort,
        String adUsername,
        String adPassword) {
}
