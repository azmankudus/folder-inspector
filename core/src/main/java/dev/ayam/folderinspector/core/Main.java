package dev.ayam.folderinspector.core;

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

  @Option(names = { "--save-snapshot" }, description = "Save scan results to a named snapshot")
  private String saveSnapshotName;

  @Option(names = { "--load-snapshot" }, description = "Load items from a named snapshot")
  private String loadSnapshotName;

  @Option(names = { "--list-snapshots" }, description = "List available snapshots")
  private boolean listSnapshots;

  @Option(names = { "--query", "-q" }, description = "SQL WHERE clause to filter snapshot results")
  private String queryFilter;

  @Option(names = { "--limit" }, description = "Limit number of results", defaultValue = "-1")
  private int limit;

  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new Main())
        .setExecutionExceptionHandler(new ExceptionHandler());

    String footer = getAvailablePlugins();
    commandLine.getCommandSpec().usageMessage().footer(footer);

    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

  private static String getAvailablePlugins() {
    StringBuilder sb = new StringBuilder();
    sb.append(StringResource.AVAILABLE_PLUGINS_HEADER);
    sb.append(String.format(StringResource.LAUNCHERS_LABEL, listServiceNames(Launcher.class)));
    sb.append(String.format(StringResource.SCANNERS_LABEL, listServiceNames(Scanner.class)));
    sb.append(String.format(StringResource.FORMATTERS_LABEL, listServiceNames(Formatter.class)));
    sb.append(String.format(StringResource.WRITERS_LABEL, listServiceNames(Writer.class)));
    sb.append(String.format(StringResource.NOTIFIERS_LABEL, listServiceNames(Notifier.class)));
    return sb.toString();
  }

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

  @Override
  public Integer call() {
    logger.debug(StringResource.BOOTSTRAPPING_APP);

    Notifier notifier = loadService(Notifier.class, notifierName);
    if (notifier == null) {
      System.err.println(StringResource.ERROR_PREFIX + StringResource.ERR_NOTIFIER_NOT_FOUND + notifierName);
      return 1;
    }

    // Handle --list-snapshots
    if (listSnapshots) {
      return handleListSnapshots(notifier);
    }

    // Handle --load-snapshot (loads from database)
    if (loadSnapshotName != null) {
      return handleLoadSnapshot(notifier);
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
      return handleSaveSnapshot(scanner, formatter, writer, notifier);
    }

    Launcher launcher = loadService(Launcher.class, launcherName);
    if (launcher == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_LAUNCHER_NOT_FOUND + launcherName);
      return 1;
    }

    logger.debug(StringResource.PLUGINS_LOADED,
        launcherName, scannerName, formatterName, writerName, notifierName);

    return launcher.launch(scanner, formatter, writer, notifier, folderPath);
  }

  private Integer handleListSnapshots(Notifier notifier) {
    try {
      var snapshotService = new dev.ayam.folderinspector.core.snapshot.SnapshotService();
      var snapshots = snapshotService.list();
      if (snapshots.isEmpty()) {
        notifier.notify("No snapshots found.");
      } else {
        System.out.println("Available snapshots:");
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

  private Integer handleLoadSnapshot(Notifier notifier) {
    try {
      var snapshotService = new dev.ayam.folderinspector.core.snapshot.SnapshotService();

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

      String whereClause = queryFilter;
      if (limit > 0 && whereClause != null) {
        // Limit is applied in SQL
      }

      try (var items = snapshotService.load(loadSnapshotName, whereClause)) {
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

  private Integer handleSaveSnapshot(Scanner scanner, Formatter formatter, Writer writer, Notifier notifier) {
    try {
      var snapshotService = new dev.ayam.folderinspector.core.snapshot.SnapshotService();
      var rootPath = java.nio.file.Paths.get(folderPath);

      // Scan and save to snapshot
      try (var items = scanner.scan(rootPath)) {
        snapshotService.save(items, saveSnapshotName);
      }

      notifier.notify("Saved snapshot: " + saveSnapshotName);
      return 0;
    } catch (Exception e) {
      notifier.notifyError("Failed to save snapshot: " + e.getMessage());
      logger.error("Snapshot save error", e);
      return 1;
    }
  }

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

  static class ExceptionHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
      System.err.println(StringResource.ERR_EXECUTION_FAILED + ex.getMessage());
      ex.printStackTrace();
      return 2;
    }
  }
}
