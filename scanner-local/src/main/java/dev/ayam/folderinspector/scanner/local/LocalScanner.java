package dev.ayam.folderinspector.scanner.local;

import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.model.InheritFlag;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.ItemType;
import dev.ayam.folderinspector.core.model.PermissionType;
import dev.ayam.folderinspector.core.model.Principal;
import dev.ayam.folderinspector.core.model.PrincipalType;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.scanner.local.utility.StringResource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to recursively scan directories and extract file metadata from the
 * local file system.
 */
public class LocalScanner implements Scanner {

    private static final Logger logger = LoggerFactory.getLogger(LocalScanner.class);

    /**
     * Scans the given root path recursively.
     *
     * @param rootPath The root directory to scan.
     * @return A stream of {@link Item} objects representing the files and
     *         directories found.
     * @throws IOException If an I/O error occurs during scanning.
     */
    @Override
    public java.util.stream.Stream<Item> scan(Path rootPath) throws IOException {
        logger.debug(StringResource.STARTING_SCAN, rootPath);

        try {
            return Files.walk(rootPath)
                    .filter(path -> !path.equals(rootPath)) // Skip root itself
                    .map(path -> {
                        try {
                            return createItem(path, rootPath,
                                    Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
                        } catch (IOException e) {
                            logger.warn(StringResource.ERROR_ACCESS_FAILED, path, e.getMessage());
                            return null;
                        }
                    })
                    .filter(item -> item != null);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Helper to create an {@link Item} from a file path and its attributes.
     */
    private Item createItem(Path path, Path rootPath, BasicFileAttributes basicAttrs) throws IOException {
        String relativePath = rootPath.relativize(path).toString();
        if (relativePath.isEmpty()) {
            relativePath = path.getFileName().toString();
        }

        ItemType type = basicAttrs.isDirectory() ? ItemType.DIRECTORY : ItemType.FILE;
        long size = basicAttrs.isDirectory() ? 0 : basicAttrs.size();
        Instant lastModified = basicAttrs.lastModifiedTime().toInstant();
        Instant created = basicAttrs.creationTime().toInstant();

        Principal owner = null;
        Principal group = null;
        Set<PermissionType> permissions = EnumSet.noneOf(PermissionType.class);

        try {
            PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (posixView != null) {
                PosixFileAttributes posixAttrs = posixView.readAttributes();
                owner = new Principal(null, null, posixAttrs.owner().getName(), PrincipalType.USER);
                group = new Principal(null, null, posixAttrs.group().getName(), PrincipalType.GROUP);
                permissions = mapPosixPermissions(posixAttrs.permissions());
            } else {
                FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (ownerView != null) {
                    owner = new Principal(null, null, ownerView.getOwner().getName(), PrincipalType.USER);
                }
            }
        } catch (Exception e) {
            logger.trace(StringResource.ERROR_POSIX_FAILED, path, e.getMessage());
        }

        List<AclItem> acls = null;
        try {
            AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (aclView != null) {
                List<AclEntry> rawAcls = aclView.getAcl();
                if (rawAcls != null) {
                    acls = new ArrayList<>();
                    for (AclEntry entry : rawAcls) {
                        String name = entry.principal().getName();
                        acls.add(new AclItem(name, mapAclPermissions(entry.permissions()), mapAclFlags(entry.flags())));
                    }
                }
            }
        } catch (Exception e) {
            logger.trace(StringResource.ERROR_ACL_FAILED, path, e.getMessage());
        }

        return new Item(
                null, // No database ID during scan
                path.getFileName().toString(),
                path.getParent().toString(),
                type,
                size,
                lastModified,
                created,
                owner,
                group,
                permissions,
                acls);
    }

    private Set<PermissionType> mapPosixPermissions(Set<PosixFilePermission> posix) {
        Set<PermissionType> perms = EnumSet.noneOf(PermissionType.class);
        if (posix.contains(PosixFilePermission.OWNER_READ))
            perms.add(PermissionType.UNIX_READ);
        if (posix.contains(PosixFilePermission.OWNER_WRITE))
            perms.add(PermissionType.UNIX_WRITE);
        if (posix.contains(PosixFilePermission.OWNER_EXECUTE))
            perms.add(PermissionType.UNIX_EXECUTE);
        return perms;
    }

    private Set<PermissionType> mapAclPermissions(Set<AclEntryPermission> aclPerms) {
        Set<PermissionType> perms = EnumSet.noneOf(PermissionType.class);
        for (AclEntryPermission p : aclPerms) {
            if (p == AclEntryPermission.READ_DATA)
                perms.add(PermissionType.NTFS_READ);
            if (p == AclEntryPermission.WRITE_DATA)
                perms.add(PermissionType.NTFS_WRITE);
            // ... more mapping could be added here
        }
        return perms;
    }

    private Set<InheritFlag> mapAclFlags(Set<AclEntryFlag> aclFlags) {
        Set<InheritFlag> flags = EnumSet.noneOf(InheritFlag.class);
        if (aclFlags.contains(AclEntryFlag.FILE_INHERIT))
            flags.add(InheritFlag.OBJECT_INHERIT);
        if (aclFlags.contains(AclEntryFlag.DIRECTORY_INHERIT))
            flags.add(InheritFlag.CONTAINER_INHERIT);
        return flags;
    }

    /**
     * Returns the name of this scanner.
     *
     * @return "local"
     */
    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
