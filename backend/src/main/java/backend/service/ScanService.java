package backend.service;

import backend.model.DirectoryCache;
import backend.model.DirectoryConfig;
import backend.model.Item;
import backend.model.ItemAcl;
import backend.model.Job;
import backend.model.JobException;
import backend.model.JobLog;
import backend.model.ScanConfig;
import backend.model.ServerConfig;
import backend.repository.DirectoryCacheRepository;
import backend.repository.DirectoryConfigRepository;
import backend.repository.ItemAclRepository;
import backend.repository.ItemRepository;
import backend.repository.JobExceptionRepository;
import backend.repository.JobLogRepository;
import backend.repository.JobRepository;
import backend.repository.ScanConfigRepository;
import backend.repository.ServerConfigRepository;
import backend.util.JobLauncher;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.msdtyp.SecurityInformation;
import com.hierynomus.msdtyp.SecurityDescriptor;
import com.hierynomus.msdtyp.ace.ACE;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.FileAttributes;

import java.time.OffsetDateTime;
import java.util.*;
import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micronaut.context.annotation.Value;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.StreamSupport;

/**
* Core service for executing file scans over SMB.
* Implements iterative directory traversal to handle deep structures.
* Requirement: Iterative traversal, LDAP failover, and constant memory usage.
*/
@Singleton
public class ScanService {

