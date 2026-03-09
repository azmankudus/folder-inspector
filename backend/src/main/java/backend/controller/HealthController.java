package backend.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import backend.repository.DirectoryProfileRepository;
import backend.model.DirectoryProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Map;

/**
 * Diagnostic controller providing deep health checks for database and directory services.
 */
@Controller("/health")
@Secured(SecurityRule.IS_ANONYMOUS)
public class HealthController {
    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);
    private final JdbcOperations jdbc;
    private final DirectoryProfileRepository dirRepo;

    public HealthController(JdbcOperations jdbc, DirectoryProfileRepository dirRepo) {
        this.jdbc = jdbc;
        this.dirRepo = dirRepo;
    }

    @Get("/deep")
    public Status check() {
        boolean dbOk = false;
        try {
            jdbc.execute(conn -> conn.isValid(2));
            dbOk = true;
        } catch (Exception e) {
            LOG.error("Database health check failed", e);
        }

        boolean ldapOk = false;
        String ldapMsg = "N/A";
        var profiles = dirRepo.findAll();
        if (profiles.iterator().hasNext()) {
            DirectoryProfile dp = profiles.iterator().next();
            try {
                Hashtable<String, String> env = new Hashtable<>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, "ldap://" + dp.adHost() + ":" + dp.adPort());
                env.put(Context.SECURITY_AUTHENTICATION, "none"); // probe connection only
                new InitialDirContext(env).close();
                ldapOk = true;
                ldapMsg = "Connected to " + dp.adHost();
            } catch (Exception e) {
                ldapMsg = "Primary failed: " + e.getMessage();
                if (dp.adBackupHost() != null) {
                    try {
                        Hashtable<String, String> env = new Hashtable<>();
                        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                        env.put(Context.PROVIDER_URL, "ldap://" + dp.adBackupHost() + ":" + dp.adBackupPort());
                        env.put(Context.SECURITY_AUTHENTICATION, "none");
                        new InitialDirContext(env).close();
                        ldapOk = true;
                        ldapMsg += " | Backup " + dp.adBackupHost() + " OK";
                    } catch (Exception ex) {
                        ldapMsg += " | Backup failed: " + ex.getMessage();
                    }
                }
            }
        }

        return new Status(dbOk ? "UP" : "DOWN", ldapOk ? "UP" : "DOWN", Map.of("ldapMessage", ldapMsg));
    }

    @Serdeable
    public record Status(String database, String directory, Map<String, String> details) {}
}
