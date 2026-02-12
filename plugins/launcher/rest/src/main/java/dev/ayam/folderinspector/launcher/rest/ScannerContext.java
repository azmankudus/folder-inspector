package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.plugin.Scanner;
import jakarta.inject.Singleton;
import java.nio.file.Path;

@Singleton
public class ScannerContext {
  private Scanner scanner;
  private Path rootPath;

  public void setScanner(Scanner scanner) {
    this.scanner = scanner;
  }

  public Scanner getScanner() {
    return scanner;
  }

  public void setRootPath(Path rootPath) {
    this.rootPath = rootPath;
  }

  public Path getRootPath() {
    return rootPath;
  }
}
