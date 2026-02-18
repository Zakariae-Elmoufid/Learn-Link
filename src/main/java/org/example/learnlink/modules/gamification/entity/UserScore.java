package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_scores", indexes = {
        @Index(name = "idx_user_score_user_id", columnList = "user_id"),
        @Index(name = "idx_user_score_level", columnList = "level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer totalPoints = 0;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer currentLevelPoints = 0;

    @Column(nullable = false)
    private Integer pointsForNextLevel = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addPoints(int points) {
        this.totalPoints += points;
        this.currentLevelPoints += points;

        while (this.currentLevelPoints >= this.pointsForNextLevel) {
            this.currentLevelPoints -= this.pointsForNextLevel;
            this.level++;
            this.pointsForNextLevel = calculatePointsForLevel(this.level);
        }
    }

    private static Integer calculatePointsForLevel(Integer level) {
        return 100 + (level * 50);
    }
}