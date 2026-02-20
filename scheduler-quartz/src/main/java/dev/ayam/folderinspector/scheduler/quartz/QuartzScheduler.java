package dev.ayam.folderinspector.scheduler.quartz;

import dev.ayam.folderinspector.core.plugin.Database;
import dev.ayam.folderinspector.core.plugin.Scanner;
import dev.ayam.folderinspector.core.plugin.Scheduler;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QuartzScheduler implements Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);
    private final org.quartz.Scheduler scheduler;

    public QuartzScheduler() {
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to initialize Quartz scheduler", e);
        }
    }

    @Override
    public void scheduleScan(String name, String cronExpression, String path, String snapshotName,
                             Scanner scanner, Database database) throws Exception {
        
        JobDetail job = JobBuilder.newJob(ScanJob.class)
                .withIdentity(name, "scans")
                .usingJobData(createDataMap(path, snapshotName, scanner, database))
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name + "-trigger", "scans")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(job, trigger);
        logger.info("Scheduled scan '{}' with cron: {}", name, cronExpression);
    }

    @Override
    public void deleteScan(String name) throws Exception {
        scheduler.deleteJob(new JobKey(name, "scans"));
        logger.info("Deleted scheduled scan: {}", name);
    }

    @Override
    public List<ScheduleInfo> listScans() throws Exception {
        List<ScheduleInfo> schedules = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                JobDataMap dataMap = jobDetail.getJobDataMap();

                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                String cron = "N/A";
                if (!triggers.isEmpty() && triggers.get(0) instanceof CronTrigger) {
                    cron = ((CronTrigger) triggers.get(0)).getCronExpression();
                }

                schedules.add(new ScheduleInfo(
                        jobName,
                        cron,
                        dataMap.getString("path"),
                        dataMap.getString("snapshotName")
                ));
            }
        }
        return schedules;
    }

    @Override
    public void start() throws Exception {
        if (!scheduler.isStarted()) {
            scheduler.start();
            logger.info("Quartz scheduler started");
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Quartz scheduler shut down");
        }
    }

    @Override
    public String getName() {
        return "quartz";
    }

    private JobDataMap createDataMap(String path, String snapshotName, Scanner scanner, Database database) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("path", path);
        dataMap.put("snapshotName", snapshotName);
        dataMap.put("scanner", scanner);
        dataMap.put("database", database);
        return dataMap;
    }
}
