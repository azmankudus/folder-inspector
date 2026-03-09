package backend.service;

import backend.model.ScanHistory;
import backend.repository.ScanHistoryRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StartupInit {
    
    private static final Logger LOG = LoggerFactory.getLogger(StartupInit.class);

    private final ScanHistoryRepository scanHistoryRepository;

    public StartupInit(ScanHistoryRepository scanHistoryRepository) {
        this.scanHistoryRepository = scanHistoryRepository;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        // Reconcile stuck scans
        for (ScanHistory hist : scanHistoryRepository.findAll()) {
            if ("IN_PROGRESS".equals(hist.status())) {
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
                    scanHistoryRepository.update(new ScanHistory(
                        hist.id(), hist.scanProfileId(), hist.progress(),
                        hist.startTime(), java.time.LocalDateTime.now(), "FAILED",
                        hist.processId(), hist.logPath()
                    ));
                }
            }
        }
    }
}
