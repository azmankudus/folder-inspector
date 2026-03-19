package backend.controller;

import backend.model.Item;
import backend.model.ItemAcl;
import backend.dto.ItemReportDTO;
import backend.model.Job;
import backend.model.JobException;
import backend.model.JobLog;
import backend.model.ScanConfig;
import backend.model.ServerConfig;
import backend.repository.ItemAclRepository;
import backend.repository.ItemRepository;
import backend.repository.JobExceptionRepository;
import backend.repository.JobLogRepository;
import backend.repository.JobRepository;
import backend.repository.ScanConfigRepository;
import backend.repository.ServerConfigRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.security.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import backend.service.ExportService;
import io.micronaut.http.annotation.QueryValue;

/**
* Controller for exploring file structures and exporting discovery data.
* Supports paginated views and streaming exports.
*/
@Controller("/explorer")
public class ExplorerController {
  private static final Logger LOG = LoggerFactory.getLogger(ExplorerController.class);

  private final ItemRepository itemRepo;
  private final ItemAclRepository itemAclRepo;
  private final SecurityService securityService;

  public ExplorerController(ItemRepository itemRepo,
      ItemAclRepository itemAclRepo,
      JobRepository jobRepo,
      ScanConfigRepository scanConfigRepo,
      JobLogRepository jobLogRepo,
      JobExceptionRepository jobExceptionRepo,
      ServerConfigRepository serverConfigRepo,
      SecurityService securityService,
      ExportService exportService) {
    this.itemRepo = itemRepo;
    this.itemAclRepo = itemAclRepo;
    this.securityService = securityService;
  }

  @Secured({ "API_EXPLORER_ALL", "API_EXPLORER_RESTRICTED" })
  @Get("/{jobId}{?showAll}")
  public Page<Item> getFiles(Long jobId, Pageable pageable, @QueryValue(defaultValue = "false") boolean showAll) {
    if (pageable == null)
      pageable = Pageable.from(0, 20);
    LOG.trace("Accessing files for history ID: {} (pageable: {})", jobId, pageable);
    Authentication auth = securityService.getAuthentication().orElse(null);

    if (auth == null) {
      LOG.warn("Unauthenticated access attempt to file history ID: {}", jobId);
      return Page.empty();
    }

    boolean userToggleAll = showAll && auth.getRoles().contains("API_EXPLORER_ALL");
    LOG.debug("Explorer access - historyId: {}, showAll: {}, hasPermission: {}", jobId, showAll, userToggleAll);

    if (userToggleAll) {
      return itemRepo.findByJobId(jobId, pageable);
    } else {
      return itemRepo.findByJobIdWithPermissionOptions(jobId, auth.getName(), pageable);
    }
  }

  @Secured({ "API_EXPLORER_ALL", "API_EXPLORER_RESTRICTED" })
  @Get("/{fileId}/acl")
  public Iterable<ItemAcl> getAcls(Long fileId) {
    LOG.trace("Fetching ACLs for file ID: {}", fileId);
    return itemAclRepo.findByItemId(fileId);
  }
}
