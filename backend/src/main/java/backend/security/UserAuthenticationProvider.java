package backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backend.model.access.RolePermission;
import backend.model.access.User;
import backend.model.access.UserRole;
import backend.repository.access.UserRepository;
import backend.repository.DirectoryConfigRepository;
import backend.repository.access.UserRoleRepository;
import backend.repository.access.RolePermissionRepository;
import backend.model.DirectoryConfig;
import backend.repository.access.RoleRepository;
import backend.repository.access.PermissionRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.ReactiveAuthenticationProvider;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Optional;

@Singleton
public class UserAuthenticationProvider implements ReactiveAuthenticationProvider<HttpRequest<?>, Object, Object> {

  private static final Logger LOG = LoggerFactory.getLogger(UserAuthenticationProvider.class);

  private final UserRepository userRepository;
  private final DirectoryConfigRepository directoryRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;
  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;

  public UserAuthenticationProvider(UserRepository userRepository,
      DirectoryConfigRepository directoryRepository,
      UserRoleRepository userRoleRepository,
      RolePermissionRepository rolePermissionRepository,
      PermissionRepository permissionRepository,
      RoleRepository roleRepository) {
    LOG.trace("UserAuthenticationProvider initialized");
    this.userRepository = userRepository;
    this.directoryRepository = directoryRepository;
    this.userRoleRepository = userRoleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
    this.permissionRepository = permissionRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<?> request,
      AuthenticationRequest<Object, Object> authRequest) {

    String username = authRequest.getIdentity().toString();
    Object secret = authRequest.getSecret();
    String password = secret instanceof char[] ? new String((char[]) secret) : secret.toString();

    LOG.trace("Auth attempt for user: {}", username);

    return Mono.fromCallable(() -> {
      Optional<User> user = userRepository.findByUsername(username);
      if (user.isPresent()) {
        LOG.debug("Found user in DB: {}. Password match: {}", username, user.get().password().equals(password));
        if (user.get().password().equals(password)) {
          List<String> permissions = new ArrayList<>();
          List<UserRole> userRoles = userRoleRepository.findByUserId(user.get().id());
          for (UserRole ur : userRoles) {
            roleRepository.findById(ur.roleId()).ifPresent(r -> permissions.add(r.name()));
            List<RolePermission> rolePerms = rolePermissionRepository.findByRoleId(ur.roleId());
            for (RolePermission rp : rolePerms) {
              permissionRepository.findById(rp.permissionId()).ifPresent(p -> permissions.add(p.name()));
            }
          }
          return AuthenticationResponse.success(username, permissions);
        }
      } else {
        LOG.debug("User not found in local DB: {}", username);
      }

      // Try AD servers
      Iterable<DirectoryConfig> profiles = directoryRepository.findAll();
      for (DirectoryConfig profile : profiles) {
        if (tryAdAuth(username, password, profile)) {
          LOG.info("AD auth success for user: {}", username);
          return AuthenticationResponse.success(username, Collections.singletonList("USER"));
        }
      }

      LOG.warn("Auth failed for user: {}", username);
      return AuthenticationResponse.failure("Invalid credentials");
    });
  }

  private boolean tryAdAuth(String username, String password, DirectoryConfig profile) {
    if (password == null || password.isEmpty())
      return false;

    Hashtable<String, String> env = new Hashtable<>();
    String ldapUrl = "ldap://" + profile.adHost() + ":" + profile.adPort();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapUrl);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, username);
    env.put(Context.SECURITY_CREDENTIALS, password);

    try {
      DirContext ctx = new InitialDirContext(env);
      ctx.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
