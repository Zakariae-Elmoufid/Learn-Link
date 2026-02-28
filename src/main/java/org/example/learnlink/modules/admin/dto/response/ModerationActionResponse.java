package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModerationActionType;
import org.example.learnlink.modules.admin.entity.ModerationTargetType;

import java.time.LocalDateTime;

/**
 * Response DTO for moderation actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationActionResponse {

    private Long id;
    private ModerationActionType actionType;
    private ModerationTargetType targetType;
    private Long targetId;
    private String reason;
    private LocalDateTime actionAt;
    private Long moderatorId;
    private String moderatorUsername;
    private Boolean userNotified;
}
