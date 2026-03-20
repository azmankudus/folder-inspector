package backend.service;

import backend.dto.DashboardDTO;
import backend.model.*;
import backend.repository.*;
import javax.sql.DataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import jakarta.transaction.Transactional;

@Singleton
public class DashboardService {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    private final JobRepository jobRepo;
    private final ScanConfigRepository scanConfigRepo;
    private final ServerConfigRepository serverConfigRepo;
    private final UserJobStatRepository userJobStatRepo;
    private final DataSource dataSource;

    public DashboardService(
            JobRepository jobRepo,
            ScanConfigRepository scanConfigRepo,
            ServerConfigRepository serverConfigRepo,
            UserJobStatRepository userJobStatRepo,
            DataSource dataSource) {
        this.jobRepo = jobRepo;
        this.scanConfigRepo = scanConfigRepo;
        this.serverConfigRepo = serverConfigRepo;
        this.userJobStatRepo = userJobStatRepo;
        this.dataSource = dataSource;
    }

    @Transactional
    public DashboardDTO getDashboard(String username) {
        try (Connection connection = dataSource.getConnection()) {
                // Get all latest completed jobs
                List<Long> latestJobIds = getLatestJobIds();
                
                List<DashboardDTO.ProfileCard> cards = getProfileCards(connection, latestJobIds, username);
                DashboardDTO.UserSummary userSummary = getUserSummary(latestJobIds, username);
                
                return new DashboardDTO(userSummary, cards);
            } catch (Exception e) {
                LOG.error("Failed to generate dashboard", e);
                return new DashboardDTO(new DashboardDTO.UserSummary(0, 0, 0, 0), new ArrayList<>());
            }
    }

    private List<Long> getLatestJobIds() {
        List<Long> jobs = new ArrayList<>();
        scanConfigRepo.findAll().forEach(config -> {
            jobRepo.findTopByScanConfigIdOrderByFinishTimeDesc(config.id())
                   .ifPresent(job -> jobs.add(job.id()));
        });
        return jobs;
    }

    private DashboardDTO.UserSummary getUserSummary(List<Long> jobIds, String username) {
        long totalOwned = 0, totalRead = 0, totalWrite = 0, totalExecute = 0;
        
        List<UserJobStat> stats = userJobStatRepo.findByJobIdInAndUsername(jobIds, username);
        for (UserJobStat stat : stats) {
            totalOwned += stat.ownedFiles() != null ? stat.ownedFiles() : 0;
            totalRead += stat.totalRead() != null ? stat.totalRead() : 0;
            totalWrite += stat.totalWrite() != null ? stat.totalWrite() : 0;
            totalExecute += stat.totalExecute() != null ? stat.totalExecute() : 0;
        }
        
        return new DashboardDTO.UserSummary(totalOwned, totalRead, totalWrite, totalExecute);
    }

    private List<DashboardDTO.ProfileCard> getProfileCards(Connection conn, List<Long> jobIds, String username) throws SQLException {
        List<DashboardDTO.ProfileCard> cards = new ArrayList<>();
        if (jobIds.isEmpty()) return cards;
        
        for (Long jobId : jobIds) {
            Job job = jobRepo.findById(jobId).orElse(null);
            if (job == null) continue;
            ScanConfig scan = scanConfigRepo.findById(job.scanConfigId()).orElse(null);
            if (scan == null) continue;
            ServerConfig server = serverConfigRepo.findById(scan.serverConfigId()).orElse(null);
            if (server == null) continue;
            
            String title = "smb://" + server.host() + "/" + scan.rootPath();
            
            UserJobStat stat = userJobStatRepo.findByJobIdAndUsername(jobId, username).orElse(null);
            if (stat == null) {
                stat = computeAndCacheStat(conn, job, username);
            }
            if (stat != null) {
                cards.add(new DashboardDTO.ProfileCard(scan.id(), title, stat.totalFiles(), stat.totalFolders(), stat.totalRead(), stat.totalWrite(), stat.totalExecute()));
            }
        }
        
        return cards;
    }

