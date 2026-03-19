package backend.controller;

import backend.model.ServerConfig;
import backend.repository.ServerConfigRepository;
import backend.repository.JobRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/config/server")
public class ServerConfigController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerConfigController.class);
    private final ServerConfigRepository connectionRepo;
    private final JobRepository scanHistoryRepo;

    public ServerConfigController(ServerConfigRepository connectionRepo,
            JobRepository scanHistoryRepo) {
        this.connectionRepo = connectionRepo;
        this.scanHistoryRepo = scanHistoryRepo;
    }

    @Secured({ "API_SERVERCONFIG_READ" })
    @Get
    public Iterable<ServerConfig> getConnections() {
        LOG.trace("Listing all connection profiles");
        return connectionRepo.findAll();
    }

    @Secured({ "API_SERVERCONFIG_CREATE" })
    @Post
    public ServerConfig addConnection(@Body ServerConfig profile) {
        LOG.debug("Creating new connection profile: {}", profile.host());
        return connectionRepo.save(profile);
    }

    @Secured({ "API_SERVERCONFIG_UPDATE" })
    @Put("/{id}")
    public ServerConfig updateConnection(Long id, @Body ServerConfig p) {
        LOG.debug("Updating connection profile ID: {}", id);
        if (scanHistoryRepo.existsByServerConfigIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Update blocked for connection profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot update connection profile while a related scan is running.");
        }
        return connectionRepo
                .update(new ServerConfig(id, p.protocol(), p.host(), p.port(), p.username(), p.password()));
    }

    @Secured({ "API_SERVERCONFIG_DELETE" })
    @Delete("/{id}")
    public void deleteConnection(Long id) {
        LOG.debug("Deleting connection profile ID: {}", id);
        if (scanHistoryRepo.existsByServerConfigIdAndStatus(id, "IN_PROGRESS")) {
            LOG.warn("Delete blocked for connection profile ID: {} - active scan running", id);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete connection profile while a related scan is running.");
        }
        connectionRepo.deleteById(id);
    }
}
