package dev.ayam.folderinspector.launcher.rest.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;

/**
 * Request DTO for scanning a path with validation.
 */
@Serdeable
@Introspected
public record ScanRequest(
    @NotBlank(message = "Path is required")
    String path,

    @Min(value = 1, message = "Depth must be at least 1")
    @Max(value = 100, message = "Depth cannot exceed 100")
    Integer depth,

    String format
) {
  public ScanRequest {
    // Set defaults
    if (depth == null) depth = 10;
    if (format == null) format = "json";
  }
}
