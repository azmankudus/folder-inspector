package dev.ayam.folderinspector.launcher.rest;

import dev.ayam.folderinspector.core.plugin.Scheduler;
import jakarta.inject.Singleton;

@Singleton
public class SchedulerContext {
  private Scheduler scheduler;

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }
}
