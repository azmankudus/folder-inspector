package dev.ayam.folderinspector.core;

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
@Command(name = "folder-inspector", mixinStandardHelpOptions = true, version = "1.0", description = "Scans a folder and reports file metadata.")
public class Main implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  @Parameters(index = "0", description = "The folder to scan.", defaultValue = ".")
  private String folderPath;

  @Option(names = { "-l",
      "--launcher" }, description = "Launcher implementation (e.g., console).", defaultValue = "console")
  private String launcherName;

  @Option(names = { "-s",
      "--scanner" }, description = "Scanner implementation (e.g., local).", defaultValue = "local")
  private String scannerName;

  @Option(names = { "-w",
      "--writer" }, description = "Writer implementation (e.g., console, csv).", defaultValue = "console")
  private String writerName;

  @Option(names = { "-n",
      "--notifier" }, description = "Notifier implementation (e.g., console).", defaultValue = "console")
  private String notifierName;

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
    sb.append("\nAvailable Plugins:\n");
    sb.append(String.format("  Launchers: %s\n", listServiceNames(Launcher.class)));
    sb.append(String.format("  Scanners:  %s\n", listServiceNames(Scanner.class)));
    sb.append(String.format("  Writers:   %s\n", listServiceNames(Writer.class)));
    sb.append(String.format("  Notifiers: %s\n", listServiceNames(Notifier.class)));
    return sb.toString();
  }

  private static <T> String listServiceNames(Class<T> serviceClass) {
    return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
        .map(s -> {
          try {
            return (String) serviceClass.getMethod("getName").invoke(s);
          } catch (Exception e) {
            return "unknown";
          }
        })
        .reduce((a, b) -> a + ", " + b)
        .orElse("none");
  }

  @Override
  public Integer call() {
    logger.debug("Bootstrapping application...");

    Notifier notifier = loadService(Notifier.class, notifierName);
    if (notifier == null) {
      System.err.println("Error: No Notifier found with name: " + notifierName);
      return 1;
    }

    Scanner scanner = loadService(Scanner.class, scannerName);
    if (scanner == null) {
      notifier.notifyError("Error: No Scanner found with name: " + scannerName);
      return 1;
    }

    Writer writer = loadService(Writer.class, writerName);
    if (writer == null) {
      notifier.notifyError("Error: No Writer found with name: " + writerName);
      return 1;
    }

    Launcher launcher = loadService(Launcher.class, launcherName);
    if (launcher == null) {
      notifier.notifyError("Error: No Launcher found with name: " + launcherName);
      return 1;
    }

    logger.debug("Plugins loaded: Launcher={}, Scanner={}, Writer={}, Notifier={}",
        launcherName, scannerName, writerName, notifierName);

    return launcher.launch(scanner, writer, notifier, folderPath);
  }

  private <T> T loadService(Class<T> serviceClass, String name) {
    return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
        .filter(s -> {
          try {
            String serviceName = (String) serviceClass.getMethod("getName").invoke(s);
            return serviceName.equalsIgnoreCase(name);
          } catch (Exception e) {
            logger.warn("Failed to get name for service: {}", s.getClass().getName(), e);
            return false;
          }
        })
        .findFirst()
        .orElse(null);
  }

  static class ExceptionHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
      return 2;
    }
  }
}
