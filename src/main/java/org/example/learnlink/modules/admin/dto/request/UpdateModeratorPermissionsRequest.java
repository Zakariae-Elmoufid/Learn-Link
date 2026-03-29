package org.example.learnlink.modules.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModeratorPermission;

import java.util.Set;

/**
 * Request DTO for updating moderator permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModeratorPermissionsRequest {

    @NotEmpty(message = "At least one permission is required")
    private Set<ModeratorPermission> permissions;

    private String reason;  // Reason for permission change
}
