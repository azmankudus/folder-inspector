package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.plugin.Database;
import jakarta.inject.Singleton;

@Singleton
public class DatabaseContext {
  private Database database;

  public void setDatabase(Database database) {
    this.database = database;
  }

  public Database getDatabase() {
    return database;
  }
}
