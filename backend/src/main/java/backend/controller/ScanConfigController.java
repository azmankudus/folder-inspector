package backend.controller;

import backend.model.ScanConfig;
import backend.repository.ScanConfigRepository;
import backend.repository.JobRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/config/scan")
public class ScanConfigController {
  private static final Logger LOG = LoggerFactory.getLogger(ScanConfigController.class);
  private final ScanConfigRepository scanConfigRepo;
  private final JobRepository jobRepo;

  public ScanConfigController(ScanConfigRepository scanConfigRepo, JobRepository jobRepo) {
    this.scanConfigRepo = scanConfigRepo;
    this.jobRepo = jobRepo;
  }

  @Secured({ "API_SCANCONFIG_READ" })
  @Get
  public Iterable<ScanConfig> getScanConfigs() {
    LOG.trace("Listing all scan profiles");
    return scanConfigRepo.findAll();
  }

  @Secured({ "API_SCANCONFIG_CREATE" })
  @Post
  public ScanConfig addScanConfig(@Body ScanConfig profile) {
    LOG.debug("Creating new scan profile for path: {}", profile.rootPath());
    return scanConfigRepo.save(profile);
  }

  @Secured({ "API_SCANCONFIG_UPDATE" })
  @Put("/{id}")
  public ScanConfig updateScanConfig(Long id, @Body ScanConfig p) {
    LOG.debug("Updating scan profile ID: {}", id);
    if (jobRepo.existsByScanConfigIdAndStatus(id, "IN_PROGRESS")) {
      LOG.warn("Update blocked for scan profile ID: {} - active scan running", id);
      throw new HttpStatusException(HttpStatus.BAD_REQUEST,
          "Cannot update scan profile while a scan is running.");
    }
    return scanConfigRepo
        .update(new ScanConfig(id, p.serverConfigId(), p.rootPath(), p.schedulerCron()));
  }

  @Secured({ "API_SCANCONFIG_DELETE" })
  @Delete("/{id}")
  public void deleteScanConfig(Long id) {
    LOG.debug("Deleting scan profile ID: {}", id);
    if (jobRepo.existsByScanConfigIdAndStatus(id, "IN_PROGRESS")) {
      LOG.warn("Delete blocked for scan profile ID: {} - active scan running", id);
      throw new HttpStatusException(HttpStatus.BAD_REQUEST,
          "Cannot delete scan profile while a scan is running.");
    }
    scanConfigRepo.deleteById(id);
  }
}
