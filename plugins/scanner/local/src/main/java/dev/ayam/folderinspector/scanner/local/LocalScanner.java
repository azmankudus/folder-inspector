package dev.ayam.folderinspector.scanner.local;

import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.scanner.local.utility.StringResource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to recursively scan directories and extract file metadata.
 */
public class LocalScanner implements Scanner {

    private static final Logger logger = LoggerFactory.getLogger(LocalScanner.class);

    /**
     * Scans the given root path recursively.
     *
     * @param rootPath The root directory to scan.
     * @return A list of {@link Item} objects representing the files and directories
     *         found.
     * @throws IOException If an I/O error occurs during scanning.
     */
    @Override
    public java.util.stream.Stream<Item> scan(Path rootPath) throws IOException {
        logger.debug(StringResource.STARTING_SCAN, rootPath);

        try {
            return Files.walk(rootPath)
                    .filter(path -> !path.equals(rootPath)) // Skip root itself if desired, matching original behavior
                                                            // (preVisitDirectory check)
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
            // Files.walk might throw immediately. In a more advanced implementation we'd
            // use a custom Spliterator.
            // For now we rethrow or handle. Original contract throws IOException.
            throw e;
        }
    }

    private Item createItem(Path path, Path rootPath, BasicFileAttributes basicAttrs) throws IOException {
        String relativePath = rootPath.relativize(path).toString();
        if (relativePath.isEmpty()) {
            relativePath = path.getFileName().toString();
        }

        String type = basicAttrs.isDirectory() ? StringResource.FILE_TYPE_DIRECTORY
                : basicAttrs.isSymbolicLink() ? StringResource.FILE_TYPE_SYMLINK : StringResource.FILE_TYPE_FILE;

        long size = basicAttrs.isDirectory() ? 0 : basicAttrs.size();
        Instant lastModified = basicAttrs.lastModifiedTime().toInstant();
        Instant created = basicAttrs.creationTime().toInstant();

        String owner = StringResource.NA;
        String group = StringResource.NA;
        String permissions = StringResource.NA;

        try {
            PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (posixView != null) {
                PosixFileAttributes posixAttrs = posixView.readAttributes();
                owner = posixAttrs.owner().getName();
                group = posixAttrs.group().getName();
                permissions = PosixFilePermissions.toString(posixAttrs.permissions());
            } else {
                FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (ownerView != null) {
                    owner = ownerView.getOwner().getName();
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
                        List<String> aclPermissions = entry.permissions().stream().map(Object::toString).toList();
                        List<String> aclFlags = entry.flags().stream().map(Object::toString).toList();
                        acls.add(new AclItem(name, aclPermissions, aclFlags));
                    }
                }
            }
        } catch (Exception e) {
            logger.trace(StringResource.ERROR_ACL_FAILED, path, e.getMessage());
        }

        return new Item(
                path.toAbsolutePath().toString(),
                relativePath,
                type,
                size,
                lastModified,
                created,
                owner,
                group,
                permissions,
                acls);
    }

    @Override
    public String getName() {
        return StringResource.NAME;
    }
}
