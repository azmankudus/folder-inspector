package backend.controller;

import backend.model.DirectoryProfile;
import backend.repository.DirectoryProfileRepository;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/directories")
public class DirectoryProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryProfileController.class);
    private final DirectoryProfileRepository directoryRepo;

    public DirectoryProfileController(DirectoryProfileRepository directoryRepo) {
        this.directoryRepo = directoryRepo;
    }

    @Secured({"AD_READ"})
    @Get
    public Iterable<DirectoryProfile> getDirectories() {
        LOG.trace("Listing directory profiles");
        return directoryRepo.findAll();
    }

    @Secured({"AD_CREATE"})
    @Post
    public DirectoryProfile addDirectory(@Body DirectoryProfile profile) {
        LOG.debug("Adding new directory profile: {}", profile.adHost());
        return directoryRepo.save(profile);
    }

    @Secured({"AD_UPDATE"})
    @Put("/{id}")
    public DirectoryProfile updateDirectory(Long id, @Body DirectoryProfile p) {
        LOG.debug("Updating directory profile ID: {}", id);
        return directoryRepo.update(new DirectoryProfile(id, p.adHost(), p.adPort(), p.adUsername(), p.adPassword()));
    }

    @Secured({"AD_DELETE"})
    @Delete("/{id}")
    public void deleteDirectory(Long id) {
        LOG.debug("Deleting directory profile ID: {}", id);
        directoryRepo.deleteById(id);
    }
}
