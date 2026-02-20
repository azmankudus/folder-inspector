package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.plugin.Database.SnapshotInfo;
import dev.ayam.folderinspector.core.plugin.Scheduler;
import dev.ayam.folderinspector.launcher.rest.dto.ScheduleRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

/**
 * REST API Controller for folder inspection and snapshot management.
 */
@Controller("/api/v1")
@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiController {

  private final InspectorService inspectorService;

  public ApiController(InspectorService inspectorService) {
    this.inspectorService = inspectorService;
  }

  @Get("/scan")
  @Secured({ "ROLE_ADMIN", "ROLE_AUDITOR" })
  public List<Item> scan() throws IOException {
    return inspectorService.scan();
  }

  @Get("/snapshots")
  public List<SnapshotInfo> listSnapshots() throws Exception {
    return inspectorService.listSnapshots();
  }

  @Post("/snapshots/{name}")
  @Secured({ "ROLE_ADMIN" })
  public HttpResponse<String> saveSnapshot(@PathVariable @NotBlank String name) throws Exception {
    inspectorService.saveSnapshot(name);
    return HttpResponse.created("Snapshot saved: " + name);
  }

  @Get("/snapshots/{name}")
  public List<Item> loadSnapshot(
      @PathVariable @NotBlank String name,
      @QueryValue(defaultValue = "") String query,
      @QueryValue(defaultValue = "-1") @Min(-1) int limit) throws Exception {
    return inspectorService.loadSnapshot(name, query, limit);
  }

  @Delete("/snapshots/{name}")
  @Secured({ "ROLE_ADMIN" })
  public HttpResponse<String> deleteSnapshot(@PathVariable @NotBlank String name) throws Exception {
    inspectorService.deleteSnapshot(name);
    return HttpResponse.ok("Snapshot deleted: " + name);
  }

  @Get("/schedules")
  @Secured({ "ROLE_ADMIN", "ROLE_AUDITOR" })
  public List<Scheduler.ScheduleInfo> listSchedules() throws Exception {
    return inspectorService.listSchedules();
  }

  @Post("/schedules")
  @Secured({ "ROLE_ADMIN" })
  public HttpResponse<String> scheduleScan(@Body @Valid ScheduleRequest request) throws Exception {
    inspectorService.scheduleScan(
        request.name(),
        request.cron(),
        request.path(),
        request.snapshotName());
    return HttpResponse.created("Scan scheduled: " + request.name());
  }

  @Delete("/schedules/{name}")
  @Secured({ "ROLE_ADMIN" })
  public HttpResponse<String> deleteSchedule(@PathVariable String name) throws Exception {
    inspectorService.deleteSchedule(name);
    return HttpResponse.ok("Schedule deleted: " + name);
  }
}
