package backend.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.ServerAuthentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller("/saml")
@Secured(SecurityRule.IS_ANONYMOUS)
public class SamlController {

  private final SAML2Client client;
  private final AccessRefreshTokenGenerator tokenGenerator;
  private static final Logger LOG = LoggerFactory.getLogger(SamlController.class);

  public SamlController(
      @Value("${micronaut.security.saml.idp-metadata-url}") String idpMetadata,
      @Value("${micronaut.security.saml.sp-entity-id}") String spEntityId,
      @Value("${micronaut.security.saml.callback-url}") String callbackUrl,
      AccessRefreshTokenGenerator tokenGenerator) {

    LOG.debug("SamlController init with IDP: {}, SP: {}, Callback: {}", idpMetadata, spEntityId, callbackUrl);
    SAML2Configuration config = new SAML2Configuration();
    config.setIdentityProviderMetadataResourceUrl(idpMetadata);
    config.setServiceProviderEntityId(spEntityId);
    config.setCallbackUrl(callbackUrl);
    config.setKeystoreResourceUrl("file:/tmp/saml_keystore.jks");
    config.setKeystorePassword("pleaseChangeThis");
    config.setPrivateKeyPassword("pleaseChangeThis");

    this.client = new SAML2Client(config);
    this.client.init();
    this.tokenGenerator = tokenGenerator;
  }

  @Get("/login")
  public HttpResponse<?> login(HttpRequest<?> request) {
    WebContext webContext = new MicronautWebContext(request);
    Optional<WithLocationAction> action = client.getRedirectionAction(new CallContext(webContext, null))
        .filter(a -> a instanceof WithLocationAction)
        .map(a -> (WithLocationAction) a);

    return action.map(withLocationAction -> HttpResponse.redirect(URI.create(withLocationAction.getLocation())))
        .orElseGet(() -> HttpResponse.serverError("SAML redirect failed"));
  }

  @Post(value = "/acs", consumes = MediaType.APPLICATION_FORM_URLENCODED)
  public HttpResponse<?> acs(HttpRequest<?> request) {
    WebContext webContext = new MicronautWebContext(request);
    CallContext ctx = new CallContext(webContext, null);

    return client.getCredentials(ctx)
        .map(credentials -> {
          return client.getUserProfile(ctx, credentials)
              .map(profile -> {
                String username = profile.getId();
                Map<String, Object> attributes = new HashMap<>(profile.getAttributes());

                Authentication auth = new ServerAuthentication(username,
                    Collections.singletonList("USER"), attributes);

                return tokenGenerator.generate(auth)
                    .map(accessRefreshToken -> HttpResponse.redirect(URI.create(
                        "http://localhost:3000/?token=" + accessRefreshToken.getAccessToken())))
                    .orElseGet(() -> HttpResponse.serverError("Token generation failed"));
              })
              .orElseGet(() -> HttpResponse.unauthorized());
        })
        .orElseGet(() -> HttpResponse.unauthorized());
  }

  @Get("/metadata")
  public String metadata() {
    return client.getServiceProviderMetadataResolver().getMetadata();
  }
}
