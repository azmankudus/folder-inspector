package backend.controller;

import backend.model.ScanProfile;
import backend.repository.ScanProfileRepository;
import backend.repository.ScanHistoryRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/scans")
public class ScanProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(ScanProfileController.class);
    private final ScanProfileRepository scanRepo;
    private final ScanHistoryRepository scanHistoryRepo;

    public ScanProfileController(ScanProfileRepository scanRepo, ScanHistoryRepository scanHistoryRepo) {
        this.scanRepo = scanRepo;
        this.scanHistoryRepo = scanHistoryRepo;
    }

    @Secured({"SCAN_READ"})
    @Get
    public Iterable<ScanProfile> getScans() {
        LOG.trace("Listing all scan profiles");
        return scanRepo.findAll();
    }

    @Secured({"SCAN_CREATE"})
    @Post
    public ScanProfile addScan(@Body ScanProfile profile) {
        LOG.debug("Creating new scan profile for path: {}", profile.rootPath());
        return scanRepo.save(profile);
    }

    @Secured({"SCAN_UPDATE"})
    @Put("/{id}")
    public ScanProfile updateScan(Long id, @Body ScanProfile p) {
        LOG.debug("Updating scan profile ID: {}", id);
        if (scanHistoryRepo.existsByScanProfileIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Update blocked for scan profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Cannot update scan profile while a scan is running.");
        }
        return scanRepo.update(new ScanProfile(id, p.connectionProfileId(), p.rootPath(), p.schedulerCron(), p.scanMode()));
    }

    @Secured({"SCAN_DELETE"})
    @Delete("/{id}")
    public void deleteScan(Long id) {
        LOG.debug("Deleting scan profile ID: {}", id);
        if (scanHistoryRepo.existsByScanProfileIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Delete blocked for scan profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Cannot delete scan profile while a scan is running.");
        }
        scanRepo.deleteById(id);
    }
}
