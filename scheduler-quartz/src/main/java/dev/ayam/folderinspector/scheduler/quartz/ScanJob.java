package dev.ayam.folderinspector.scheduler.quartz;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.core.plugin.Scanner;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ScanJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ScanJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String pathStr = dataMap.getString("path");
        String snapshotName = dataMap.getString("snapshotName");
        Scanner scanner = (Scanner) dataMap.get("scanner");
        Database database = (Database) dataMap.get("database");

        Path path = Paths.get(pathStr);
        logger.info("Executing scheduled scan for path: {} (Snapshot: {})", path, snapshotName);

        try (Stream<Item> items = scanner.scan(path)) {
            database.save(items, snapshotName);
            logger.info("Scheduled scan completed successfully: {}", snapshotName);
        } catch (Exception e) {
            logger.error("Scheduled scan failed: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
