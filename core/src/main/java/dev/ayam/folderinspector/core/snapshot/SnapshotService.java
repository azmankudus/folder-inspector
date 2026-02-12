package dev.ayam.folderinspector.core.snapshot;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.AclItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing searchable snapshots using SQLite.
 */
public class SnapshotService {

  private static final Logger logger = LoggerFactory.getLogger(SnapshotService.class);
  private static final String SNAPSHOT_DIR = System.getProperty("user.home") + "/.folder-inspector/snapshots";
  private static final int BATCH_SIZE = 10000;

  /**
   * Record representing snapshot metadata.
   */
  public record SnapshotInfo(String name, long itemCount, long fileSize, Instant created) {
  }

  /**
   * Saves items to a snapshot database.
   */
  public void save(Stream<Item> items, String name) throws SQLException, IOException {
    Path snapshotPath = getSnapshotPath(name);
    Files.createDirectories(snapshotPath.getParent());

    // Delete existing if present
    Files.deleteIfExists(snapshotPath);

    String url = "jdbc:sqlite:" + snapshotPath.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(url)) {
      // Enable WAL mode BEFORE disabling autocommit (WAL mode requires autocommit)
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("PRAGMA journal_mode=WAL");
        stmt.execute("PRAGMA synchronous=NORMAL");
      }

      conn.setAutoCommit(false);

      createSchema(conn);

      String insertSql = """
          INSERT INTO items (absolutePath, relativePath, type, size, lastModified, created, owner, groupName, permissions, acls)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          """;

      try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
        int count = 0;
        Iterator<Item> it = items.iterator();

        while (it.hasNext()) {
          Item item = it.next();
          pstmt.setString(1, item.absolutePath());
          pstmt.setString(2, item.relativePath());
          pstmt.setString(3, item.type());
          pstmt.setLong(4, item.size());
          pstmt.setString(5, item.lastModified() != null ? item.lastModified().toString() : null);
          pstmt.setString(6, item.created() != null ? item.created().toString() : null);
          pstmt.setString(7, item.owner());
          pstmt.setString(8, item.group());
          pstmt.setString(9, item.permissions());
          pstmt.setString(10, formatAcls(item.acls()));
          pstmt.addBatch();

          if (++count % BATCH_SIZE == 0) {
            pstmt.executeBatch();
            conn.commit();
            logger.debug("Saved {} items...", count);
          }
        }

