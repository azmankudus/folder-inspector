package backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JobLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(JobLauncher.class);

  public static long launchScanJob(Long profileId, Long historyId) throws Exception {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    String className = "backend.Application";

    File logDir = new File("jobs/logs");
    File pidDir = new File("jobs/pid");
    if (!logDir.exists()) logDir.mkdirs();
    if (!pidDir.exists()) pidDir.mkdirs();

    String baseName = "backend_scan_" + historyId;
    String logPath = "jobs/logs/" + baseName + ".log";
    String pidPath = "jobs/pid/" + baseName + ".pid";

    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.add("-Dmicronaut.server.enabled=false");
    command.add("-Dmicronaut.http.server.enabled=false");
    command.add("-Dmicronaut.server.port=-1");
    command.add("-cp");
    command.add(classpath);
    command.add(className);
    command.add("--run-scan");
    command.add(profileId.toString());
    command.add(historyId.toString());

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectErrorStream(true);
    builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logPath)));
    
    Process process = builder.start();
    long pid = process.pid();
    
    try (PrintWriter pw = new PrintWriter(pidPath)) {
      pw.print(pid);
    }
    
    LOG.info("Launched scan job [History ID: {}] with PID: {}", historyId, pid);
    return pid;
  }
}
