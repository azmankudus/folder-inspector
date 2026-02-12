package dev.ayam.folderinspector.launcher.rest.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.reader.TokenReader;
import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Reads API Key from X-API-Key header.
 */
@Singleton
public class ApiKeyTokenReader implements TokenReader<HttpRequest<?>> {

  private static final String API_KEY_HEADER = "X-API-Key";
  private static final String API_KEY_PREFIX = "apikey:";

  @Override
  public Optional<String> findToken(HttpRequest<?> request) {
    String apiKey = request.getHeaders().get(API_KEY_HEADER);
    if (apiKey != null && !apiKey.isBlank()) {
      // Prefix to distinguish from JWT tokens
      return Optional.of(API_KEY_PREFIX + apiKey);
    }
    return Optional.empty();
  }
}
