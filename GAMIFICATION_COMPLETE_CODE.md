# 💻 CODE COMPLET - MODULE GAMIFICATION

## 1. BadgeType.java

```java
package org.example.learnlink.modules.gamification.entity;

public enum BadgeType {
    // Contribution badges
    FIRST_POST,
    HELPFUL_CONTRIBUTOR,
    EXPERT,
    
    // Activity badges
    STREAK_WARRIOR,
    LEVEL_MASTER,
    THOUSAND_POINTS,
    
    // Social badges
    CONNECTOR,
    MENTOR,
    COMMUNITY_LEADER
}
```

---

## 2. BadgeRarity.java

```java
package org.example.learnlink.modules.gamification.entity;

public enum BadgeRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}
```

---

## 3. UserScore.java

```java
package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

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
    private UUID userId;
    
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
```

---

## 4. Badge.java

```java
package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeRarity rarity;
    
    @Column(nullable = false)
    private Integer pointsRequired = 0;
    
    @Column(nullable = false)
    private Boolean active = true;
}
```

---

## 5. UserBadge.java

```java
package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

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
    private UUID userId;
    
    @Column(nullable = false)
    private Long badgeId;
    
    @Column(name = "earned_at", nullable = false, updatable = false)
    private Instant earnedAt;
    
    @PrePersist
    protected void onCreate() {
        this.earnedAt = Instant.now();
    }
}
```

---

## 6. ScoreHistory.java

```java
package org.example.learnlink.modules.gamification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "score_history", indexes = {
    @Index(name = "idx_score_history_user_id", columnList = "user_id"),
    @Index(name = "idx_score_history_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private Integer points;
    
    @Column(nullable = false)
    private String actionType;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
```

---

## 7. UserScoreResponse.java

```java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScoreResponse {
    private UUID userId;
    private Integer totalPoints;
    private Integer level;
    private Integer currentLevelPoints;
    private Integer pointsForNextLevel;
    private Double progressPercentage;
}
```

---

## 8. BadgeResponse.java

```java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import org.example.learnlink.modules.gamification.entity.BadgeRarity;
import org.example.learnlink.modules.gamification.entity.BadgeType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private BadgeType type;
    private BadgeRarity rarity;
    private Integer pointsRequired;
    private Boolean earned;
}
```

---

## 9. LeaderboardEntryResponse.java

```java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private Integer rank;
    private UUID userId;
    private String username;
    private String profilePictureUrl;
    private Integer totalPoints;
    private Integer level;
    private Integer badgeCount;
}
```

---

## 10. AddPointsRequest.java

```java
package org.example.learnlink.modules.gamification.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsRequest {
    
    @NotBlank(message = "Action type is required")
    private String actionType;
    
    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Integer points;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
```

---

## 11. ScoreHistoryResponse.java

```java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreHistoryResponse {
    private Long id;
    private Integer points;
    private String actionType;
    private String description;
    private Instant createdAt;
}
```

---

## 12. AchievementResponse.java

```java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponse {
    private BadgeResponse badge;
    private Instant earnedAt;
    private String earnedMessage;
}
```

---

## 13. UserScoreRepository.java

```java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    
    Optional<UserScore> findByUserId(UUID userId);
    
    @Query(value = "SELECT * FROM user_scores ORDER BY total_points DESC, level DESC LIMIT ?1", 
           nativeQuery = true)
    List<UserScore> findTopByPoints(int limit);
    
    @Query("SELECT us FROM UserScore us ORDER BY us.level DESC, us.totalPoints DESC")
    List<UserScore> findAllOrderedByLevelAndPoints();
}
```

---

## 14. BadgeRepository.java

```java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.Badge;
import org.example.learnlink.modules.gamification.entity.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    Optional<Badge> findByCode(String code);
    
    List<Badge> findByActiveTrue();
    
    List<Badge> findByType(BadgeType type);
    
    List<Badge> findByActiveTrueOrderByRarity();
}
```

---

## 15. UserBadgeRepository.java

```java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    List<UserBadge> findByUserId(UUID userId);
    
    Optional<UserBadge> findByUserIdAndBadgeId(UUID userId, Long badgeId);
    
    Integer countByUserId(UUID userId);
    
    boolean existsByUserIdAndBadgeId(UUID userId, Long badgeId);
}
```

---

## 16. ScoreHistoryRepository.java

```java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.ScoreHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {
    
    Page<ScoreHistory> findByUserId(UUID userId, Pageable pageable);
    
    List<ScoreHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
```

---

## 17. GamificationService.java

