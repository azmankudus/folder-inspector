package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.core.plugin.Scheduler;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service providing core functionality for the folder inspector.
 */
@Singleton
public class InspectorService {

  private final ScannerContext scannerContext;
  private final DatabaseContext databaseContext;
  private final SchedulerContext schedulerContext;

  public InspectorService(ScannerContext scannerContext, DatabaseContext databaseContext,
      SchedulerContext schedulerContext) {
    this.scannerContext = scannerContext;
    this.databaseContext = databaseContext;
    this.schedulerContext = schedulerContext;
  }

  public List<Item> scan() throws IOException {
    try (Stream<Item> stream = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      return stream.collect(Collectors.toList());
    }
  }

  public List<Database.SnapshotInfo> listSnapshots() throws Exception {
    return databaseContext.getDatabase().list();
  }

  public void saveSnapshot(String name) throws Exception {
    try (Stream<Item> items = scannerContext.getScanner().scan(scannerContext.getRootPath())) {
      databaseContext.getDatabase().save(items, name);
    }
  }

  public List<Item> loadSnapshot(String name, String query, int limit) throws Exception {
    String whereClause = (query == null || query.isBlank()) ? null : query;
    try (Stream<Item> stream = databaseContext.getDatabase().load(name, whereClause)) {
      Stream<Item> limited = limit > 0 ? stream.limit(limit) : stream;
      return limited.collect(Collectors.toList());
    }
  }

  public void deleteSnapshot(String name) throws Exception {
    databaseContext.getDatabase().delete(name);
  }

  public List<Scheduler.ScheduleInfo> listSchedules() throws Exception {
    return schedulerContext.getScheduler().listScans();
  }

  public void scheduleScan(String name, String cron, String path, String snapshotName) throws Exception {
    schedulerContext.getScheduler().scheduleScan(
        name,
        cron,
        path,
        snapshotName,
        scannerContext.getScanner(),
        databaseContext.getDatabase());
  }

  public void deleteSchedule(String name) throws Exception {
    schedulerContext.getScheduler().deleteScan(name);
  }
}
