package dev.ayam.folderinspector.launcher.rest.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Key authentication provider.
 * Validates X-API-Key header for programmatic access.
 */
@Singleton
@Requires(property = "folder-inspector.api-keys.enabled", value = "true", defaultValue = "true")
public class ApiKeyAuthenticationProvider<B> implements HttpRequestAuthenticationProvider<B> {

  private static final String API_KEY_HEADER = "X-API-Key";

  // In production, these would be stored in database/config
  private static final Map<String, ApiKeyInfo> API_KEYS = new ConcurrentHashMap<>(Map.of(
      "sk_admin_demo_key", new ApiKeyInfo("api-admin", "ROLE_ADMIN"),
      "sk_auditor_demo_key", new ApiKeyInfo("api-auditor", "ROLE_AUDITOR"),
      "sk_viewer_demo_key", new ApiKeyInfo("api-viewer", "ROLE_VIEWER")
  ));

  @Override
  public AuthenticationResponse authenticate(
      @Nullable HttpRequest<B> httpRequest,
      AuthenticationRequest<String, String> authenticationRequest) {
    
    if (httpRequest == null) {
      return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
    }

    String apiKey = httpRequest.getHeaders().get(API_KEY_HEADER);
    
    if (apiKey != null) {
      ApiKeyInfo keyInfo = API_KEYS.get(apiKey);
      if (keyInfo != null) {
        return AuthenticationResponse.success(
            keyInfo.username(),
            List.of(keyInfo.role()),
            Map.of("authMethod", "api-key", "role", keyInfo.role())
        );
      }
    }
    
    // Fall through to other providers
    return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
  }

  private record ApiKeyInfo(String username, String role) {}
}
