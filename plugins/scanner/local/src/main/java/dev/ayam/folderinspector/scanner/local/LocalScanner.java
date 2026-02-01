package dev.ayam.folderinspector.scanner.local;

import dev.ayam.folderinspector.core.Scanner;
import dev.ayam.folderinspector.core.model.Item;
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
    public List<Item> scan(Path rootPath) throws IOException {
        logger.debug("Starting scan of directory: {}", rootPath);
        List<Item> items = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(rootPath)) {
                    items.add(createItem(dir, rootPath, attrs));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                items.add(createItem(file, rootPath, attrs));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                logger.warn("Failed to access file/directory: {} - {}", file, exc.getMessage());
                // We should probably rely on a Notifier here, but for now system.err or logger
                // is within scope of a local scanner logic handling issues
                return FileVisitResult.CONTINUE;
            }
        });

        logger.info("Scan completed. Found {} items.", items.size());
        return items;
    }

    private Item createItem(Path path, Path rootPath, BasicFileAttributes basicAttrs) throws IOException {
        String relativePath = rootPath.relativize(path).toString();
        if (relativePath.isEmpty()) {
            relativePath = path.getFileName().toString();
        }

        String type = basicAttrs.isDirectory() ? "Directory" : basicAttrs.isSymbolicLink() ? "Symbolic Link" : "File";

        long size = basicAttrs.isDirectory() ? 0 : basicAttrs.size();
        Instant lastModified = basicAttrs.lastModifiedTime().toInstant();
        Instant created = basicAttrs.creationTime().toInstant();

        String owner = "N/A";
        String group = "N/A";
        String permissions = "N/A";

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
            logger.trace("Failed to retrieve POSIX/Owner attributes for {}: {}", path, e.getMessage());
        }

        List<AclEntry> acls = null;
        try {
            AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (aclView != null) {
                acls = aclView.getAcl();
            }
        } catch (Exception e) {
            logger.trace("Failed to retrieve ACL attributes for {}: {}", path, e.getMessage());
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
        return "local";
    }
}
