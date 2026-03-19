package backend.service;

import backend.model.access.Permission;
import backend.model.access.Role;
import backend.model.access.RolePermission;
import backend.model.access.User;
import backend.model.access.UserRole;
import backend.repository.access.PermissionRepository;
import backend.repository.access.RolePermissionRepository;
import backend.repository.access.RoleRepository;
import backend.repository.access.UserRepository;
import backend.repository.access.UserRoleRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataInit {
  private static final Logger LOG = LoggerFactory.getLogger(DataInit.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;

  public DataInit(UserRepository userRepository,
      RoleRepository roleRepository,
      PermissionRepository permissionRepository,
      UserRoleRepository userRoleRepository,
      RolePermissionRepository rolePermissionRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.userRoleRepository = userRoleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
  }

  @EventListener
  public void onStartup(StartupEvent event) {
    LOG.info("Initializing application data...");

    // DirectoryConfigController permissions
    Permission directoryConfigRead = getOrCreatePermission("API_DIRECTORYCONFIG_READ");
    Permission directoryConfigCreate = getOrCreatePermission("API_DIRECTORYCONFIG_CREATE");
    Permission directoryConfigUpdate = getOrCreatePermission("API_DIRECTORYCONFIG_UPDATE");
    Permission directoryConfigDelete = getOrCreatePermission("API_DIRECTORYCONFIG_DELETE");

    // ServerConfigController permissions
    Permission serverConfigRead = getOrCreatePermission("API_SERVERCONFIG_READ");
    Permission serverConfigCreate = getOrCreatePermission("API_SERVERCONFIG_CREATE");
    Permission serverConfigUpdate = getOrCreatePermission("API_SERVERCONFIG_UPDATE");
    Permission serverConfigDelete = getOrCreatePermission("API_SERVERCONFIG_DELETE");

    // ScanConfigController permissions
    Permission scanConfigRead = getOrCreatePermission("API_SCANCONFIG_READ");
    Permission scanConfigCreate = getOrCreatePermission("API_SCANCONFIG_CREATE");
    Permission scanConfigUpdate = getOrCreatePermission("API_SCANCONFIG_UPDATE");
    Permission scanConfigDelete = getOrCreatePermission("API_SCANCONFIG_DELETE");
    Permission reportViewAll = getOrCreatePermission("API_REPORT_ALL");
    Permission reportViewRestricted = getOrCreatePermission("API_REPORT_RESTRICTED");

    // JobController permissions
    Permission jobRead = getOrCreatePermission("API_JOB_READ");
    Permission jobStart = getOrCreatePermission("API_JOB_START");
    Permission jobStop = getOrCreatePermission("API_JOB_STOP");

    // ExplorerController permissions
    Permission explorerViewAll = getOrCreatePermission("API_EXPLORER_ALL");
    Permission explorerViewRestricted = getOrCreatePermission("API_EXPLORER_RESTRICTED");

    // SqlController permissions
    Permission sqlListTables = getOrCreatePermission("API_SQL_LIST_TABLES");
    Permission sqlExecute = getOrCreatePermission("API_SQL_EXECUTE");

    // UI-only permissions
    Permission viewExplorer = getOrCreatePermission("UI_EXPLORER_VIEW");
    Permission viewJob = getOrCreatePermission("UI_JOB_VIEW");
    Permission viewScanConfig = getOrCreatePermission("UI_SCANCONFIG_VIEW");
    Permission viewDirectoryConfig = getOrCreatePermission("UI_DIRECTORYCONFIG_VIEW");
    Permission viewServerConfig = getOrCreatePermission("UI_SERVERCONFIG_VIEW");
    Permission viewSql = getOrCreatePermission("UI_SQL_VIEW");
    Permission viewReport = getOrCreatePermission("UI_REPORT_VIEW");
    Permission explorerToggleAll = getOrCreatePermission("UI_EXPLORER_TOGGLE");

    // Create roles
    Role adminRole = getOrCreateRole("ADMIN");
    Role managerRole = getOrCreateRole("MANAGER");
    Role userRole = getOrCreateRole("USER");

    // ADMIN: full access to everything
    getOrCreateRolePermission(adminRole.id(), directoryConfigRead.id());
    getOrCreateRolePermission(adminRole.id(), directoryConfigCreate.id());
    getOrCreateRolePermission(adminRole.id(), directoryConfigUpdate.id());
    getOrCreateRolePermission(adminRole.id(), directoryConfigDelete.id());

    getOrCreateRolePermission(adminRole.id(), serverConfigRead.id());
    getOrCreateRolePermission(adminRole.id(), serverConfigCreate.id());
    getOrCreateRolePermission(adminRole.id(), serverConfigUpdate.id());
    getOrCreateRolePermission(adminRole.id(), serverConfigDelete.id());

    getOrCreateRolePermission(adminRole.id(), scanConfigRead.id());
    getOrCreateRolePermission(adminRole.id(), scanConfigCreate.id());
    getOrCreateRolePermission(adminRole.id(), scanConfigUpdate.id());
    getOrCreateRolePermission(adminRole.id(), scanConfigDelete.id());
    getOrCreateRolePermission(adminRole.id(), jobStart.id());
    getOrCreateRolePermission(adminRole.id(), jobStop.id());

    getOrCreateRolePermission(adminRole.id(), jobRead.id());

    getOrCreateRolePermission(adminRole.id(), explorerViewAll.id());
    getOrCreateRolePermission(adminRole.id(), explorerViewRestricted.id());
    getOrCreateRolePermission(adminRole.id(), reportViewAll.id());

    getOrCreateRolePermission(adminRole.id(), sqlListTables.id());
    getOrCreateRolePermission(adminRole.id(), sqlExecute.id());

    getOrCreateRolePermission(adminRole.id(), viewExplorer.id());
    getOrCreateRolePermission(adminRole.id(), viewJob.id());
    getOrCreateRolePermission(adminRole.id(), viewScanConfig.id());
    getOrCreateRolePermission(adminRole.id(), viewDirectoryConfig.id());
    getOrCreateRolePermission(adminRole.id(), viewServerConfig.id());
    getOrCreateRolePermission(adminRole.id(), viewSql.id());
    getOrCreateRolePermission(adminRole.id(), viewReport.id());
    getOrCreateRolePermission(adminRole.id(), explorerToggleAll.id());

    // MANAGER: read-only explorer + history
    getOrCreateRolePermission(managerRole.id(), reportViewAll.id());
    getOrCreateRolePermission(managerRole.id(), explorerViewAll.id());
    getOrCreateRolePermission(managerRole.id(), jobRead.id());
    getOrCreateRolePermission(managerRole.id(), viewExplorer.id());
    getOrCreateRolePermission(managerRole.id(), viewJob.id());
    getOrCreateRolePermission(managerRole.id(), viewReport.id());
    getOrCreateRolePermission(managerRole.id(), explorerToggleAll.id());

    // USER: restricted explorer only
    getOrCreateRolePermission(userRole.id(), reportViewRestricted.id());
    getOrCreateRolePermission(userRole.id(), explorerViewRestricted.id());
    getOrCreateRolePermission(userRole.id(), jobRead.id());
    getOrCreateRolePermission(userRole.id(), viewExplorer.id());
    getOrCreateRolePermission(userRole.id(), viewReport.id());

    // Create default users
    User admin = getOrCreateUser("admin", "admin");
    User manager = getOrCreateUser("manager", "manager");
    User user = getOrCreateUser("user", "user");

    getOrCreateUserRole(admin.id(), adminRole.id());
    getOrCreateUserRole(manager.id(), managerRole.id());
    getOrCreateUserRole(user.id(), userRole.id());
    LOG.info("Data initialization completed.");
  }

  private Permission getOrCreatePermission(String name) {
    return permissionRepository.findByName(name)
        .orElseGet(() -> permissionRepository.save(new Permission(null, name)));
  }

  private Role getOrCreateRole(String name) {
    return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(null, name)));
  }

  private User getOrCreateUser(String username, String pass) {
    return userRepository.findByUsername(username)
        .orElseGet(() -> userRepository.save(new User(null, username, pass)));
  }

  private void getOrCreateRolePermission(Long roleId, Long permissionId) {
    if (rolePermissionRepository.findByRoleId(roleId).stream()
        .noneMatch(rp -> rp.permissionId().equals(permissionId))) {
      rolePermissionRepository.save(new RolePermission(null, roleId, permissionId));
    }
  }

  private void getOrCreateUserRole(Long userId, Long roleId) {
    if (userRoleRepository.findByUserId(userId).stream().noneMatch(ur -> ur.roleId().equals(roleId))) {
      userRoleRepository.save(new UserRole(null, userId, roleId));
    }
  }
}
