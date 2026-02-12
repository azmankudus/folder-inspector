package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.plugin.Database;
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
  public int launch(Database database, Scanner scanner, Formatter formatter, Writer writer, Notifier notifier,
      dev.ayam.folderinspector.core.plugin.Scheduler scheduler, String path) {
    try {
      if (scheduler != null) {
        scheduler.start();
      }
    } catch (Exception e) {
      notifier.notifyError("Failed to start scheduler: " + e.getMessage());
      return 1;
    }

    try (ApplicationContext context = Micronaut.run(RestLauncher.class)) {
      ScannerContext scannerContext = context.getBean(ScannerContext.class);
      scannerContext.setScanner(scanner);
      scannerContext.setRootPath(Paths.get(path));

      DatabaseContext databaseContext = context.getBean(DatabaseContext.class);
      databaseContext.setDatabase(database);

      SchedulerContext schedulerContext = context.getBean(SchedulerContext.class);
      schedulerContext.setScheduler(scheduler);

      // Keep the application running
      Thread.currentThread().join();
      return 0;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return 1;
    } finally {
      if (scheduler != null) {
        try {
          scheduler.shutdown();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

  @Override
  public String getName() {
    return "rest";
  }
}