  private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);

  private final ServerConfigRepository connectionRepo;
  private final JobRepository scanHistoryRepo;
  private final ItemRepository fileNodeRepo;
  private final ItemAclRepository fileAclRepo;
  private final DirectoryConfigRepository directoryProfileRepo;
  private final DirectoryCacheRepository adUserCacheRepo;
  private final ScanConfigRepository scanProfileRepo;
  private final DatabaseSchemaService dbSchemaService;
  private final JobLogRepository scanLogRepo;
  private final JobExceptionRepository scanExceptionRepo;
  private final ConcurrentHashMap<String, String> sidCache = new ConcurrentHashMap<>();

  @Value("${application.dev.scan.delay-ms:0}")
  private long scanDelayMs;

  @Value("${application.scan.log.found.size:100}")
  private int logFoundSize;

  public ScanService(ServerConfigRepository connectionRepo,
      JobRepository scanHistoryRepo,
      ItemRepository fileNodeRepo,
      ItemAclRepository fileAclRepo,
      DirectoryConfigRepository directoryProfileRepo,
      DirectoryCacheRepository adUserCacheRepo,
      ScanConfigRepository scanProfileRepo,
      DatabaseSchemaService dbSchemaService,
      JobLogRepository scanLogRepo,
      JobExceptionRepository scanExceptionRepo) {
    this.connectionRepo = connectionRepo;
    this.scanHistoryRepo = scanHistoryRepo;
    this.fileNodeRepo = fileNodeRepo;
    this.fileAclRepo = fileAclRepo;
    this.directoryProfileRepo = directoryProfileRepo;
    this.adUserCacheRepo = adUserCacheRepo;
    this.scanProfileRepo = scanProfileRepo;
    this.dbSchemaService = dbSchemaService;
    this.scanLogRepo = scanLogRepo;
    this.scanExceptionRepo = scanExceptionRepo;
  }

  public Job startScan(ScanConfig scanProfile) {
    LOG.trace("Invoking startScan for profile ID: {}", scanProfile.id());
    Job history = new Job(null, scanProfile.id(), 0, OffsetDateTime.now(), null, "IN_PROGRESS", null,
        null, null, null, null);
    history = scanHistoryRepo.save(history);
    final Long historyId = history.id();
    try {
      long pid = JobLauncher.launchScanJob(scanProfile.id(), historyId);
      String logPath = "jobs/logs/backend_scan_" + historyId + ".log";
      scanHistoryRepo.update(new Job(historyId, scanProfile.id(), 0, history.startTime(), null,
          "IN_PROGRESS", pid, logPath, null, null, null));
    } catch (Exception e) {
      LOG.error("Failed to start scan process", e);
      scanHistoryRepo.update(new Job(historyId, scanProfile.id(), 0, OffsetDateTime.now(),
          OffsetDateTime.now(), "FAILED", null, null, null, null, null));
    }
    return history;
  }

  public void stopScan(Long scanProfileId) {
    LOG.trace("Invoking stopScan for profile ID: {}", scanProfileId);
    scanHistoryRepo.findAll().forEach(history -> {
      if (history.scanConfigId().equals(scanProfileId) && "IN_PROGRESS".equals(history.status())) {
        scanHistoryRepo.update(new Job(history.id(), history.scanConfigId(), history.progress(),
            history.startTime(), OffsetDateTime.now(), "STOP_REQUESTED", history.processId(),
            history.logPath(), history.totalFiles(), history.totalFolders(), history.totalExceptions()));
      }
    });
  }

  @Transactional
  public void runScanStandalone(Long profileId, Long historyId) {
    try {
      Job hist = scanHistoryRepo.findById(historyId).orElseThrow();
      ScanConfig profile = scanProfileRepo.findById(profileId).orElseThrow();
      ServerConfig conn = connectionRepo.findById(profile.serverConfigId()).orElseThrow();

      // Pre-scan Cleanup: Drop partitions for previous partial/failed scans of this profile
      scanHistoryRepo.findByScanConfigId(profileId).stream()
          .filter(h -> !"COMPLETED".equals(h.status()) && !h.id().equals(historyId))
          .forEach(h -> dbSchemaService.dropHistoryPartition(h.id()));

      dbSchemaService.ensurePartitionExists(historyId);

      SMBClient client = new SMBClient();

      try (Connection connection = client.connect(conn.host(), conn.port())) {
        AuthenticationContext ac = new AuthenticationContext(conn.username(), conn.password().toCharArray(),
            conn.host());
        Session session = connection.authenticate(ac);

        String shareName = profile.rootPath();
        String folderPath = "";

        if (shareName.contains("/")) {
          int idx = shareName.indexOf('/');
          folderPath = shareName.substring(idx + 1);
          shareName = shareName.substring(0, idx);
        } else if (shareName.contains("\\")) {
          int idx = shareName.indexOf('\\');
          folderPath = shareName.substring(idx + 1);
          shareName = shareName.substring(0, idx);
        }

        if (folderPath.startsWith("\\") || folderPath.startsWith("/")) {
          folderPath = folderPath.substring(1);
        }

        DirectoryConfig dirProfile = directoryProfileRepo.findAll().iterator().hasNext()
            ? directoryProfileRepo.findAll().iterator().next()
            : null;
        DirContext dirContext = null;
        if (dirProfile != null) {
          dirContext = connectToLdap(dirProfile.adHost(), dirProfile.adPort(), dirProfile.adUsername(),
              dirProfile.adPassword());

          if (dirContext == null && dirProfile.adBackupHost() != null) {
            LOG.info("Primary LDAP failed, trying backup: {}", dirProfile.adBackupHost());
            dirContext = connectToLdap(dirProfile.adBackupHost(), dirProfile.adBackupPort(),
                dirProfile.adUsername(), dirProfile.adPassword());
          }
        }

        try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
          dbLog(historyId, "INFO", "Connected to share: " + shareName + " - Parallel scan starting...");
          ScanStats stats = new ScanStats();

          BlockingQueue<DbBatchItem> dbQueue = new LinkedBlockingQueue<>(10000);
          CountDownLatch batchDone = new CountDownLatch(1);

          // Background DB Batch Writer
          Thread.ofVirtual().start(() -> {
            List<DbBatchItem> batch = new ArrayList<>();
            try {
              while (true) {
                DbBatchItem item = dbQueue.poll(500, TimeUnit.MILLISECONDS);
                if (item != null) {
                  batch.add(item);
                }

                if (batch.size() >= 500 || (item == null && !batch.isEmpty())) {
                  List<Item> nodesToSave = batch.stream().map(b -> b.node).toList();
                  Iterable<Item> saved = fileNodeRepo.saveAll(nodesToSave);
                  List<Item> savedList = StreamSupport.stream(saved.spliterator(), false)
                      .toList();

                  List<ItemAcl> aclsToSave = new ArrayList<>();
                  for (int i = 0; i < savedList.size(); i++) {
                    final Long nodeId = savedList.get(i).id();
                    for (ItemAcl acl : batch.get(i).acls) {
                      aclsToSave.add(new ItemAcl(null, nodeId, acl.principal(),
                          acl.inheritanceType(), acl.canView(), acl.canAdd(), acl.canEdit(),
                          acl.canRemove()));
                    }
                  }
                  if (!aclsToSave.isEmpty()) {
                    fileAclRepo.saveAll(aclsToSave);
                  }
                  batch.clear();
                }

                if (item == null && batchDone.getCount() == 0 && dbQueue.isEmpty())
                  break;
              }
            } catch (Exception e) {
              LOG.error("Batch DB Writer error", e);
            } finally {
              batchDone.countDown();
            }
          });

          parallelTraversing(share, folderPath, historyId, dirContext, profile, stats, dbQueue);
          batchDone.countDown();

          // Wait for remaining batch to be flushed
          batchDone.await(30, TimeUnit.SECONDS);

          scanHistoryRepo.update(new Job(historyId, profile.id(), 100, hist.startTime(),
              OffsetDateTime.now(), "COMPLETED", hist.processId(), hist.logPath(), stats.files.get(),
              stats.folders.get(), stats.exceptions.get()));
          dbLog(historyId, "INFO", "Scan completed. Files: " + stats.files.get() + ", Folders: "
              + stats.folders.get() + ", Exceptions: " + stats.exceptions.get());

          // Post-scan Cleanup: Delete data partitions for all older scans of this profile
          scanHistoryRepo.findByScanConfigId(profileId).stream()
              .filter(h -> !h.id().equals(historyId))
              .forEach(h -> dbSchemaService.dropHistoryPartition(h.id()));
        } finally {
          if (dirContext != null) {
            try {
              dirContext.close();
            } catch (Exception e) {
            }
          }
        }
      } catch (Exception e) {
        if ("SCAN_STOPPED".equals(e.getMessage())) {
          LOG.info("Scan stopped gracefully by user request.");
          scanHistoryRepo.update(new Job(historyId, profile.id(), 0, hist.startTime(),
              OffsetDateTime.now(), "STOPPED", hist.processId(), hist.logPath(), null, null, null));
        } else {
          LOG.error("Scan error running smbj", e);
          dbLog(historyId, "ERROR", "Scan failed: " + e.getMessage());
          dbException(historyId, "ERROR", profile.rootPath(), "Scan execution failed", e.getMessage(), null);
          scanHistoryRepo.update(new Job(historyId, profile.id(), 0, hist.startTime(),
              OffsetDateTime.now(), "FAILED", hist.processId(), hist.logPath(), null, null, null));
        }
      } finally {
        client.close();
      }
    } catch (Exception e) {
      scanHistoryRepo.update(new Job(historyId, profileId, 0, OffsetDateTime.now(), OffsetDateTime.now(),
          "FAILED", null, null, null, null, null));
      LOG.error("Standalone scan execution failed", e);
    }
  }

  private DirContext connectToLdap(String host, Integer port, String user, String pass) {
    if (host == null || host.isEmpty())
      return null;
    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + (port != null ? port : 389));
    if (user != null && !user.isEmpty()) {
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, user);
      env.put(Context.SECURITY_CREDENTIALS, pass);
    }
    try {
      return new InitialDirContext(env);
    } catch (AuthenticationException e) {
      LOG.error("LDAP [{}]: Authentication Failed: {}", host, e.getMessage());
    } catch (CommunicationException e) {
      LOG.error("LDAP [{}]: Communication Failed: {}", host, e.getMessage());
    } catch (Exception e) {
      LOG.error("LDAP [{}]: Unexpected Failure: {}", host, e.getMessage());
    }
    return null;
  }

  private static class ScanStats {
    final AtomicLong files = new AtomicLong(0);
    final AtomicLong folders = new AtomicLong(0);
    final AtomicLong exceptions = new AtomicLong(0);
  }

  private static class DbBatchItem {
    final Item node;
    final List<ItemAcl> acls;

    DbBatchItem(Item node, List<ItemAcl> acls) {
      this.node = node;
      this.acls = acls;
    }
  }

  private void parallelTraversing(DiskShare share, String rootPath, Long historyId, DirContext dirContext,
      ScanConfig profile, ScanStats stats, BlockingQueue<DbBatchItem> dbQueue) {
    Phaser phaser = new Phaser(1);
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    try {
      scanDirectoryRecursive(share, rootPath, null, null, null, historyId, dirContext, profile, stats, phaser,
          executor, dbQueue);
      phaser.arriveAndAwaitAdvance();
    } finally {
      executor.shutdown();
    }
  }

  private void scanDirectoryRecursive(DiskShare share, String path, Long parentId, String currentOwner,
      String parentPath, Long historyId, DirContext dirContext, ScanConfig profile, ScanStats stats,
      Phaser phaser, ExecutorService executor, BlockingQueue<DbBatchItem> dbQueue) {
    try {
      // Check for stop request periodically
      if (stats.files.get() % 100 == 0) {
        Job hist = scanHistoryRepo.findById(historyId).orElse(null);
        if (hist == null || "STOP_REQUESTED".equals(hist.status()) || "STOPPED".equals(hist.status())) {
          return;
        }
      }

      List<FileIdBothDirectoryInformation> items = share.list(path);
      for (FileIdBothDirectoryInformation info : items) {
        String fileName = info.getFileName();
        if (".".equals(fileName) || "..".equals(fileName))
          continue;

        // Simulate Network Latency
        if (scanDelayMs > 0) {
          try {
            Thread.sleep(scanDelayMs);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }

        String fullPath = path.isEmpty() ? fileName : path + "\\" + fileName;
        boolean isDirectory = (info.getFileAttributes()
            & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0;
        String nodeType = isDirectory ? "FOLDER" : "FILE";

        if (isDirectory)
          stats.folders.incrementAndGet();
        else
          stats.files.incrementAndGet();

        String ownerSid = null;
        String ownerName = null;
        String groupSid = null;
        String groupName = null;
        SecurityDescriptor sd = null;

        try {
          sd = share.getSecurityInfo(fullPath,
              EnumSet.of(SecurityInformation.OWNER_SECURITY_INFORMATION,
                  SecurityInformation.GROUP_SECURITY_INFORMATION,
                  SecurityInformation.DACL_SECURITY_INFORMATION));
          if (sd.getOwnerSid() != null) {
            ownerSid = sd.getOwnerSid().toString();
            ownerName = resolveSid(ownerSid, dirContext);
          }
          if (sd.getGroupSid() != null) {
            groupSid = sd.getGroupSid().toString();
            groupName = resolveSid(groupSid, dirContext);
          }
        } catch (Exception e) {
          LOG.warn("ACL read failed for {}: {}", fullPath, e.getMessage());
          String note = String.format("Parent directory %s owned by %s", path,
              currentOwner != null ? currentOwner : "Unknown");
          dbException(historyId, "WARN", fullPath, "Reading ACL Failed", e.getMessage(), note);
          stats.exceptions.incrementAndGet();
        }

        List<ItemAcl> acls = new ArrayList<>();
        if (sd != null && sd.getDacl() != null) {
          for (ACE ace : sd.getDacl().getAces()) {
            String sidStr = ace.getSid().toString();
            String resolvedName = resolveSid(sidStr, dirContext);
            String userOrGroup = resolvedName != null ? resolvedName : sidStr;
            String inheritanceType = ace.getAceHeader().getAceFlags().isEmpty() ? "DIRECT" : "INHERITED";

            boolean canView = (ace.getAccessMask() & 0x01) != 0 || (ace.getAccessMask() & 0x80) != 0;
            boolean canAdd = (ace.getAccessMask() & 0x02) != 0 || (ace.getAccessMask() & 0x04) != 0;
            boolean canEdit = (ace.getAccessMask() & 0x02) != 0 || (ace.getAccessMask() & 0x40) != 0;
            boolean canRemove = (ace.getAccessMask() & 0x010000) != 0 || (ace.getAccessMask() & 0x40) != 0;

            acls.add(new ItemAcl(null, null, userOrGroup, inheritanceType, canView, canAdd, canEdit,
                canRemove));
          }
        }

        Item node = new Item(null, historyId, parentId, fileName, fullPath, nodeType,
            info.getEndOfFile(), OffsetDateTime.now(), ownerSid, ownerName, groupSid, groupName,
            profile.id());

        try {
          dbQueue.put(new DbBatchItem(node, acls));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        if (isDirectory) {
          final String nextPath = fullPath;
          final String nextOwner = ownerName;
          phaser.register();
          executor.submit(() -> {
            try {
              scanDirectoryRecursive(share, nextPath, null, nextOwner, path, historyId, dirContext,
                  profile, stats, phaser, executor, dbQueue);
            } finally {
              phaser.arriveAndDeregister();
            }
          });
        }
      }
    } catch (Exception e) {
      LOG.warn("List failed for {}: {}", path, e.getMessage());
      String note = String.format("Parent directory %s owned by %s", parentPath != null ? parentPath : "Root",
          currentOwner != null ? currentOwner : "Unknown");
      dbException(historyId, "ERROR", path, "Directory Listing Failed", e.getMessage(), note);
      stats.exceptions.incrementAndGet();
    } finally {
      // Root phaser decrement is handled in the caller loop or by arriveAndAwaitAdvance
    }
  }

  private String resolveSid(String sid, DirContext ctx) {
    if (sid == null)
      return null;
    return sidCache.computeIfAbsent(sid, s -> {
      var cached = adUserCacheRepo.findById(s);
      if (cached.isPresent())
        return cached.get().resolutionName();

      if (ctx != null) {
        try {
          Attributes attrs = ctx.getAttributes("<SID=" + s + ">",
              new String[] { "displayName", "name", "cn", "sAMAccountName" });
          String resolved = null;
          if (attrs.get("displayName") != null)
            resolved = attrs.get("displayName").get().toString();
          else if (attrs.get("name") != null)
            resolved = attrs.get("name").get().toString();
          else if (attrs.get("cn") != null)
            resolved = attrs.get("cn").get().toString();
          else if (attrs.get("sAMAccountName") != null)
            resolved = attrs.get("sAMAccountName").get().toString();

          if (resolved != null) {
            adUserCacheRepo.save(new DirectoryCache(s, resolved));
            return resolved;
          }
        } catch (Exception e) {
          LOG.warn("SID resolution failed for {}: {}", s, e.getMessage());
        }
      }

      adUserCacheRepo.save(new DirectoryCache(s, s));
      return s;
    });
  }

  private void dbLog(Long historyId, String level, String message) {
    try {
      scanLogRepo.save(new JobLog(null, historyId, level, message, OffsetDateTime.now()));
    } catch (Exception e) {
      LOG.error("Failed to save ScanLog to DB", e);
    }
  }

  private void dbException(Long historyId, String level, String path, String message, String reason, String note) {
    String cleanedReason = reason;
    if (cleanedReason != null
        && (cleanedReason.contains("Create failed for") || cleanedReason.contains("Create failed "))) {
      // Specifically strip SMBSatus style "Create failed for \path\to\file with status STATUS_..."
      int idx = cleanedReason.indexOf("Create failed");
      if (idx != -1) {
        cleanedReason = cleanedReason.substring(0, idx).trim();
        if (cleanedReason.isEmpty()) {
          cleanedReason = reason; // Fallback if it was JUST "Create failed..."
          cleanedReason = cleanedReason.replaceFirst("(?i)Create failed (for )?.*?( with status |$)", "")
              .trim();
        }
      }
    }
    // Simpler catch-all as requested: remove "Create failed ..."
    if (cleanedReason != null && cleanedReason.contains("Create failed")) {
      cleanedReason = cleanedReason.split("Create failed")[0].trim();
      if (cleanedReason.isEmpty())
        cleanedReason = reason.replace("Create failed", "").trim();
    }

    try {
      scanExceptionRepo.save(
          new JobException(null, historyId, level, path, message, cleanedReason, OffsetDateTime.now(), note));
    } catch (Exception e) {
      LOG.error("Failed to save ScanException to DB", e);
    }
  }
}
