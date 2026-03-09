package backend.service;

import backend.model.*;
import backend.repository.*;
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
import java.time.LocalDateTime;
import java.util.*;
import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core service for executing file scans over SMB.
 * Implements iterative directory traversal to handle deep structures.
 * Requirement: Iterative traversal, LDAP failover, and constant memory usage.
 */
@Singleton
public class ScanService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanService.class);

    private final ConnectionProfileRepository connectionRepo;
    private final ScanHistoryRepository scanHistoryRepo;
    private final FileNodeRepository fileNodeRepo;
    private final FileAclRepository fileAclRepo;
    private final DirectoryProfileRepository directoryProfileRepo;
    private final AdUserCacheRepository adUserCacheRepo;
    private final ScanProfileRepository scanProfileRepo;
    private final DatabaseSchemaService dbSchemaService;
    private final ConcurrentHashMap<String, String> sidCache = new ConcurrentHashMap<>();
    
    @io.micronaut.context.annotation.Value("${application.dev.scan.delay-ms:0}")
    private long scanDelayMs;

    @io.micronaut.context.annotation.Value("${application.scan.log.found.size:100}")
    private int logFoundSize;

    public ScanService(ConnectionProfileRepository connectionRepo,
                       ScanHistoryRepository scanHistoryRepo,
                       FileNodeRepository fileNodeRepo,
                       FileAclRepository fileAclRepo,
                       DirectoryProfileRepository directoryProfileRepo,
                       AdUserCacheRepository adUserCacheRepo,
                       ScanProfileRepository scanProfileRepo,
                       DatabaseSchemaService dbSchemaService) {
        this.connectionRepo = connectionRepo;
        this.scanHistoryRepo = scanHistoryRepo;
        this.fileNodeRepo = fileNodeRepo;
        this.fileAclRepo = fileAclRepo;
        this.directoryProfileRepo = directoryProfileRepo;
        this.adUserCacheRepo = adUserCacheRepo;
        this.scanProfileRepo = scanProfileRepo;
        this.dbSchemaService = dbSchemaService;
    }

    public ScanHistory startScan(ScanProfile scanProfile) {
        LOG.trace("Invoking startScan for profile ID: {}", scanProfile.id());
        ScanHistory history = new ScanHistory(null, scanProfile.id(), 0, LocalDateTime.now(), null, "IN_PROGRESS", null, null);
        history = scanHistoryRepo.save(history);
        final Long historyId = history.id();
        try {
            long pid = JobLauncher.launchScanJob(scanProfile.id(), historyId);
            String logPath = "jobs/logs/backend_scan_" + historyId + ".log";
            scanHistoryRepo.update(new ScanHistory(historyId, scanProfile.id(), 0, history.startTime(), null, "IN_PROGRESS", pid, logPath));
        } catch (Exception e) {
            LOG.error("Failed to start scan process", e);
            scanHistoryRepo.update(new ScanHistory(historyId, scanProfile.id(), 0, LocalDateTime.now(), LocalDateTime.now(), "FAILED", null, null));
        }
        return history;
    }

    public void stopScan(Long scanProfileId) {
        LOG.trace("Invoking stopScan for profile ID: {}", scanProfileId);
        scanHistoryRepo.findAll().forEach(history -> {
            if (history.scanProfileId().equals(scanProfileId) && "IN_PROGRESS".equals(history.status())) {
                scanHistoryRepo.update(new ScanHistory(history.id(), history.scanProfileId(), history.progress(), history.startTime(), LocalDateTime.now(), "STOP_REQUESTED", history.processId(), history.logPath()));
            }
        });
    }

    @Transactional
    public void runScanStandalone(Long profileId, Long historyId) {
        try {
            ScanHistory hist = scanHistoryRepo.findById(historyId).orElseThrow();
            ScanProfile profile = scanProfileRepo.findById(profileId).orElseThrow();
            ConnectionProfile conn = connectionRepo.findById(profile.connectionProfileId()).orElseThrow();
            
            dbSchemaService.ensurePartitionExists(profileId);
            dbSchemaService.truncatePartition(profileId);
            
            SMBClient client = new SMBClient();
            
            try (Connection connection = client.connect(conn.host(), conn.port())) {
                AuthenticationContext ac = new AuthenticationContext(conn.username(), conn.password().toCharArray(), conn.host());
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
                
                DirectoryProfile dirProfile = directoryProfileRepo.findAll().iterator().hasNext() ? directoryProfileRepo.findAll().iterator().next() : null;
                DirContext dirContext = null;
                if (dirProfile != null) {
                    dirContext = connectToLdap(dirProfile.adHost(), dirProfile.adPort(), dirProfile.adUsername(), dirProfile.adPassword());
                    
                    if (dirContext == null && dirProfile.adBackupHost() != null) {
                        LOG.info("Primary LDAP failed, trying backup: {}", dirProfile.adBackupHost());
                        dirContext = connectToLdap(dirProfile.adBackupHost(), dirProfile.adBackupPort(), dirProfile.adUsername(), dirProfile.adPassword());
                    }
                }
                
                try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
                    iterativeTraversing(share, folderPath, historyId, dirContext, profile);
                    scanHistoryRepo.update(new ScanHistory(historyId, profile.id(), 100, hist.startTime(), LocalDateTime.now(), "COMPLETED", hist.processId(), hist.logPath()));
                } finally {
                    if (dirContext != null) {
                        try {
                            dirContext.close();
                        } catch (Exception e) {}
                    }
                }
            } catch (Exception e) {
                if ("SCAN_STOPPED".equals(e.getMessage())) {
                    LOG.info("Scan stopped gracefully by user request.");
                    scanHistoryRepo.update(new ScanHistory(historyId, profile.id(), 0, hist.startTime(), LocalDateTime.now(), "STOPPED", hist.processId(), hist.logPath()));
                } else {
                    LOG.error("Scan error running smbj", e);
                    scanHistoryRepo.update(new ScanHistory(historyId, profile.id(), 0, hist.startTime(), LocalDateTime.now(), "FAILED", hist.processId(), hist.logPath()));
                }
            } finally {
                client.close();
            }
        } catch (Exception e) {
             scanHistoryRepo.update(new ScanHistory(historyId, profileId, 0, LocalDateTime.now(), LocalDateTime.now(), "FAILED", null, null));
             LOG.error("Standalone scan execution failed", e);
        }
    }

    private DirContext connectToLdap(String host, Integer port, String user, String pass) {
        if (host == null || host.isEmpty()) return null;
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
        } catch (javax.naming.AuthenticationException e) {
            LOG.error("LDAP [{}]: Authentication Failed: {}", host, e.getMessage());
        } catch (javax.naming.CommunicationException e) {
            LOG.error("LDAP [{}]: Communication Failed: {}", host, e.getMessage());
        } catch (Exception e) {
            LOG.error("LDAP [{}]: Unexpected Failure: {}", host, e.getMessage());
        }
        return null;
    }

    private static class TraversalNode {
        final String path;
        final Long parentId;

        TraversalNode(String path, Long parentId) {
            this.path = path;
            this.parentId = parentId;
        }
    }

    private void iterativeTraversing(DiskShare share, String rootPath, Long historyId, DirContext dirContext, ScanProfile profile) {
        Deque<TraversalNode> stack = new ArrayDeque<>();
        stack.push(new TraversalNode(rootPath, null));
        long fileCount = 0;

        while (!stack.isEmpty()) {
            TraversalNode current = stack.pop();

            if (fileCount++ % 100 == 0) {
                ScanHistory hist = scanHistoryRepo.findById(historyId).orElse(null);
                if (hist == null || "STOP_REQUESTED".equals(hist.status()) || "STOPPED".equals(hist.status())) {
                    throw new RuntimeException("SCAN_STOPPED");
                }
            }

            try {
                List<FileIdBothDirectoryInformation> files = share.list(current.path);
                for (FileIdBothDirectoryInformation info : files) {
                    String fileName = info.getFileName();
                    if (".".equals(fileName) || "..".equals(fileName)) continue;

                    if (scanDelayMs > 0) {
                        try { Thread.sleep(scanDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }

                    String fullPath = current.path.isEmpty() ? fileName : current.path + "\\" + fileName;
                    long attributes = info.getFileAttributes();
                    boolean isDirectory = (attributes & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0;
                    String nodeType = isDirectory ? "FOLDER" : "FILE";

                    String ownerSid = null;
                    String ownerName = null;
                    String groupSid = null;
                    String groupName = null;
                    SecurityDescriptor sd = null;
                    
                    try {
                        sd = share.getSecurityInfo(fullPath, EnumSet.of(SecurityInformation.OWNER_SECURITY_INFORMATION, SecurityInformation.GROUP_SECURITY_INFORMATION, SecurityInformation.DACL_SECURITY_INFORMATION));
                        if (sd.getOwnerSid() != null) {
                            ownerSid = sd.getOwnerSid().toString();
                            ownerName = resolveSid(ownerSid, dirContext);
                        }
                        if (sd.getGroupSid() != null) {
                            groupSid = sd.getGroupSid().toString();
                            groupName = resolveSid(groupSid, dirContext);
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to read ACL for {}: {}", fullPath, e.getMessage());
                    }

                    boolean hasExplicitAcl = false;
                    List<FileAcl> aclsToSave = new ArrayList<>();
                    if (sd != null && sd.getDacl() != null) {
                        for (ACE ace : sd.getDacl().getAces()) {
                            String sidStr = ace.getSid().toString();
                            String resolvedName = resolveSid(sidStr, dirContext);
                            String userOrGroup = resolvedName != null ? resolvedName : sidStr;
                            String inheritanceType = ace.getAceHeader().getAceFlags().isEmpty() ? "DIRECT" : "INHERITED";
                            if ("DIRECT".equals(inheritanceType)) hasExplicitAcl = true;
                            
                            boolean canView = (ace.getAccessMask() & 0x01) != 0 || (ace.getAccessMask() & 0x80) != 0;
                            boolean canAdd = (ace.getAccessMask() & 0x02) != 0 || (ace.getAccessMask() & 0x04) != 0;
                            boolean canEdit = (ace.getAccessMask() & 0x02) != 0 || (ace.getAccessMask() & 0x40) != 0;
                            boolean canRemove = (ace.getAccessMask() & 0x010000) != 0 || (ace.getAccessMask() & 0x40) != 0;
                            
                            aclsToSave.add(new FileAcl(null, null, userOrGroup, inheritanceType, canView, canAdd, canEdit, canRemove, profile.id()));
                        }
                    }

                    if (!(ScanMode.AUDIT.equals(profile.scanMode()) && !isDirectory && !hasExplicitAcl)) {
                        FileNode node = new FileNode(null, historyId, current.parentId, fileName, fullPath, nodeType, info.getEndOfFile(), LocalDateTime.now(), ownerSid, ownerName, groupSid, groupName, profile.id());
                        final FileNode savedNode = fileNodeRepo.save(node);
                        
                        for (FileAcl acl : aclsToSave) {
                            fileAclRepo.save(new FileAcl(null, savedNode.id(), acl.userOrGroup(), acl.inheritanceType(), acl.canView(), acl.canAdd(), acl.canEdit(), acl.canRemove(), profile.id()));
                        }

                        if (isDirectory) {
                            stack.push(new TraversalNode(fullPath, savedNode.id()));
                        }
                    } else if (isDirectory) {
                         stack.push(new TraversalNode(fullPath, null));
                    }

                    if (logFoundSize > 0 && fileCount % logFoundSize == 0) {
                         LOG.info("Scan Progress [History ID {}]: Found {} items currently processing {}", historyId, fileCount, fullPath);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Cannot read directory {}: {}", current.path, e.getMessage());
            }
        }
    }

    private String resolveSid(String sid, DirContext ctx) {
        if (sid == null) return null;
        return sidCache.computeIfAbsent(sid, s -> {
            var cached = adUserCacheRepo.findById(s);
            if (cached.isPresent()) return cached.get().resolutionName();
            
            if (ctx != null) {
                try {
                    Attributes attrs = ctx.getAttributes("<SID=" + s + ">", new String[]{"displayName", "name", "cn", "sAMAccountName"});
                    String resolved = null;
                    if (attrs.get("displayName") != null) resolved = attrs.get("displayName").get().toString();
                    else if (attrs.get("name") != null) resolved = attrs.get("name").get().toString();
                    else if (attrs.get("cn") != null) resolved = attrs.get("cn").get().toString();
                    else if (attrs.get("sAMAccountName") != null) resolved = attrs.get("sAMAccountName").get().toString();
                    
                    if (resolved != null) {
                        adUserCacheRepo.save(new AdUserCache(s, resolved));
                        return resolved;
                    }
                } catch (Exception e) {
                    LOG.warn("SID resolution failed for {}: {}", s, e.getMessage());
                }
            }
            
            adUserCacheRepo.save(new AdUserCache(s, s));
            return s;
        });
    }
}
