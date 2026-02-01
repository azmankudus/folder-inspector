package dev.ayam.folderinspector.core;

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

  @Option(names = { "-w",
      "--writer" }, descriptionKey = "folder-inspector.writer.description", defaultValue = "console")
  private String writerName;

  @Option(names = { "-n",
      "--notifier" }, descriptionKey = "folder-inspector.notifier.description", defaultValue = "console")
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
    sb.append(StringResource.AVAILABLE_PLUGINS_HEADER);
    sb.append(String.format(StringResource.LAUNCHERS_LABEL, listServiceNames(Launcher.class)));
    sb.append(String.format(StringResource.SCANNERS_LABEL, listServiceNames(Scanner.class)));
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

    Scanner scanner = loadService(Scanner.class, scannerName);
    if (scanner == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_SCANNER_NOT_FOUND + scannerName);
      return 1;
    }

    Writer writer = loadService(Writer.class, writerName);
    if (writer == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_WRITER_NOT_FOUND + writerName);
      return 1;
    }

    Launcher launcher = loadService(Launcher.class, launcherName);
    if (launcher == null) {
      notifier.notifyError(StringResource.ERROR_PREFIX + StringResource.ERR_LAUNCHER_NOT_FOUND + launcherName);
      return 1;
    }

    logger.debug(StringResource.PLUGINS_LOADED,
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
