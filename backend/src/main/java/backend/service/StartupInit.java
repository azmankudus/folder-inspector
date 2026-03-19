package backend.service;

import backend.model.Job;
import backend.repository.JobRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.OffsetDateTime;

@Singleton
public class StartupInit {

  private static final Logger LOG = LoggerFactory.getLogger(StartupInit.class);

  private final JobRepository scanHistoryRepository;

  public StartupInit(JobRepository scanHistoryRepository) {
    this.scanHistoryRepository = scanHistoryRepository;
  }

  @EventListener
  public void onStartup(StartupEvent event) {
    // Reconcile stuck scans
    for (Job hist : scanHistoryRepository.findByStatus("IN_PROGRESS")) {
      boolean alive = false;
      if (hist.processId() != null) {
        var handleOpt = ProcessHandle.of(hist.processId());
        if (handleOpt.isPresent()) {
          ProcessHandle handle = handleOpt.get();
          String cmd = handle.info().commandLine().orElse("");
          // the command will typically include 'java' and 'backend.Application'
          if (cmd.contains("java") || cmd.isEmpty()) {
            alive = true; // Still alive, or we lack privileges to read its command (assume alive)
          }
        }
      }
      if (!alive) {
        LOG.info("Reconciling dead scan history ID: {}", hist.id());
        scanHistoryRepository.update(new Job(
            hist.id(), hist.scanConfigId(), hist.progress(),
            hist.startTime(), OffsetDateTime.now(), "FAILED",
            hist.processId(), hist.logPath(),
            hist.totalFiles(), hist.totalFolders(), hist.totalExceptions()));
      }
    }
  }
}
