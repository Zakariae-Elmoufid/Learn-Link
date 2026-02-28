package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModeratorPermission;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for moderator details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Boolean active;

    private Set<ModeratorPermission> permissions;

    private Long assignedByUserId;
    private String assignedByUsername;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;

    private String notes;
}
