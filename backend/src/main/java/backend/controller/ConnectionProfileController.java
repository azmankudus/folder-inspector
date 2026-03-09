package backend.controller;

import backend.model.ConnectionProfile;
import backend.repository.ConnectionProfileRepository;
import backend.repository.ScanHistoryRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/connections")
public class ConnectionProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionProfileController.class);
    private final ConnectionProfileRepository connectionRepo;
    private final ScanHistoryRepository scanHistoryRepo;

    public ConnectionProfileController(ConnectionProfileRepository connectionRepo, ScanHistoryRepository scanHistoryRepo) {
        this.connectionRepo = connectionRepo;
        this.scanHistoryRepo = scanHistoryRepo;
    }

    @Secured({"CONN_READ"})
    @Get
    public Iterable<ConnectionProfile> getConnections() {
        LOG.trace("Listing all connection profiles");
        return connectionRepo.findAll();
    }

    @Secured({"CONN_CREATE"})
    @Post
    public ConnectionProfile addConnection(@Body ConnectionProfile profile) {
        LOG.debug("Creating new connection profile: {}", profile.host());
        return connectionRepo.save(profile);
    }

    @Secured({"CONN_UPDATE"})
    @Put("/{id}")
    public ConnectionProfile updateConnection(Long id, @Body ConnectionProfile p) {
        LOG.debug("Updating connection profile ID: {}", id);
        if (scanHistoryRepo.existsByConnectionProfileIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Update blocked for connection profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Cannot update connection profile while a related scan is running.");
        }
        return connectionRepo.update(new ConnectionProfile(id, p.protocol(), p.host(), p.port(), p.username(), p.password()));
    }

    @Secured({"CONN_DELETE"})
    @Delete("/{id}")
    public void deleteConnection(Long id) {
        LOG.debug("Deleting connection profile ID: {}", id);
        if (scanHistoryRepo.existsByConnectionProfileIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Delete blocked for connection profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Cannot delete connection profile while a related scan is running.");
        }
        connectionRepo.deleteById(id);
    }
}