```java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.entity.ScoreHistory;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.ScoreHistoryRepository;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {
    
    private final UserScoreRepository userScoreRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    
    /**
     * Ajoute des points à un utilisateur
     */
    public UserScore addPoints(UUID userId, Integer points, String actionType, String description) {
        log.info("Adding {} points to user {} for action {}", points, userId, actionType);
        
        UserScore userScore = userScoreRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserScore newScore = UserScore.builder()
                    .userId(userId)
                    .totalPoints(0)
                    .level(1)
                    .currentLevelPoints(0)
                    .pointsForNextLevel(100)
                    .build();
                return userScoreRepository.save(newScore);
            });
        
        int previousLevel = userScore.getLevel();
        userScore.addPoints(points);
        userScore = userScoreRepository.save(userScore);
        
        // Enregistrer l'historique
        ScoreHistory history = ScoreHistory.builder()
            .userId(userId)
            .points(points)
            .actionType(actionType)
            .description(description)
            .build();
        scoreHistoryRepository.save(history);
        
        log.info("Points added successfully. New total: {}, Level: {}", 
            userScore.getTotalPoints(), userScore.getLevel());
        
        return userScore;
    }
    
    /**
     * Récupère le score d'un utilisateur
     */
    public UserScoreResponse getUserScore(UUID userId) {
        UserScore userScore = userScoreRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User score not found"));
        
        return mapToResponse(userScore);
    }
    
    private UserScoreResponse mapToResponse(UserScore userScore) {
        double progress = (double) userScore.getCurrentLevelPoints() / 
                         userScore.getPointsForNextLevel() * 100;
        
        return UserScoreResponse.builder()
            .userId(userScore.getUserId())
            .totalPoints(userScore.getTotalPoints())
            .level(userScore.getLevel())
            .currentLevelPoints(userScore.getCurrentLevelPoints())
            .pointsForNextLevel(userScore.getPointsForNextLevel())
            .progressPercentage(progress)
            .build();
    }
}
```

---

## 18. BadgeService.java

```java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.BadgeResponse;
import org.example.learnlink.modules.gamification.entity.Badge;
import org.example.learnlink.modules.gamification.entity.UserBadge;
import org.example.learnlink.modules.gamification.repository.BadgeRepository;
import org.example.learnlink.modules.gamification.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeService {
    
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    
    /**
     * Récupère tous les badges actifs
     */
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findByActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }
    
    /**
     * Récupère les badges déverrouillés par un utilisateur
     */
    public List<BadgeResponse> getUserBadges(UUID userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        return userBadges.stream()
            .map(ub -> badgeRepository.findById(ub.getBadgeId())
                .map(badge -> {
                    BadgeResponse response = mapToResponse(badge);
                    response.setEarned(true);
                    return response;
                })
                .orElse(null))
            .toList();
    }
    
    /**
     * Déverrouille un badge pour un utilisateur
     */
    public void unlockBadge(UUID userId, Long badgeId) {
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            log.debug("Badge already unlocked for user {}", userId);
            return;
        }
        
        UserBadge userBadge = UserBadge.builder()
            .userId(userId)
            .badgeId(badgeId)
            .build();
        
        userBadgeRepository.save(userBadge);
        log.info("Badge {} unlocked for user {}", badgeId, userId);
    }
    
    /**
     * Compte les badges déverrouillés
     */
    public Integer countUserBadges(UUID userId) {
        return userBadgeRepository.countByUserId(userId);
    }
    
    private BadgeResponse mapToResponse(Badge badge) {
        return BadgeResponse.builder()
            .id(badge.getId())
            .code(badge.getCode())
            .name(badge.getName())
            .description(badge.getDescription())
            .iconUrl(badge.getIconUrl())
            .type(badge.getType())
            .rarity(badge.getRarity())
            .pointsRequired(badge.getPointsRequired())
            .earned(false)
            .build();
    }
}
```

---

## 19. LeaderboardService.java

