package backend.service;

import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;

/**
 * Service for managing dynamic database partitions for file nodes and ACLs.
 * Ensures partitions exist for each scan profile before scanning begins.
 */
@Singleton
public class DatabaseSchemaService {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSchemaService.class);
    private final JdbcOperations jdbcOperations;

    public DatabaseSchemaService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * Creates table partitions for file_node and file_acl if they don't already exist for the profile.
     * @param profileId The ID of the scan profile
     */
    @Transactional
    public void ensurePartitionExists(Long profileId) {
        String tableName = "file_node_p" + profileId;
        String aclTableName = "file_acl_p" + profileId;
        
        jdbcOperations.execute(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + 
                             " PARTITION OF file_node FOR VALUES IN (" + profileId + ")");
                
                stmt.execute("CREATE TABLE IF NOT EXISTS " + aclTableName + 
                             " PARTITION OF file_acl FOR VALUES IN (" + profileId + ")");
                
                LOG.debug("Ensured partitions exist for profile ID: {}", profileId);
            }
            return null;
        });
    }

    /**
     * Truncates the partitions associated with a scan profile to prepare for a fresh scan.
     * @param profileId The ID of the scan profile
     */
    @Transactional
    public void truncatePartition(Long profileId) {
        String tableName = "file_node_p" + profileId;
        String aclTableName = "file_acl_p" + profileId;
        
        jdbcOperations.execute(conn -> {
            try (Statement stmt = conn.createStatement()) {
                LOG.info("Truncating partitions for profile ID: {}", profileId);
                stmt.execute("TRUNCATE TABLE " + tableName + " CASCADE");
                stmt.execute("TRUNCATE TABLE " + aclTableName + " CASCADE");
            }
            return null;
        });
    }
}
