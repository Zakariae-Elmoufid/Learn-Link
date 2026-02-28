package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModeratorPermission;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for moderator permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorPermissionsResponse {

    private Long userId;
    private Set<ModeratorPermission> currentPermissions;
    private Set<ModeratorPermission> availablePermissions;
    private LocalDateTime lastUpdated;
}
