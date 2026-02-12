package dev.ayam.folderinspector.launcher.web;

import dev.ayam.folderinspector.core.plugin.Scanner;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import java.nio.file.Path;

/**
 * Holds scanner context for web requests.
 */
@Singleton
@Bean
public class ScannerContext {
  private Scanner scanner;
  private Path rootPath;

  public Scanner getScanner() {
    return scanner;
  }

  public void setScanner(Scanner scanner) {
    this.scanner = scanner;
  }

  public Path getRootPath() {
    return rootPath;
  }

  public void setRootPath(Path rootPath) {
    this.rootPath = rootPath;
  }
}
