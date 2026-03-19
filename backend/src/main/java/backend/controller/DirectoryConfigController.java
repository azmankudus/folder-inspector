package backend.controller;

import backend.model.DirectoryConfig;
import backend.repository.DirectoryConfigRepository;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/config/directory")
public class DirectoryConfigController {
  private static final Logger LOG = LoggerFactory.getLogger(DirectoryConfigController.class);
  private final DirectoryConfigRepository directoryConfigRepo;

  public DirectoryConfigController(DirectoryConfigRepository directoryConfigRepo) {
    this.directoryConfigRepo = directoryConfigRepo;
  }

  @Secured({ "API_DIRECTORYCONFIG_READ" })
  @Get
  public Iterable<DirectoryConfig> getDirectoryConfigs() {
    LOG.trace("Listing directory profiles");
    return directoryConfigRepo.findAll();
  }

  @Secured({ "API_DIRECTORYCONFIG_CREATE" })
  @Post
  public DirectoryConfig addDirectoryConfig(@Body DirectoryConfig profile) {
    LOG.debug("Adding new directory profile: {}", profile.adHost());
    return directoryConfigRepo.save(profile);
  }

  @Secured({ "API_DIRECTORYCONFIG_UPDATE" })
  @Put("/{id}")
  public DirectoryConfig updateDirectoryConfig(Long id, @Body DirectoryConfig p) {
    LOG.debug("Updating directory profile ID: {}", id);
    return directoryConfigRepo
        .update(new DirectoryConfig(id, p.adHost(), p.adPort(), p.adBackupHost(), p.adBackupPort(),
            p.adUsername(), p.adPassword()));
  }

  @Secured({ "API_DIRECTORYCONFIG_DELETE" })
  @Delete("/{id}")
  public void deleteDirectoryConfig(Long id) {
    LOG.debug("Deleting directory profile ID: {}", id);
    directoryConfigRepo.deleteById(id);
  }
}
