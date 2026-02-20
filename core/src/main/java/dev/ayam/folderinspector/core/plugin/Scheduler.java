package dev.ayam.folderinspector.core.plugin;

import java.util.List;

/**
 * Interface for scheduling background folder scans.
 */
public interface Scheduler {

  /**
   * Information about a scheduled scan.
   */
  record ScheduleInfo(String name, String cron, String path, String snapshotName) {
  }

  /**
   * Schedules a new background scan.
   *
   * @param name           Unique name for the schedule.
   * @param cronExpression Cron expression for scheduling.
   * @param path           Root path to scan.
   * @param snapshotName   Name of the snapshot to save.
   * @param scanner        The scanner plugin to use.
   * @param database       The database plugin to use.
   * @throws Exception If scheduling fails.
   */
  void scheduleScan(String name, String cronExpression, String path, String snapshotName,
      Scanner scanner, Database database) throws Exception;

  /**
   * Deletes a scheduled scan by name.
   *
   * @param name Name of the schedule to delete.
   * @throws Exception If deletion fails.
   */
  void deleteScan(String name) throws Exception;

  /**
   * Lists all active scan schedules.
   *
   * @return A list of schedule information objects.
   * @throws Exception If listing fails.
   */
  List<ScheduleInfo> listScans() throws Exception;

  /**
   * Starts the scheduler.
   *
   * @throws Exception If starting fails.
   */
  void start() throws Exception;

  /**
   * Shuts down the scheduler.
   *
   * @throws Exception If shutdown fails.
   */
  void shutdown() throws Exception;

  /**
   * Returns the unique name of this scheduler implementation.
   *
   * @return The scheduler name.
   */
  String getName();
}
