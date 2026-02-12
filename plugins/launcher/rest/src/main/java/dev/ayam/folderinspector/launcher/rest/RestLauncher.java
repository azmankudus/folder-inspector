package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.plugin.Launcher;
import dev.ayam.folderinspector.core.plugin.Notifier;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.plugin.Writer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import java.nio.file.Paths;

public class RestLauncher implements Launcher {

  @Override
  public int launch(Scanner scanner, Formatter formatter, Writer writer, Notifier notifier, String path) {
    try (ApplicationContext context = Micronaut.run(RestLauncher.class)) {
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
    return "rest";
  }
}
