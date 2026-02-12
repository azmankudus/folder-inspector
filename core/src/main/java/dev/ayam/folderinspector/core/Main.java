package dev.ayam.folderinspector.core;

import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.plugin.Launcher;
import dev.ayam.folderinspector.core.plugin.Notifier;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.plugin.Writer;
import dev.ayam.folderinspector.core.utility.StringResource;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

/**
 * Main entry point for the Folder Inspector application.
 * Manages plugin selection and execution.
 */
@Command(name = "folder-inspector", resourceBundle = "strings_core", mixinStandardHelpOptions = true, version = "1.0")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  @Parameters(index = "0", descriptionKey = "folder-inspector.folderPath.description", defaultValue = ".")
  private String folderPath;

  @Option(names = { "-l",
      "--launcher" }, descriptionKey = "folder-inspector.launcher.description", defaultValue = "console")
  private String launcherName;

  @Option(names = { "-s",
      "--scanner" }, descriptionKey = "folder-inspector.scanner.description", defaultValue = "local")
  private String scannerName;

  @Option(names = { "-f",
      "--formatter" }, descriptionKey = "folder-inspector.formatter.description", defaultValue = "text")
  private String formatterName;

  @Option(names = { "-w",
      "--writer" }, descriptionKey = "folder-inspector.writer.description", defaultValue = "console")
  private String writerName;

  @Option(names = { "-n",
      "--notifier" }, descriptionKey = "folder-inspector.notifier.description", defaultValue = "console")
  private String notifierName;

  @Option(names = { "-d",
      "--database" }, description = "Database plugin to use for snapshots", defaultValue = "jpa")
  private String databaseName;

  @Option(names = {
      "--scheduler" }, description = "Scheduler plugin to use for background scans", defaultValue = "quartz")
  private String schedulerName;

  @Option(names = { "--save-snapshot" }, description = "Save scan results to a named snapshot")
  private String saveSnapshotName;

  @Option(names = { "--load-snapshot" }, description = "Load items from a named snapshot")
  private String loadSnapshotName;

  @Option(names = { "--list-snapshots" }, description = "List available snapshots")
  private boolean listSnapshots;

  @Option(names = { "--query", "-q" }, description = "Query to filter snapshot results")
  private String queryFilter;

  @Option(names = { "--limit" }, description = "Limit number of results", defaultValue = "-1")
  private int limit;

  /**
   * Main entry point. Initializes Picocli and executes the command.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new Main())
        .setExecutionExceptionHandler(new ExceptionHandler());

    String footer = getAvailablePlugins();
    commandLine.getCommandSpec().usageMessage().footer(footer);

    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

  /**
   * Generates a footer string for the usage message listing available plugins.
   *
   * @return The footer string.
   */
  private static String getAvailablePlugins() {
    StringBuilder sb = new StringBuilder();
    sb.append(StringResource.AVAILABLE_PLUGINS_HEADER);
    sb.append(String.format(StringResource.LAUNCHERS_LABEL, listServiceNames(Launcher.class)));
    sb.append(String.format(StringResource.SCANNERS_LABEL, listServiceNames(Scanner.class)));
    sb.append(String.format(StringResource.FORMATTERS_LABEL, listServiceNames(Formatter.class)));
    sb.append(String.format(StringResource.WRITERS_LABEL, listServiceNames(Writer.class)));
    sb.append(String.format(StringResource.NOTIFIERS_LABEL, listServiceNames(Notifier.class)));
    sb.append(String.format(" Databases: %s\n", listServiceNames(Database.class)));
    sb.append(
        String.format(" Schedulers: %s\n", listServiceNames(dev.ayam.folderinspector.core.plugin.Scheduler.class)));
    return sb.toString();
  }

  /**
   * Lists names of all registered implementations of a given service class.
   *
   * @param serviceClass The interface class to load services for.
   * @return A comma-separated string of plugin names.
   */
  private static <T> String listServiceNames(Class<T> serviceClass) {
    return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
        .map(s -> {
          try {
            return (String) serviceClass.getMethod("getName").invoke(s);
          } catch (Exception e) {
            return StringResource.UNKNOWN_PLUGIN;
          }
        })
        .reduce((a, b) -> a + ", " + b)
        .orElse(StringResource.NONE_PLUGIN);
  }

  /**
   * Core execution logic. Loads required plugins and launches the scan.
   *
   * @return Process exit code.
   */
  @Override
  public Integer call() {
    logger.debug(StringResource.BOOTSTRAPPING_APP);

    Notifier notifier = loadService(Notifier.class, notifierName);
    if (notifier == null) {
      System.err.println(StringResource.ERROR_PREFIX + StringResource.ERR_NOTIFIER_NOT_FOUND
          + (notifierName != null ? notifierName : "default"));
      return 1;
    }

    // Load database plugin if needed
    Database database = null;
    if (listSnapshots || loadSnapshotName != null || saveSnapshotName != null) {
      database = loadService(Database.class, databaseName);
      if (database == null) {
        notifier.notifyError("Database plugin not found: " + databaseName);
        return 1;
      }
    }

    // Handle --list-snapshots
    if (listSnapshots) {
      return handleListSnapshots(database, notifier);
    }

    // Handle --load-snapshot (loads from database)
    if (loadSnapshotName != null) {
      return handleLoadSnapshot(database, notifier);
    }

    // Normal scan flow
    Scanner scanner = loadService(Scanner.class, scannerName);
    if (scanner == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_SCANNER_NOT_FOUND + scannerName);
      return 1;
    }

    Formatter formatter = loadService(Formatter.class, formatterName);
    if (formatter == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_FORMATTER_NOT_FOUND + formatterName);
      return 1;
    }

    Writer writer = loadService(Writer.class, writerName);
    if (writer == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_WRITER_NOT_FOUND + writerName);
      return 1;
    }

    // Handle --save-snapshot during normal scan
    if (saveSnapshotName != null) {
      return handleSaveSnapshot(database, scanner, formatter, writer, notifier);
    }

    Launcher launcher = loadService(Launcher.class, launcherName);
    if (launcher == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_LAUNCHER_NOT_FOUND + launcherName);
      return 1;
    }

    dev.ayam.folderinspector.core.plugin.Scheduler scheduler = loadService(
        dev.ayam.folderinspector.core.plugin.Scheduler.class, schedulerName);
    if (scheduler == null) {
      notifier.notifyError("Scheduler plugin not found: " + schedulerName);
      return 1;
    }

    logger.debug(StringResource.PLUGINS_LOADED,
        launcherName, scannerName, formatterName, writerName, notifierName);

    return launcher.launch(database, scanner, formatter, writer, notifier, scheduler, folderPath);
  }

  /**
   * Lists available snapshots to the user via the notifier.
   */
  private Integer handleListSnapshots(Database database, Notifier notifier) {
    try {
      var snapshots = database.list();
      if (snapshots.isEmpty()) {
        notifier.notify("No snapshots found in database: " + database.getName());
      } else {
        System.out.println("Available snapshots (" + database.getName() + "):");
        System.out.printf("%-20s %15s %15s %s%n", "NAME", "ITEMS", "SIZE", "CREATED");
        System.out.println("-".repeat(70));
        for (var snap : snapshots) {
          System.out.printf("%-20s %,15d %,12d KB %s%n",
              snap.name(), snap.itemCount(), snap.fileSize() / 1024, snap.created());
        }
      }
      return 0;
    } catch (Exception e) {
      notifier.notifyError("Failed to list snapshots: " + e.getMessage());
      return 1;
    }
  }

  /**
   * Loads and displays contents of a snapshot.
   */
  private Integer handleLoadSnapshot(Database database, Notifier notifier) {
    try {
      Formatter formatter = loadService(Formatter.class, formatterName);
      if (formatter == null) {
        notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_FORMATTER_NOT_FOUND + formatterName);
        return 1;
      }

      Writer writer = loadService(Writer.class, writerName);
      if (writer == null) {
        notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_WRITER_NOT_FOUND + writerName);
        return 1;
      }

      try (var items = database.load(loadSnapshotName, queryFilter)) {
        var itemStream = limit > 0 ? items.limit(limit) : items;
        try (var formatted = formatter.format(itemStream)) {
          writer.write(formatted);
        }
      }
      notifier.notify("Loaded from snapshot: " + loadSnapshotName);
      return 0;
    } catch (Exception e) {
      notifier.notifyError("Failed to load snapshot: " + e.getMessage());
      logger.error("Snapshot load error", e);
      return 1;
    }
  }

  /**
   * Executes a scan and saves the results to a snapshot.
   */
  private Integer handleSaveSnapshot(Database database, Scanner scanner, Formatter formatter, Writer writer,
      Notifier notifier) {
    try {
      var rootPath = java.nio.file.Paths.get(folderPath);

      // Scan and save to snapshot
      try (var items = scanner.scan(rootPath)) {
        database.save(items, saveSnapshotName);
      }

      notifier.notify("Saved snapshot: " + saveSnapshotName);
      return 0;
    } catch (Exception e) {
      notifier.notifyError("Failed to save snapshot: " + e.getMessage());
      logger.error("Snapshot save error", e);
      return 1;
    }
  }

  /**
   * Helper to load a specific service implementation by name.
   */
  private <T> T loadService(Class<T> serviceClass, String name) {
    return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
        .filter(s -> {
          try {
            String serviceName = (String) serviceClass.getMethod("getName").invoke(s);
            return serviceName.equalsIgnoreCase(name);
          } catch (Exception e) {
            logger.warn(StringResource.ERR_SERVICE_NAME_FAILED, s.getClass().getName(), e);
            return false;
          }
        })
        .findFirst()
        .orElse(null);
  }

  /**
   * Execution exception handler for picocli.
   */
  static class ExceptionHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
      System.err.println(StringResource.ERR_EXECUTION_FAILED + ex.getMessage());
      ex.printStackTrace();
      return 2;
    }
  }
}