        pstmt.executeBatch();
        conn.commit();
        logger.info("Saved {} items to snapshot '{}'", count, name);
      }

      // Create indexes after bulk insert for better performance
      createIndexes(conn);
      conn.commit();

      // VACUUM must run outside transaction - re-enable autocommit
      conn.setAutoCommit(true);
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("VACUUM");
      }
    }
  }

  /**
   * Loads items from a snapshot, optionally filtered by a WHERE clause.
   */
  public Stream<Item> load(String name, String whereClause) throws SQLException {
    Path snapshotPath = getSnapshotPath(name);
    if (!Files.exists(snapshotPath)) {
      throw new SQLException("Snapshot not found: " + name);
    }

    String url = "jdbc:sqlite:" + snapshotPath.toAbsolutePath();
    Connection conn = DriverManager.getConnection(url);

    String sql = "SELECT * FROM items";
    if (whereClause != null && !whereClause.isBlank()) {
      sql += " WHERE " + whereClause;
    }

    try {
      Statement stmt = conn.createStatement();
      stmt.setFetchSize(1000); // Cursor-based fetching
      ResultSet rs = stmt.executeQuery(sql);

      // Return a stream that closes resources when done
      Iterator<Item> iterator = new ResultSetIterator(rs, stmt, conn);
      Spliterator<Item> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
      return StreamSupport.stream(spliterator, false)
          .onClose(() -> closeQuietly(conn));
    } catch (SQLException e) {
      closeQuietly(conn);
      throw e;
    }
  }

  /**
   * Lists all available snapshots.
   */
  public List<SnapshotInfo> list() throws IOException, SQLException {
    Path snapshotDir = Paths.get(SNAPSHOT_DIR);
    if (!Files.exists(snapshotDir)) {
      return Collections.emptyList();
    }

    List<SnapshotInfo> snapshots = new ArrayList<>();
    try (var files = Files.list(snapshotDir)) {
      for (Path path : files.filter(p -> p.toString().endsWith(".db")).toList()) {
        String name = path.getFileName().toString().replace(".db", "");
        long fileSize = Files.size(path);
        Instant created = Files.getLastModifiedTime(path).toInstant();
        long itemCount = getItemCount(path);
        snapshots.add(new SnapshotInfo(name, itemCount, fileSize, created));
      }
    }
    return snapshots;
  }

  /**
   * Deletes a snapshot.
   */
  public void delete(String name) throws IOException {
    Path snapshotPath = getSnapshotPath(name);
    Files.deleteIfExists(snapshotPath);
    // Also delete WAL and SHM files if they exist
    Files.deleteIfExists(Paths.get(snapshotPath + "-wal"));
    Files.deleteIfExists(Paths.get(snapshotPath + "-shm"));
    logger.info("Deleted snapshot '{}'", name);
  }

  private Path getSnapshotPath(String name) {
    return Paths.get(SNAPSHOT_DIR, name + ".db");
  }

  private void createSchema(Connection conn) throws SQLException {
    String createTable = """
        CREATE TABLE IF NOT EXISTS items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            absolutePath TEXT,
            relativePath TEXT,
            type TEXT,
            size INTEGER,
            lastModified TEXT,
            created TEXT,
            owner TEXT,
            groupName TEXT,
            permissions TEXT,
            acls TEXT
        )
        """;
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(createTable);
    }
  }

  private void createIndexes(Connection conn) throws SQLException {
    String[] indexes = {
        "CREATE INDEX IF NOT EXISTS idx_relativePath ON items(relativePath)",
        "CREATE INDEX IF NOT EXISTS idx_type ON items(type)",
        "CREATE INDEX IF NOT EXISTS idx_size ON items(size)",
        "CREATE INDEX IF NOT EXISTS idx_owner ON items(owner)"
    };
    try (Statement stmt = conn.createStatement()) {
      for (String index : indexes) {
        stmt.execute(index);
      }
    }
  }

  private String formatAcls(List<AclItem> acls) {
    if (acls == null || acls.isEmpty())
      return "";
    StringBuilder sb = new StringBuilder();
    for (AclItem acl : acls) {
      if (sb.length() > 0)
        sb.append("; ");
      sb.append(acl.name()).append(":[")
          .append(String.join(",", acl.permissions())).append("]:[")
          .append(String.join(",", acl.flags())).append("]");
    }
    return sb.toString();
  }

  private long getItemCount(Path dbPath) throws SQLException {
    String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
    try (Connection conn = DriverManager.getConnection(url);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items")) {
      return rs.getLong(1);
    }
  }

  private void closeQuietly(Connection conn) {
    try {
      if (conn != null)
        conn.close();
    } catch (SQLException e) {
      logger.warn("Failed to close connection", e);
    }
  }

  /**
   * Iterator that converts ResultSet rows to Item objects.
   */
  private static class ResultSetIterator implements Iterator<Item> {
    private final ResultSet rs;
    private final Statement stmt;
    private final Connection conn;
    private boolean hasNext;

    ResultSetIterator(ResultSet rs, Statement stmt, Connection conn) throws SQLException {
      this.rs = rs;
      this.stmt = stmt;
      this.conn = conn;
      this.hasNext = rs.next();
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public Item next() {
      if (!hasNext)
        throw new NoSuchElementException();
      try {
        Item item = new Item(
            rs.getString("absolutePath"),
            rs.getString("relativePath"),
            rs.getString("type"),
            rs.getLong("size"),
            parseInstant(rs.getString("lastModified")),
            parseInstant(rs.getString("created")),
            rs.getString("owner"),
            rs.getString("groupName"),
            rs.getString("permissions"),
            null // ACLs not loaded back for simplicity
        );
        hasNext = rs.next();
        if (!hasNext) {
          // Close resources when done
          rs.close();
          stmt.close();
        }
        return item;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    private Instant parseInstant(String str) {
      if (str == null || str.isEmpty())
        return null;
      try {
        return Instant.parse(str);
      } catch (Exception e) {
        return null;
      }
    }
  }
}
