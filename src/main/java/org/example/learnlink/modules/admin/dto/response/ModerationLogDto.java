package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.admin.entity.ModerationActionType;
import org.example.learnlink.modules.admin.entity.ModerationTargetType;

import java.time.LocalDateTime;

/**
 * DTO for moderation log entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLogDto {

    private Long id;
    private Long moderatorId;
    private String moderatorUsername;
    private ModerationActionType actionType;
    private ModerationTargetType targetType;
    private Long targetId;
    private Long targetUserId;
    private String targetUsername;
    private String reason;
    private String contentSnapshot;
    private LocalDateTime createdAt;
}
