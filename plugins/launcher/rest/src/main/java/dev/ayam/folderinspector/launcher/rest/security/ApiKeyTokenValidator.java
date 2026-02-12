package dev.ayam.folderinspector.launcher.rest.security;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.validator.TokenValidator;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates API Key tokens (prefixed with "apikey:").
 */
@Singleton
public class ApiKeyTokenValidator implements TokenValidator<String> {

  private static final String API_KEY_PREFIX = "apikey:";

  // In production, these would be stored in database/config
  private static final Map<String, ApiKeyInfo> API_KEYS = new ConcurrentHashMap<>(Map.of(
      "sk_admin_demo_key", new ApiKeyInfo("api-admin", "ROLE_ADMIN"),
      "sk_auditor_demo_key", new ApiKeyInfo("api-auditor", "ROLE_AUDITOR"),
      "sk_viewer_demo_key", new ApiKeyInfo("api-viewer", "ROLE_VIEWER")
  ));

  @Override
  public Publisher<Authentication> validateToken(String token, String request) {
    if (token != null && token.startsWith(API_KEY_PREFIX)) {
      String apiKey = token.substring(API_KEY_PREFIX.length());
      ApiKeyInfo keyInfo = API_KEYS.get(apiKey);
      
      if (keyInfo != null) {
        return Publishers.just(Authentication.build(
            keyInfo.username(),
            List.of(keyInfo.role()),
            Map.of("authMethod", "api-key", "role", keyInfo.role())
        ));
      }
    }
    return Publishers.empty();
  }

  private record ApiKeyInfo(String username, String role) {}
}
