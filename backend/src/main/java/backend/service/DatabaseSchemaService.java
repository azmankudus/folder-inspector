package backend.service;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Previously managed dynamic PostgreSQL partitions for tb_item and tb_item_acl.
 * Now a no-op since both tables use a flat schema keyed by job_id.
 */
@Singleton
public class DatabaseSchemaService {
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseSchemaService.class);

  /**
   * No-op: partition management has been removed.
   * tb_item rows are filtered by job_id instead.
   *
   * @param historyId The scan job ID (unused)
   */
  public void ensurePartitionExists(Long historyId) {
    LOG.trace("ensurePartitionExists called for jobId={} (no-op, table is not partitioned)", historyId);
  }

  /**
   * No-op: partition management has been removed.
   *
   * @param historyId The scan job ID (unused)
   */
  public void dropHistoryPartition(Long historyId) {
    LOG.trace("dropHistoryPartition called for jobId={} (no-op, table is not partitioned)", historyId);
  }
}
