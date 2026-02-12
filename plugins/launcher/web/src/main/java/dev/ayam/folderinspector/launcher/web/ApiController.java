package dev.ayam.folderinspector.launcher.web;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.snapshot.SnapshotService;
import dev.ayam.folderinspector.core.snapshot.SnapshotService.SnapshotInfo;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller("/api/v1")
public class ApiController {

  private final ScannerContext scannerContext;
  private final SnapshotService snapshotService;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  @Value("${folder-inspector.api-url:}")
  private String apiUrl;

  // Simple in-memory users for demo (username -> {password, role})
  private static final Map<String, Map<String, String>> USERS = Map.of(
      "admin", Map.of("password", "admin123", "role", "admin"),
      "auditor", Map.of("password", "auditor123", "role", "auditor"),
      "user", Map.of("password", "user123", "role", "viewer"));

  public ApiController(ScannerContext scannerContext, ObjectMapper objectMapper) {
    this.scannerContext = scannerContext;
    this.snapshotService = new SnapshotService();
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
  }

  private boolean useProxy() {
    return apiUrl != null && !apiUrl.isBlank();
  }

  /**
   * Login endpoint.
   */
  @Post("/auth/login")
  public HttpResponse<Map<String, Object>> login(@Body Map<String, String> credentials, Session session) {
    String username = credentials.get("username");
    String password = credentials.get("password");

    if (username != null && USERS.containsKey(username)) {
      Map<String, String> userInfo = USERS.get(username);
      if (userInfo.get("password").equals(password)) {
        session.put("user", username);
        session.put("role", userInfo.get("role"));
        return HttpResponse.ok(Map.of("success", true, "username", username, "role", userInfo.get("role")));
      }
    }
    return HttpResponse.unauthorized().body(Map.of("success", false, "error", "Invalid credentials"));
  }

  /**
   * Logout endpoint.
   */
  @Post("/auth/logout")
  public HttpResponse<Map<String, Object>> logout(Session session) {
    session.remove("user");
    return HttpResponse.ok(Map.of("success", true));
  }

  /**
   * Get current user.
   */
  @Get("/auth/me")
  public HttpResponse<Map<String, Object>> me(Session session) {
    Object user = session.get("user").orElse(null);
    Object role = session.get("role").orElse("viewer");
    if (user != null) {
      return HttpResponse.ok(Map.of("authenticated", true, "username", user, "role", role));
    }
    return HttpResponse.ok(Map.of("authenticated", false));
  }

  /**
   * Scan the configured root path.
   * Proxies to REST launcher if apiUrl is configured.
   */
  @Get("/scan")
  public List<Item> scan(Session session) throws IOException, InterruptedException {
    requireAuth(session);
    
    if (useProxy()) {
      // Proxy to REST launcher
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl + "/api/v1/scan"))
          .GET()
          .build();
      var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
      return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
    }
    
    // Local scan
    try (Stream<Item> stream = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      return stream.collect(Collectors.toList());
    }
  }

  /**
   * List all available snapshots.
   * Proxies to REST launcher if apiUrl is configured.
   */
  @Get("/snapshots")
  public List<SnapshotInfo> listSnapshots(Session session) throws IOException, SQLException, InterruptedException {
    requireAuth(session);
    
    if (useProxy()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl + "/api/v1/snapshots"))
          .GET()
          .build();
      var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
      return objectMapper.readValue(response.body(), new TypeReference<List<SnapshotInfo>>() {});
    }
    
    return snapshotService.list();
  }

  /**
   * Save current scan to a named snapshot.
   * Proxies to REST launcher if apiUrl is configured.
   */
  @Post("/snapshots/{name}")
  public HttpResponse<String> saveSnapshot(@PathVariable String name, Session session) throws IOException, InterruptedException {
    requireAuth(session);
    
    if (useProxy()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl + "/api/v1/snapshots/" + name))
          .POST(HttpRequest.BodyPublishers.noBody())
          .build();
      var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
      return response.statusCode() < 400 
          ? HttpResponse.created(response.body())
          : HttpResponse.serverError(response.body());
    }
    
    try (Stream<Item> items = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      snapshotService.save(items, name);
      return HttpResponse.created("Snapshot saved: " + name);
    } catch (Exception e) {
      return HttpResponse.serverError("Failed to save snapshot: " + e.getMessage());
    }
  }

  /**
   * Load items from a snapshot with optional query filter.
   * Proxies to REST launcher if apiUrl is configured.
   */
  @Get("/snapshots/{name}")
  public List<Item> loadSnapshot(
      @PathVariable String name,
      @QueryValue(defaultValue = "") String query,
      @QueryValue(defaultValue = "-1") int limit,
      Session session) throws SQLException, IOException, InterruptedException {
    requireAuth(session);
    
    if (useProxy()) {
      StringBuilder uri = new StringBuilder(apiUrl + "/api/v1/snapshots/" + name);
      String sep = "?";
      if (!query.isBlank()) {
        uri.append(sep).append("query=").append(java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
        sep = "&";
      }
      if (limit > 0) {
        uri.append(sep).append("limit=").append(limit);
      }
      
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri.toString()))
          .GET()
          .build();
      var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
      return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
    }
    
    String whereClause = query.isBlank() ? null : query;
    try (Stream<Item> stream = snapshotService.load(name, whereClause)) {
      Stream<Item> limited = limit > 0 ? stream.limit(limit) : stream;
      return limited.collect(Collectors.toList());
    }
  }

  /**
   * Delete a snapshot.
   * Proxies to REST launcher if apiUrl is configured.
   */
  @Delete("/snapshots/{name}")
  public HttpResponse<String> deleteSnapshot(@PathVariable String name, Session session) throws IOException, InterruptedException {
    requireAuth(session);
    
    if (useProxy()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl + "/api/v1/snapshots/" + name))
          .DELETE()
          .build();
      var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
      return response.statusCode() < 400 
          ? HttpResponse.ok(response.body())
          : HttpResponse.serverError(response.body());
    }
    
    try {
      snapshotService.delete(name);
      return HttpResponse.ok("Snapshot deleted: " + name);
    } catch (IOException e) {
      return HttpResponse.serverError("Failed to delete snapshot: " + e.getMessage());
    }
  }

  private void requireAuth(Session session) {
    if (session.get("user").isEmpty()) {
      throw new io.micronaut.http.exceptions.HttpStatusException(
          io.micronaut.http.HttpStatus.UNAUTHORIZED, "Authentication required");
    }
  }
}
