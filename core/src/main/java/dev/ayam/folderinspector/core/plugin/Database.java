package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for database plugins that manage snapshots of file system items.
 */
public interface Database {

  /**
   * Metadata about a snapshot.
   */
  record SnapshotInfo(String name, long itemCount, long fileSize, Instant created) {
  }

  /**
   * Saves a stream of items as a named snapshot.
   *
   * @param items The items to save.
   * @param name  The name of the snapshot.
   * @throws Exception If an error occurs during saving.
   */
  void save(Stream<Item> items, String name) throws Exception;

  /**
   * Loads items from a named snapshot.
   *
   * @param name  The name of the snapshot.
   * @param query An optional query filter (plugin-specific).
   * @return A stream of items.
   * @throws Exception If an error occurs during loading.
   */
  Stream<Item> load(String name, String query) throws Exception;

  /**
   * Lists all available snapshots managed by this database plugin.
   *
   * @return A list of snapshot metadata.
   * @throws Exception If an error occurs during listing.
   */
  List<SnapshotInfo> list() throws Exception;

  /**
   * Deletes a named snapshot.
   *
   * @param name The name of the snapshot to delete.
   * @throws Exception If an error occurs during deletion.
   */
  void delete(String name) throws Exception;

  /**
   * Returns the unique name of this database plugin (e.g., "sqlite", "jpa").
   *
   * @return The plugin name.
   */
  String getName();
}
