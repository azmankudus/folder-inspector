package dev.ayam.folderinspector.launcher.rest.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for authentication.
 */
@Serdeable
public record AuthRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {}
