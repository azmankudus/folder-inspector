package backend.controller;

import backend.dto.DashboardDTO;
import backend.service.DashboardService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import java.util.List;

@Controller("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityService securityService;

    public DashboardController(DashboardService dashboardService, SecurityService securityService) {
        this.dashboardService = dashboardService;
        this.securityService = securityService;
    }

    @Secured({ "API_DASHBOARD_VIEW" })
    @Get
    public DashboardDTO getDashboard(@QueryValue(defaultValue = "") String targetUser) {
        Authentication auth = securityService.getAuthentication().orElse(null);
        if (auth == null) {
            return new DashboardDTO(new DashboardDTO.UserSummary(0, 0, 0, 0), List.of());
        }

        String usernameToQuery = auth.getName();
        
        // If a specific target user is requested, verify the requester has manager/admin permissions
        if (targetUser != null && !targetUser.isBlank() && !targetUser.equalsIgnoreCase(auth.getName())) {
            boolean canImpersonate = auth.getRoles().contains("API_REPORT_ALL") || auth.getRoles().contains("API_LIST_ALL");
            if (canImpersonate) {
                usernameToQuery = targetUser;
            } else {
                throw new HttpStatusException(
                    HttpStatus.FORBIDDEN, 
                    "You do not have permission to view stats for other users."
                );
            }
        }

        return dashboardService.getDashboard(usernameToQuery);
    }

    @Secured({ "API_DASHBOARD_VIEW" })
    @Get("/history/{scanConfigId}")
    public List<DashboardDTO.ProfileHistoryItem> getDashboardHistory(
        @PathVariable Long scanConfigId,
        @QueryValue(defaultValue = "") String targetUser) {
        
        Authentication auth = securityService.getAuthentication().orElse(null);
        if (auth == null) {
            return List.of();
        }

        String usernameToQuery = auth.getName();
        
        if (targetUser != null && !targetUser.isBlank() && !targetUser.equalsIgnoreCase(auth.getName())) {
            boolean canImpersonate = auth.getRoles().contains("API_REPORT_ALL") || auth.getRoles().contains("API_LIST_ALL");
            if (canImpersonate) {
                usernameToQuery = targetUser;
            } else {
                throw new HttpStatusException(
                    HttpStatus.FORBIDDEN, 
                    "You do not have permission to view stats for other users."
                );
            }
        }

        return dashboardService.getDashboardHistory(usernameToQuery, scanConfigId);
    }
}
