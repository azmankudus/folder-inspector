package dev.ayam.folderinspector.launcher.web;

import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.plugin.Launcher;
import dev.ayam.folderinspector.core.plugin.Notifier;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.plugin.Writer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import java.nio.file.Paths;

/**
 * Web launcher that serves a SolidJS frontend with REST API.
 */
public class WebLauncher implements Launcher {

  @Override
  public int launch(Scanner scanner, Formatter formatter, Writer writer, Notifier notifier, String path) {
    try (ApplicationContext context = Micronaut.run(WebLauncher.class)) {
      ScannerContext scannerContext = context.getBean(ScannerContext.class);
      scannerContext.setScanner(scanner);
      scannerContext.setRootPath(Paths.get(path));

      // Keep the application running
      Thread.currentThread().join();
      return 0;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return 1;
    }
  }

  @Override
  public String getName() {
    return "web";
  }
}
