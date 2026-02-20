package dev.ayam.folderinspector.launcher.rest.dto;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;

@Introspected
public record ScheduleRequest(
    @NotBlank String name,
    @NotBlank String cron,
    @NotBlank String path,
    @NotBlank String snapshotName) {
}
