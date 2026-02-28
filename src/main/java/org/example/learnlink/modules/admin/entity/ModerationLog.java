package org.example.learnlink.modules.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for logging all moderation actions for audit purposes
 */
@Entity
@Table(name = "moderation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ModerationActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ModerationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
