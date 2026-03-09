package backend.controller;

import backend.model.FileNode;
import backend.model.FileAcl;
import backend.repository.FileNodeRepository;
import backend.repository.FileAclRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.security.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.core.io.Writable;
import io.micronaut.http.HttpHeaders;
import java.io.IOException;
import backend.service.ExportService;

/**
 * Controller for exploring file structures and exporting discovery data.
 * Supports paginated views and streaming exports.
 */
@Controller("/files")
public class FileExplorerController {
    private static final Logger LOG = LoggerFactory.getLogger(FileExplorerController.class);

    private final FileNodeRepository fileRepo;
    private final FileAclRepository aclRepo;
    private final SecurityService securityService;
    private final ExportService exportService;

    public FileExplorerController(FileNodeRepository fileRepo,
                                  FileAclRepository aclRepo,
                                  SecurityService securityService,
                                  ExportService exportService) {
        this.fileRepo = fileRepo;
        this.aclRepo = aclRepo;
        this.securityService = securityService;
        this.exportService = exportService;
    }

    @Secured({"FILE_EXPLORER_VIEW_ALL", "FILE_EXPLORER_VIEW_RESTRICTED"})
    @Get("/{historyId}{?pageable*}")
    public Page<FileNode> getFiles(Long historyId, Pageable pageable) {
        if (pageable == null) pageable = Pageable.from(0, 20);
        LOG.trace("Accessing files for history ID: {} (pageable: {})", historyId, pageable);
        Authentication auth = securityService.getAuthentication().orElse(null);
        
        if (auth == null) {
            LOG.warn("Unauthenticated access attempt to file history ID: {}", historyId);
            return Page.empty();
        }

        boolean canViewAll = auth.getRoles().contains("FILE_EXPLORER_VIEW_ALL");
        LOG.debug("User {} viewing history ID: {} (canViewAll: {})", auth.getName(), historyId, canViewAll);
        
        if (canViewAll) {
            return fileRepo.findByScanHistoryId(historyId, pageable);
        } else {
            return fileRepo.findByScanHistoryIdWithPermissionOptions(historyId, auth.getName(), pageable);
        }
    }

    @Secured({"FILE_EXPLORER_VIEW_ALL", "FILE_EXPLORER_VIEW_RESTRICTED"})
    @Get("/{historyId}/export/{format}")
    public HttpResponse<?> exportFiles(
            Long historyId, 
            String format,
            @QueryValue(defaultValue = "all") String scope,
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size) throws IOException {
        
        LOG.info("Exporting files for history ID: {} (format: {}, scope: {})", historyId, format, scope);
        Authentication auth = securityService.getAuthentication().orElse(null);
        if (auth == null) throw new io.micronaut.http.exceptions.HttpStatusException(io.micronaut.http.HttpStatus.UNAUTHORIZED, "Unauthorized");

        boolean canViewAll = auth.getRoles().contains("FILE_EXPLORER_VIEW_ALL");
        java.util.stream.Stream<FileNode> nodes;

        if ("page".equalsIgnoreCase(scope)) {
            Pageable pageable = Pageable.from(page, size);
            nodes = canViewAll ? 
                    fileRepo.findByScanHistoryId(historyId, pageable).getContent().stream() : 
                    fileRepo.findByScanHistoryIdWithPermissionOptions(historyId, auth.getName(), pageable).getContent().stream();
        } else {
            nodes = canViewAll ? 
                    fileRepo.findByScanHistoryId(historyId) : 
                    fileRepo.findByScanHistoryIdWithPermissionOptions(historyId, auth.getName());
        }

        // We still need to collect IDs to fetch ACLMap efficiently in one go.
        // For truly huge datasets, we'd need a different strategy, but nodeIds are relatively small.
        java.util.List<FileNode> nodeList = nodes.collect(java.util.stream.Collectors.toList());
        
        java.util.List<Long> nodeIds = nodeList.stream()
                .map(FileNode::id)
                .collect(java.util.stream.Collectors.toList());
        
        java.util.Map<Long, java.util.List<FileAcl>> aclMap = new java.util.HashMap<>();
        if (!nodeIds.isEmpty()) {
            java.util.stream.StreamSupport.stream(aclRepo.findByFileNodeIdIn(nodeIds).spliterator(), false)
                    .forEach(acl -> aclMap.computeIfAbsent(acl.fileNodeId(), k -> new java.util.ArrayList<>()).add(acl));
        }

        String contentType;
        String extension;
        if ("xlsx".equalsIgnoreCase(format)) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extension = "xlsx";
        } else {
            contentType = "text/csv";
            extension = "csv";
        }

        Writable writable = new Writable() {
            @Override
            public void writeTo(java.io.Writer out) throws IOException {
                // Default implementation calls writeTo(OutputStream)
            }
            @Override
            public void writeTo(java.io.OutputStream out, java.nio.charset.Charset charset) throws IOException {
                if ("xlsx".equalsIgnoreCase(format)) {
                    exportService.generateXlsx(nodeList, aclMap, out);
                } else {
                    exportService.generateCsv(nodeList, aclMap, out);
                }
            }
        };

        String filename = "export_" + historyId + "_" + System.currentTimeMillis() + "." + extension;
        
        return HttpResponse.ok(writable)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.of(contentType));
    }

    @Secured({"FILE_EXPLORER_VIEW_ALL", "FILE_EXPLORER_VIEW_RESTRICTED"})
    @Get("/{fileId}/acl")
    public Iterable<FileAcl> getAcls(Long fileId) {
        LOG.trace("Fetching ACLs for file ID: {}", fileId);
        return aclRepo.findByFileNodeId(fileId);
    }
}
