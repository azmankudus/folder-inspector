package backend.service;

import backend.model.ScanMode;
import backend.model.ScanConfig;
import backend.model.Job;
import backend.repository.JobRepository;
import backend.repository.ScanConfigRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

@MicronautTest
class ScanServiceTest {

    @Inject
    ScanService scanService;

    @Inject
    JobRepository scanHistoryRepo;

    @Inject
    ScanConfigRepository scanProfileRepo;

    @Test
    void testStartScanHistoryCreation() {
        ScanConfig profile = new ScanConfig(null, 1L, "C:\\Test", "0 0 0 * * ?", ScanMode.FULL);
        profile = scanProfileRepo.save(profile);

        Job history = scanService.startScan(profile);

        Assertions.assertNotNull(history.id());
        Assertions.assertEquals("IN_PROGRESS", history.status());

        // Clean up
        scanHistoryRepo.delete(history);
        scanProfileRepo.delete(profile);
    }
}