    @Transactional
    public List<DashboardDTO.ProfileHistoryItem> getDashboardHistory(String username, Long scanConfigId) {
        List<DashboardDTO.ProfileHistoryItem> history = new ArrayList<>();
        List<Job> jobs = jobRepo.findByScanConfigId(scanConfigId);
        
        // Only consider completed jobs
        jobs = jobs.stream().filter(j -> "COMPLETED".equals(j.status()))
                   .sorted(Comparator.comparing(Job::finishTime))
                   .toList();
                   
        try (Connection conn = dataSource.getConnection()) {
            for (Job job : jobs) {
                UserJobStat stat = userJobStatRepo.findByJobIdAndUsername(job.id(), username).orElse(null);
                if (stat == null) {
                    stat = computeAndCacheStat(conn, job, username);
                }
                
                if (stat != null && job.finishTime() != null) {
                    history.add(new DashboardDTO.ProfileHistoryItem(
                        job.finishTime().toString(),
                        stat.totalFiles(),
                        stat.totalFolders(),
                        stat.totalRead(),
                        stat.totalWrite(),
                        stat.totalExecute()
                    ));
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to generate dashboard history", e);
        }
        
        return history;
    }

    private UserJobStat computeAndCacheStat(Connection conn, Job job, String username) throws SQLException {
        long files = 0, folders = 0, reads = 0, writes = 0, executes = 0;
        long jobId = job.id();
        
        // Total explicit files/folders the user has access to on this profile
        String itemSql = "SELECT COUNT(DISTINCT i.id), " +
                         "SUM(CASE WHEN i.node_type = 'FILE' THEN 1 ELSE 0 END), " +
                         "SUM(CASE WHEN i.node_type = 'FOLDER' THEN 1 ELSE 0 END) " +
                         "FROM tb_item i " +
                         "WHERE i.job_id = ? " +
                         "AND i.id IN (" +
                         "  SELECT id FROM tb_item WHERE job_id = ? AND owner_name = ? " +
                         "  UNION " +
                         "  SELECT item_id FROM tb_item_acl WHERE principal = ?" +
                         ")";
        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            ps.setLong(1, jobId);
            ps.setLong(2, jobId);
            ps.setString(3, username);
            ps.setString(4, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    files = rs.getLong(2);
                    folders = rs.getLong(3);
                }
            }
        }
        
        String aclSql = "SELECT a.can_view, a.can_add, a.can_edit, a.can_remove, i.node_type " +
                        "FROM tb_item i JOIN tb_item_acl a ON i.id = a.item_id " +
                        "WHERE i.job_id = ? AND a.principal = ?";
        try (PreparedStatement ps = conn.prepareStatement(aclSql)) {
            ps.setLong(1, jobId);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean canView = rs.getBoolean("can_view");
                    boolean canAdd = rs.getBoolean("can_add");
                    boolean canEdit = rs.getBoolean("can_edit");
                    boolean canRemove = rs.getBoolean("can_remove");
                    String nodeType = rs.getString("node_type");
                    
                    if (canView) reads++;
                    if (canAdd || canEdit || canRemove) writes++;
                    if (canView && "FOLDER".equalsIgnoreCase(nodeType)) executes++;
                }
            }
        }
        
        long ownedFiles = 0;
        String ownerSql = "SELECT COUNT(*) FROM tb_item WHERE job_id = ? AND owner_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(ownerSql)) {
            ps.setLong(1, jobId);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ownedFiles = rs.getLong(1);
            }
        }
        
        UserJobStat newStat = null;
        if ("COMPLETED".equals(job.status())) {
            newStat = new UserJobStat(null, jobId, username, files, folders, ownedFiles, reads, writes, executes);
            try {
                // Return the saved instance since it might set the ID
                newStat = userJobStatRepo.save(newStat);
            } catch (Exception e) {
                LOG.warn("Could not save cached UserJobStat for job {} and user {}: {}", jobId, username, e.getMessage());
            }
        }
        
        return newStat != null ? newStat : new UserJobStat(null, jobId, username, files, folders, ownedFiles, reads, writes, executes);
    }
}