```java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.UserBadgeRepository;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {
    
    private final UserScoreRepository userScoreRepository;
    private final UserBadgeRepository userBadgeRepository;
    
    /**
     * Récupère le classement global
     */
    public List<LeaderboardEntryResponse> getGlobalLeaderboard(int limit) {
        List<UserScore> topUsers = userScoreRepository.findTopByPoints(limit);
        return mapToLeaderboard(topUsers);
    }
    
    /**
     * Récupère le rang d'un utilisateur
     */
    public Integer getUserRank(UUID userId) {
        List<UserScore> allUsers = userScoreRepository.findAllOrderedByLevelAndPoints();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserId().equals(userId)) {
                return i + 1;
            }
        }
        return null;
    }
    
    private List<LeaderboardEntryResponse> mapToLeaderboard(List<UserScore> userScores) {
        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        int rank = 1;
        
        for (UserScore score : userScores) {
            LeaderboardEntryResponse entry = LeaderboardEntryResponse.builder()
                .rank(rank++)
                .userId(score.getUserId())
                .totalPoints(score.getTotalPoints())
                .level(score.getLevel())
                .badgeCount(userBadgeRepository.countByUserId(score.getUserId()))
                .build();
            
            entries.add(entry);
        }
        
        return entries;
    }
}
```

---

## 20. GamificationController.java

```java
package org.example.learnlink.modules.gamification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.*;
import org.example.learnlink.modules.gamification.service.BadgeService;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.gamification.service.LeaderboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {
    
    private final GamificationService gamificationService;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;
    
    @GetMapping("/score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserScoreResponse> getMyScore(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }
    
    @GetMapping("/score/{userId}")
    public ResponseEntity<UserScoreResponse> getUserScore(@PathVariable UUID userId) {
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }
    
    @PostMapping("/points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserScoreResponse> addPoints(
        @RequestParam UUID userId,
        @Valid @RequestBody AddPointsRequest request) {
        
        gamificationService.addPoints(userId, request.getPoints(), 
            request.getActionType(), request.getDescription());
        
        UserScoreResponse response = gamificationService.getUserScore(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }
    
    @GetMapping("/badges/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeResponse>> getMyBadges(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
    
    @GetMapping("/badges/{userId}")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable UUID userId) {
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
        @RequestParam(defaultValue = "50") int limit) {
        
        List<LeaderboardEntryResponse> leaderboard = 
            leaderboardService.getGlobalLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
    
    @GetMapping("/leaderboard/rank")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> getMyRank(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Integer rank = leaderboardService.getUserRank(userId);
        return ResponseEntity.ok(rank);
    }
}
```

---

## 21. V1_0_1__Create_Gamification_Tables.sql

```sql
CREATE TABLE IF NOT EXISTS user_scores (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    total_points INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    current_level_points INTEGER NOT NULL DEFAULT 0,
    points_for_next_level INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_score_user_id ON user_scores(user_id);
CREATE INDEX idx_user_score_level ON user_scores(level);

CREATE TABLE IF NOT EXISTS badges (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    icon_url VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) NOT NULL,
    points_required INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_badge_code ON badges(code);

CREATE TABLE IF NOT EXISTS user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, badge_id),
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_badge_user_id ON user_badges(user_id);
CREATE INDEX idx_user_badge_badge_id ON user_badges(badge_id);

CREATE TABLE IF NOT EXISTS score_history (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    points INTEGER NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_score_history_user_id ON score_history(user_id);

INSERT INTO badges (code, name, description, type, rarity, points_required, active)
VALUES 
    ('FIRST_POST', 'Premier Post', 'Créer votre premier post', 'FIRST_POST', 'COMMON', 0, true),
    ('HELPFUL_CONTRIBUTOR', 'Contributeur Utile', '10 réponses acceptées', 'HELPFUL_CONTRIBUTOR', 'UNCOMMON', 500, true),
    ('EXPERT', 'Expert', '100 réponses acceptées', 'EXPERT', 'RARE', 5000, true),
    ('STREAK_WARRIOR', 'Guerrier du Streaks', '7 jours consécutifs actif', 'STREAK_WARRIOR', 'UNCOMMON', 350, true),
    ('LEVEL_MASTER', 'Maître du Niveau', 'Atteindre le niveau 10', 'LEVEL_MASTER', 'EPIC', 5000, true),
    ('THOUSAND_POINTS', 'Millier', '1000 points gagnés', 'THOUSAND_POINTS', 'RARE', 1000, true),
    ('CONNECTOR', 'Connecteur', '10 connexions', 'CONNECTOR', 'UNCOMMON', 250, true),
    ('MENTOR', 'Mentor', 'Aider 5 personnes', 'MENTOR', 'RARE', 500, true),
    ('COMMUNITY_LEADER', 'Leader Communautaire', 'Top 10 du leaderboard', 'COMMUNITY_LEADER', 'LEGENDARY', 10000, true);
```

---

## Résumé

Cette documentation fournit le code complet et prêt à utiliser pour implémenter le module de gamification. Tous les fichiers sont structurés suivant les bonnes pratiques Spring Boot et peuvent être directement copiés dans votre projet.


