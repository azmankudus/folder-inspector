package backend.service;

import backend.model.*;
import backend.repository.*;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataInit {
    private static final Logger LOG = LoggerFactory.getLogger(DataInit.class);

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public DataInit(UserAccountRepository userRepository,
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
        // Create permissions
        Permission adRead = getOrCreatePermission("AD_READ");
        Permission adCreate = getOrCreatePermission("AD_CREATE");
        Permission adUpdate = getOrCreatePermission("AD_UPDATE");
        Permission adDelete = getOrCreatePermission("AD_DELETE");

        Permission connRead = getOrCreatePermission("CONN_READ");
        Permission connCreate = getOrCreatePermission("CONN_CREATE");
        Permission connUpdate = getOrCreatePermission("CONN_UPDATE");
        Permission connDelete = getOrCreatePermission("CONN_DELETE");

        Permission scanRead = getOrCreatePermission("SCAN_READ");
        Permission scanCreate = getOrCreatePermission("SCAN_CREATE");
        Permission scanUpdate = getOrCreatePermission("SCAN_UPDATE");
        Permission scanDelete = getOrCreatePermission("SCAN_DELETE");

        Permission scanStart = getOrCreatePermission("SCAN_START");
        Permission scanStop = getOrCreatePermission("SCAN_STOP");
        Permission scanHistRead = getOrCreatePermission("SCAN_HISTORY_READ");

        Permission viewAllFiles = getOrCreatePermission("FILE_EXPLORER_VIEW_ALL");
        Permission viewRestrictedFiles = getOrCreatePermission("FILE_EXPLORER_VIEW_RESTRICTED");
        Permission sqlAdmin = getOrCreatePermission("SQL_ADMIN");

        // Create roles
        Role adminRole = getOrCreateRole("ADMIN");
        Role managerRole = getOrCreateRole("MANAGER");
        Role userRole = getOrCreateRole("USER");

        // Map roles to permissions
        getOrCreateRolePermission(adminRole.id(), adRead.id());
        getOrCreateRolePermission(adminRole.id(), adCreate.id());
        getOrCreateRolePermission(adminRole.id(), adUpdate.id());
        getOrCreateRolePermission(adminRole.id(), adDelete.id());

        getOrCreateRolePermission(adminRole.id(), connRead.id());
        getOrCreateRolePermission(adminRole.id(), connCreate.id());
        getOrCreateRolePermission(adminRole.id(), connUpdate.id());
        getOrCreateRolePermission(adminRole.id(), connDelete.id());

        getOrCreateRolePermission(adminRole.id(), scanRead.id());
        getOrCreateRolePermission(adminRole.id(), scanCreate.id());
        getOrCreateRolePermission(adminRole.id(), scanUpdate.id());
        getOrCreateRolePermission(adminRole.id(), scanDelete.id());

        getOrCreateRolePermission(adminRole.id(), scanStart.id());
        getOrCreateRolePermission(adminRole.id(), scanStop.id());
        getOrCreateRolePermission(adminRole.id(), scanHistRead.id());
        getOrCreateRolePermission(adminRole.id(), viewAllFiles.id());
        getOrCreateRolePermission(adminRole.id(), sqlAdmin.id());

        getOrCreateRolePermission(managerRole.id(), viewAllFiles.id());

        getOrCreateRolePermission(userRole.id(), viewRestrictedFiles.id());

        // Create users
        UserAccount admin = getOrCreateUser("admin", "admin");
        UserAccount manager = getOrCreateUser("manager", "manager");
        UserAccount user = getOrCreateUser("user", "user");

        // Map users to roles
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

    private UserAccount getOrCreateUser(String username, String pass) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new UserAccount(null, username, pass)));
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
