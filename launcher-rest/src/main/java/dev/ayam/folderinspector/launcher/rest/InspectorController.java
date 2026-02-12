package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.snapshot.SnapshotService;
import dev.ayam.folderinspector.core.snapshot.SnapshotService.SnapshotInfo;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller("/api/v1")
@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
public class InspectorController {

  private final ScannerContext scannerContext;
  private final SnapshotService snapshotService;

  public InspectorController(ScannerContext scannerContext) {
    this.scannerContext = scannerContext;
    this.snapshotService = new SnapshotService();
  }

  /**
   * Get current authenticated user info.
   */
  @Get("/me")
  public Map<String, Object> me(Authentication authentication) {
    return Map.of(
        "username", authentication.getName(),
        "roles", authentication.getRoles(),
        "attributes", authentication.getAttributes()
    );
  }

  /**
   * Scan the configured root path.
   */
  @Get("/scan")
  @Secured({"ROLE_ADMIN", "ROLE_AUDITOR"})
  public List<Item> scan() throws IOException {
    try (Stream<Item> stream = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      return stream.collect(Collectors.toList());
    }
  }

  /**
   * List all available snapshots.
   */
  @Get("/snapshots")
  public List<SnapshotInfo> listSnapshots() throws IOException, SQLException {
    return snapshotService.list();
  }

  /**
   * Save current scan to a named snapshot.
   */
  @Post("/snapshots/{name}")
  @Secured({"ROLE_ADMIN"})
  public HttpResponse<String> saveSnapshot(
      @PathVariable @NotBlank String name,
      Authentication authentication) {
    try (Stream<Item> items = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      snapshotService.save(items, name);
      return HttpResponse.created("Snapshot saved: " + name + " by " + authentication.getName());
    } catch (Exception e) {
      return HttpResponse.serverError("Failed to save snapshot: " + e.getMessage());
    }
  }

  /**
   * Load items from a snapshot with optional query filter.
   */
  @Get("/snapshots/{name}")
  public List<Item> loadSnapshot(
      @PathVariable @NotBlank String name,
      @QueryValue(defaultValue = "") String query,
      @QueryValue(defaultValue = "-1") @Min(-1) int limit) throws SQLException {

    String whereClause = query.isBlank() ? null : query;
    try (Stream<Item> stream = snapshotService.load(name, whereClause)) {
      Stream<Item> limited = limit > 0 ? stream.limit(limit) : stream;
      return limited.collect(Collectors.toList());
    }
  }

  /**
   * Delete a snapshot.
   */
  @Delete("/snapshots/{name}")
  @Secured({"ROLE_ADMIN"})
  public HttpResponse<String> deleteSnapshot(@PathVariable @NotBlank String name) {
    try {
      snapshotService.delete(name);
      return HttpResponse.ok("Snapshot deleted: " + name);
    } catch (IOException e) {
      return HttpResponse.serverError("Failed to delete snapshot: " + e.getMessage());
    }
  }
}
