package org.example.learnlink.modules.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModeratorPermission;

import java.util.Set;

/**
 * Request DTO for creating a new moderator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModeratorRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one permission is required")
    private Set<ModeratorPermission> permissions;

    private String notes;  // Optional notes about why this user is being made a moderator
}
