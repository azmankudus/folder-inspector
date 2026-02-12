package dev.ayam.folderinspector.launcher.rest.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Authentication provider for JWT token generation.
 * Validates username/password and returns user details with roles.
 */
@Singleton
public class JwtAuthenticationProvider<B> implements HttpRequestAuthenticationProvider<B> {

  // Demo users: username -> {password, role}
  private static final Map<String, UserCredentials> USERS = Map.of(
      "admin", new UserCredentials("admin123", "ROLE_ADMIN"),
      "auditor", new UserCredentials("auditor123", "ROLE_AUDITOR"),
      "user", new UserCredentials("user123", "ROLE_VIEWER")
  );

  @Override
  public AuthenticationResponse authenticate(
      @Nullable HttpRequest<B> httpRequest,
      AuthenticationRequest<String, String> authenticationRequest) {
    
    String username = authenticationRequest.getIdentity();
    String password = authenticationRequest.getSecret();

    UserCredentials userCreds = USERS.get(username);
    
    if (userCreds != null && userCreds.password().equals(password)) {
      return AuthenticationResponse.success(
          username,
          List.of(userCreds.role()),
          Map.of("role", userCreds.role())
      );
    }
    
    return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
  }

  private record UserCredentials(String password, String role) {}
}
