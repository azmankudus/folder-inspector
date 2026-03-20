package backend.dto;

import java.util.List;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DashboardDTO(
    UserSummary userSummary,
    List<ProfileCard> profileCards
) {
    @Serdeable
    public record UserSummary(
        long totalFilesOwned,
        long totalFilesRead,
        long totalFilesWrite,
        long totalFilesExecute
    ) {}

    @Serdeable
    public record ProfileCard(
        Long scanConfigId,
        String title, // smb://...
        long totalFiles,
        long totalFolders,
        long totalRead,
        long totalWrite,
        long totalExecute
    ) {}

    @Serdeable
    public record ProfileHistoryItem(
        String timestamp,
        long totalFiles,
        long totalFolders,
        long totalRead,
        long totalWrite,
        long totalExecute
    ) {}
}
