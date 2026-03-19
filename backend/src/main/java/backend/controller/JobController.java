package backend.controller;

import backend.dto.JobDTO;
import backend.model.Job;
import backend.model.JobException;
import backend.model.JobLog;
import backend.model.ScanConfig;
import backend.repository.JobExceptionRepository;
import backend.repository.JobLogRepository;
import backend.repository.JobRepository;
import backend.repository.ScanConfigRepository;
import backend.repository.ServerConfigRepository;
import backend.service.DatabaseSchemaService;
import backend.service.ScanService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
* Controller for managing scan jobs, including starting, stopping, and retrieving history.
*/
@Controller("/job")
public class JobController {
  private static final Logger LOG = LoggerFactory.getLogger(JobController.class);

  private final ScanConfigRepository scanConfigRepo;
  private final JobRepository jobRepo;
  private final JobLogRepository jobLogRepo;
  private final JobExceptionRepository jobExceptionRepo;
  private final ServerConfigRepository serverConfigRepo;
  private final ScanService scanService;

  public JobController(ScanConfigRepository scanConfigRepo,
      JobRepository jobRepo,
      JobLogRepository jobLogRepo,
      JobExceptionRepository jobExceptionRepo,
      DatabaseSchemaService dbSchemaService,
      ServerConfigRepository serverConfigRepo,
      ScanService scanService) {
    this.scanConfigRepo = scanConfigRepo;
    this.jobRepo = jobRepo;
    this.jobLogRepo = jobLogRepo;
    this.jobExceptionRepo = jobExceptionRepo;
    this.serverConfigRepo = serverConfigRepo;
    this.scanService = scanService;
  }

  @Secured({ "API_JOB_READ" })
  @Get
  public Iterable<JobDTO> getJobs() {
    LOG.trace("Listing scan jobs with profile details");
    List<JobDTO> dtos = new java.util.ArrayList<>();
    jobRepo.findAll().forEach(job -> {
      String host = "N/A";
      String path = "N/A";
      String user = "N/A";
      if (job.scanConfigId() != null) {
        var profile = scanConfigRepo.findById(job.scanConfigId());
        if (profile.isPresent()) {
          path = profile.get().rootPath();
          if (profile.get().serverConfigId() != null) {
            var conn = serverConfigRepo.findById(profile.get().serverConfigId());
            if (conn.isPresent()) {
              host = conn.get().host();
              user = conn.get().username() != null ? conn.get().username() : "N/A";
            }
          }
        }
      }
      dtos.add(new JobDTO(
          job.id(), job.scanConfigId(), job.progress(), job.startTime(), job.finishTime(),
          job.status(), job.totalFiles(), job.totalFolders(), job.totalExceptions(), host, path, user, job.logPath()));
    });
    return dtos;
  }

  @Secured({ "API_JOB_READ" })
  @Get("/{jobId}")
  public HttpResponse<JobDTO> getJob(Long jobId) {
    return jobRepo.findById(jobId).map(job -> {
      String host = "N/A";
      String path = "N/A";
      String user = "N/A";
      if (job.scanConfigId() != null) {
        var scanConfig = scanConfigRepo.findById(job.scanConfigId());
        if (scanConfig.isPresent()) {
          path = scanConfig.get().rootPath();
          if (scanConfig.get().serverConfigId() != null) {
            var serverConfig = serverConfigRepo.findById(scanConfig.get().serverConfigId());
            if (serverConfig.isPresent()) {
              host = serverConfig.get().host();
              user = serverConfig.get().username() != null ? serverConfig.get().username() : "N/A";
            }
          }
        }
      }
      return HttpResponse.ok(new JobDTO(
          job.id(), job.scanConfigId(), job.progress(), job.startTime(), job.finishTime(),
          job.status(), job.totalFiles(), job.totalFolders(), job.totalExceptions(), host, path, user, job.logPath()));
    }).orElse(HttpResponse.notFound());
  }

  @Secured({ "API_JOB_READ" })
  @Get("/{jobId}/logs")
  public Iterable<JobLog> getDbLogs(Long jobId) {
    return jobLogRepo.findByJobIdOrderByTimestampAsc(jobId);
  }

  @Secured({ "API_JOB_READ" })
  @Get("/{jobId}/exceptions")
  public Iterable<JobException> getExceptions(Long jobId) {
    return jobExceptionRepo.findByJobId(jobId);
  }

  @Secured({ "API_JOB_READ" })
  @Get(value = "/{jobId}/log-file", produces = MediaType.TEXT_PLAIN)
  public HttpResponse<String> getLogFile(Long jobId) {
    return jobRepo.findById(jobId)
        .map(job -> {
          if (job.logPath() == null || job.logPath().isBlank()) {
            return HttpResponse.<String>notFound("No log file path recorded for this job.");
          }
          Path logPath = Paths.get(job.logPath());
          if (!Files.exists(logPath)) {
            return HttpResponse.<String>notFound("Log file not found: " + job.logPath());
          }
          try {
            String content = Files.readString(logPath);
            return HttpResponse.ok(content);
          } catch (IOException e) {
            LOG.error("Failed to read log file for job {}", jobId, e);
            return HttpResponse.<String>serverError("Failed to read log file: " + e.getMessage());
          }
        })
        .orElse(HttpResponse.notFound("Job not found."));
  }

  @Secured({ "API_JOB_START" })
  @Post("/{id}/start")
  public Job startScan(Long id) {
    LOG.info("Request to start scan for profile ID: {}", id);
    ScanConfig scanConfig = scanConfigRepo.findById(id).orElseThrow();
    return scanService.startScan(scanConfig);
  }

  @Secured({ "API_JOB_STOP" })
  @Post("/{id}/stop")
  public void stopScan(Long id) {
    LOG.info("Request to stop scan for profile ID: {}", id);
    scanService.stopScan(id);
  }
}
