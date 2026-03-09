package backend.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdClaims;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import java.util.Collections;

@Singleton
public class MicrosoftEntraUserDetailsMapper implements OpenIdAuthenticationMapper {

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(String providerName,
                                                                         OpenIdTokenResponse tokenResponse,
                                                                         OpenIdClaims claims,
                                                                         @Nullable State state) {
        String username = claims.getPreferredUsername() != null ? claims.getPreferredUsername() : claims.getSubject();
        
        // Map roles based on AD groups or roles if present, otherwise default to USER
        // For development, we'll give them the 'USER' role.
        return Publishers.just(AuthenticationResponse.success(username, Collections.singletonList("USER"), claims.getClaims()));
    }
}
