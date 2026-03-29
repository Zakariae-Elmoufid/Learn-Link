package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_badges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "badge_id"})
}, indexes = {
    @Index(name = "idx_user_badge_user_id", columnList = "user_id"),
    @Index(name = "idx_user_badge_badge_id", columnList = "badge_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long badgeId;

    @Column(name = "earned_at", nullable = false, updatable = false)
    private Instant earnedAt;

    @PrePersist
    protected void onCreate() {
        this.earnedAt = Instant.now();
    }
}

