package org.example.learnlink.modules.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for moderation actions (hide, restore, delete)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationActionRequest {

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Builder.Default
    private Boolean notifyUser = false;
}
