package backend;

import io.micronaut.runtime.Micronaut;
import io.micronaut.context.ApplicationContext;
import backend.service.ScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        backend.util.SecretGenerator.ensureSecrets();
        if (args.length >= 3 && "--run-scan".equals(args[0])) {
            Long profileId = Long.parseLong(args[1]);
            Long historyId = Long.parseLong(args[2]);
            LOG.info("Starting standalone scan for profile ID: {}, history ID: {}", profileId, historyId);
            
            System.setProperty("micronaut.server.enabled", "false");
            System.setProperty("micronaut.http.server.enabled", "false");
            System.setProperty("micronaut.server.port", "-1");
            
            ApplicationContext context = ApplicationContext.builder()
                    .mainClass(Application.class)
                    .environments("cli")
                    .deduceEnvironment(false)
                    .properties(java.util.Map.of(
                        "micronaut.server.enabled", "false",
                        "micronaut.http.server.enabled", "false",
                        "micronaut.server.port", "-1",
                        "micronaut.runtime", "none"
                    ))
                    .start();
            try {
                ScanService service = context.getBean(ScanService.class);
                service.runScanStandalone(profileId, historyId);
            } finally {
                context.close();
            }
            return; // exit the standalone process
        }
        
        Micronaut.run(Application.class, args);
    }
}
