package backend.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Configuration for connecting to a remote SMB storage system.
 */
@Serdeable
@MappedEntity
public record ConnectionProfile(
    @Id @GeneratedValue Long id,
    String protocol,
    String host,
    Integer port,
    String username,
    String password
) {}
