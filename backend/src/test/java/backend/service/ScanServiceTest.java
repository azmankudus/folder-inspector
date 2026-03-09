package backend.service;

import backend.model.ScanMode;
import backend.model.ScanProfile;
import backend.model.ScanHistory;
import backend.repository.ScanHistoryRepository;
import backend.repository.ScanProfileRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

@MicronautTest
class ScanServiceTest {

    @Inject
    ScanService scanService;

    @Inject
    ScanHistoryRepository scanHistoryRepo;

    @Inject
    ScanProfileRepository scanProfileRepo;

    @Test
    void testStartScanHistoryCreation() {
        ScanProfile profile = new ScanProfile(null, 1L, "C:\\Test", "0 0 0 * * ?", ScanMode.FULL);
        profile = scanProfileRepo.save(profile);
        
        ScanHistory history = scanService.startScan(profile);
        
        Assertions.assertNotNull(history.id());
        Assertions.assertEquals("IN_PROGRESS", history.status());
        
        // Clean up
        scanHistoryRepo.delete(history);
        scanProfileRepo.delete(profile);
    }
}
