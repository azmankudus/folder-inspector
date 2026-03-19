package backend.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Configuration for connecting to a remote SMB storage system.
 */
@Serdeable
@MappedEntity("tb_config_server")
public record ServerConfig(
    @Id @GeneratedValue Long id,
    @Nullable String protocol,
    @Nullable String host,
    @Nullable Integer port,
    @Nullable String username,
    @Nullable String password) {
}
