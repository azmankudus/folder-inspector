package backend.controller;

import backend.model.*;
import backend.repository.*;
import backend.service.ScanService;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for managing scan jobs, including starting, stopping, and retrieving history.
 */
@Controller("/jobs")
public class ScanJobController {
    private static final Logger LOG = LoggerFactory.getLogger(ScanJobController.class);

    private final ScanProfileRepository scanRepo;
    private final ScanHistoryRepository historyRepo;
    private final ScanService scanService;

    public ScanJobController(ScanProfileRepository scanRepo,
                             ScanHistoryRepository historyRepo,
                             ScanService scanService) {
        this.scanRepo = scanRepo;
        this.historyRepo = historyRepo;
        this.scanService = scanService;
    }

    @Secured({"SCAN_START"})
    @Post("/{id}/start")
    public ScanHistory startScan(Long id) {
        LOG.info("Request to start scan for profile ID: {}", id);
        ScanProfile profile = scanRepo.findById(id).orElseThrow();
        return scanService.startScan(profile);
    }

    @Secured({"SCAN_STOP"})
    @Post("/{id}/stop")
    public void stopScan(Long id) {
        LOG.info("Request to stop scan for profile ID: {}", id);
        scanService.stopScan(id);
    }

    @Secured({"SCAN_HISTORY_READ"})
    @Get("/history")
    public Iterable<ScanHistory> getHistories() {
        LOG.trace("Listing scan history");
        return historyRepo.findAll();
    }

    @Secured({"SCAN_HISTORY_READ"})
    @Get("/{historyId}/logs")
    public String getLogs(Long historyId) {
        ScanHistory hist = historyRepo.findById(historyId).orElseThrow();
        String path = hist.logPath();
        if (path == null) return "logs deleted";
        java.io.File file = new java.io.File(path);
        if (!file.exists()) return "logs deleted";
        try {
            return java.nio.file.Files.readString(file.toPath());
        } catch (java.io.IOException e) {
            LOG.error("Failed to read log file {}: {}", path, e.getMessage());
            return "logs deleted"; // As per user requirement, or could be error msg
        }
    }
}
