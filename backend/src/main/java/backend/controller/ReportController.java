package backend.controller;

import backend.dto.ItemReportDTO;
import backend.model.*;
import backend.repository.*;
import backend.service.ExportService;
import io.micronaut.core.io.Writable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Controller("/report")
public class ReportController {
    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    private final ItemRepository itemRepo;
    private final ItemAclRepository itemAclRepo;
    private final JobRepository jobRepo;
    private final ScanConfigRepository scanConfigRepo;
    private final JobLogRepository jobLogRepo;
    private final JobExceptionRepository jobExceptionRepo;
    private final ServerConfigRepository serverConfigRepo;
    private final SecurityService securityService;
    private final ExportService exportService;

    public ReportController(ItemRepository itemRepo,
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
        this.jobRepo = jobRepo;
        this.scanConfigRepo = scanConfigRepo;
        this.jobLogRepo = jobLogRepo;
        this.jobExceptionRepo = jobExceptionRepo;
        this.serverConfigRepo = serverConfigRepo;
        this.securityService = securityService;
        this.exportService = exportService;
    }

    @Secured({ "API_REPORT_ALL", "API_REPORT_RESTRICTED" })
    @Get("/{jobId}/items")
    public Page<ItemReportDTO> getReportItems(Long jobId, Pageable pageable, @QueryValue(defaultValue = "false") boolean showAll) {
        if (pageable == null) pageable = Pageable.from(0, 20);
        Authentication auth = securityService.getAuthentication().orElse(null);
        if (auth == null) return Page.empty();

        boolean userToggleAll = showAll && auth.getRoles().contains("API_REPORT_ALL");
        Page<Item> itemPage = userToggleAll ? itemRepo.findByJobId(jobId, pageable)
                : itemRepo.findByJobIdWithPermissionOptions(jobId, auth.getName(), pageable);

        List<Long> itemIds = itemPage.getContent().stream().map(Item::id).collect(Collectors.toList());
        Map<Long, List<ItemAcl>> aclMap = new HashMap<>();
        if (!itemIds.isEmpty()) {
            itemAclRepo.findByItemIdIn(itemIds).forEach(acl -> aclMap.computeIfAbsent(acl.itemId(), k -> new ArrayList<>()).add(acl));
        }

        return itemPage.map(item -> new ItemReportDTO(
                item.id(), item.name(), item.path(), item.nodeType(), item.sizeBytes(),
                item.lastModified(), item.ownerName(), item.groupName(),
                aclMap.getOrDefault(item.id(), List.of())));
    }

    @Secured({ "API_REPORT_ALL", "API_REPORT_RESTRICTED" })
    @Get("/{jobId}/export/{format}")
    public HttpResponse<?> exportReport(
            Long jobId,
            String format,
            @QueryValue(defaultValue = "all") String scope,
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size,
            @QueryValue(defaultValue = "true") boolean includeData,
            @QueryValue(defaultValue = "false") boolean showAll) throws IOException {

        LOG.info("Exporting report for job ID: {} (format: {}, scope: {})", jobId, format, scope);
        Authentication auth = securityService.getAuthentication().orElse(null);
        if (auth == null)
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        boolean userToggleAll = showAll && auth.getRoles().contains("API_REPORT_ALL");
        Stream<Item> nodes;

        if ("page".equalsIgnoreCase(scope)) {
            Pageable pageable = Pageable.from(page, size);
            nodes = userToggleAll ? itemRepo.findByJobId(jobId, pageable).getContent().stream()
                    : itemRepo.findByJobIdWithPermissionOptions(jobId, auth.getName(), pageable)
                    .getContent().stream();
        } else {
            nodes = userToggleAll ? StreamSupport.stream(itemRepo.findByJobId(jobId).spliterator(), false)
                    : itemRepo.findByJobIdWithPermissionOptions(jobId, auth.getName());
        }

        List<Item> nodeList = nodes.collect(Collectors.toList());
        List<Long> nodeIds = nodeList.stream().map(Item::id).collect(Collectors.toList());

        Map<Long, List<ItemAcl>> aclMap = new HashMap<>();
        if (includeData && !nodeIds.isEmpty()) {
            StreamSupport.stream(itemAclRepo.findByItemIdIn(nodeIds).spliterator(), false)
                    .forEach(acl -> aclMap.computeIfAbsent(acl.itemId(), k -> new ArrayList<>()).add(acl));
        }

        Job job = jobRepo.findById(jobId).orElse(null);
        ScanConfig scanConfig = job != null ? scanConfigRepo.findById(job.scanConfigId()).orElse(null) : null;
        ServerConfig serverConfig = (scanConfig != null && scanConfig.serverConfigId() != null)
                ? serverConfigRepo.findById(scanConfig.serverConfigId()).orElse(null)
                : null;
        List<JobLog> logs = jobLogRepo.findByJobIdOrderByTimestampAsc(jobId);
        List<JobException> exceptions = jobExceptionRepo.findByJobId(jobId);

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
            public void writeTo(Writer out) throws IOException {}

            @Override
            public void writeTo(OutputStream out, Charset charset) throws IOException {
                if ("xlsx".equalsIgnoreCase(format)) {
                    exportService.generateXlsxWithReport(job, scanConfig, serverConfig, logs, exceptions,
                            includeData ? nodeList : null, aclMap, out);
                } else {
                    exportService.generateCsv(nodeList, aclMap, out);
                }
            }
        };

        String filename = "report_" + jobId + "_" + System.currentTimeMillis() + "." + extension;

        return HttpResponse.ok(writable)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.of(contentType));
    }
}
