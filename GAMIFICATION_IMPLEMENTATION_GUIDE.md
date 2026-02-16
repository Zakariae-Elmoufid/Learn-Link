# 📚 GUIDE COMPLET D'IMPLÉMENTATION - MODULE GAMIFICATION

## 🎯 Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Structure des fichiers](#structure-des-fichiers)
4. [Implémentation étape par étape](#implémentation-étape-par-étape)
5. [Base de données](#base-de-données)
6. [API REST](#api-rest)
7. [Intégration avec les événements](#intégration-avec-les-événements)
8. [Tests](#tests)

---

## 🎮 VUE D'ENSEMBLE

### Fonctionnalités à implémenter

| ID | Fonctionnalité | Description | Priorité |
|----|---|---|---|
| F-G-01 | Système de points | Gagner des points par action | ⭐⭐⭐ |
| F-G-02 | Niveaux | Progression par niveaux (1-10+) | ⭐⭐⭐ |
| F-G-03 | Badges | Débloquer des badges | ⭐⭐ |
| F-G-04 | Leaderboard global | Classement général | ⭐⭐ |
| F-G-05 | Leaderboard hebdo | Classement de la semaine | ⭐⭐ |
| F-G-06 | Historique points | Voir historique des gains | ⭐ |
| F-G-07 | Profil public | Afficher niveau et badges | ⭐⭐ |

### Flux de fonctionnement

```
User Action (Post created, Answer accepted, etc)
       ↓
Event Published (PostCreatedEvent, AnswerAcceptedEvent, etc)
       ↓
GamificationListener (Captures event)
       ↓
GamificationService (Process points, check badges, update levels)
       ↓
UserScore Updated (Points, Level increased)
       ↓
UserAchievement Recorded (Badge unlocked)
       ↓
Notification Sent (Optional)
```

---

## 🏗️ ARCHITECTURE

### Composants principaux

#### 1. **Entités JPA**
- `UserScore` - Score et niveau utilisateur
- `Badge` - Définition des badges
- `UserBadge` - Badges déverrouillés par l'utilisateur
- `ScoreHistory` - Historique des points gagnés

#### 2. **DTOs (Transfer Objects)**
- `UserScoreResponse` - Réponse du score utilisateur
- `BadgeResponse` - Réponse du badge
- `LeaderboardEntryResponse` - Entrée du classement
- `AddPointsRequest` - Requête d'ajout de points
- `AchievementResponse` - Réponse d'un badge déverrouillé

#### 3. **Services**
- `GamificationService` - Gestion des points et niveaux
- `BadgeService` - Gestion des badges
- `LeaderboardService` - Gestion des classements

#### 4. **Contrôleurs**
- `GamificationController` - Endpoints de gamification

#### 5. **Repositories**
- `UserScoreRepository` - Accès aux scores
- `BadgeRepository` - Accès aux badges
- `UserBadgeRepository` - Accès aux badges utilisateur
- `ScoreHistoryRepository` - Accès à l'historique

---

## 📂 STRUCTURE DES FICHIERS

```
src/main/java/org/example/learnlink/modules/gamification/
├── entity/
│   ├── UserScore.java
│   ├── Badge.java
│   ├── BadgeType.java
│   ├── BadgeRarity.java
│   ├── UserBadge.java
│   └── ScoreHistory.java
├── dto/
│   ├── UserScoreResponse.java
│   ├── BadgeResponse.java
│   ├── LeaderboardEntryResponse.java
│   ├── AddPointsRequest.java
│   ├── AchievementResponse.java
│   └── ScoreHistoryResponse.java
├── repository/
│   ├── UserScoreRepository.java
│   ├── BadgeRepository.java
│   ├── UserBadgeRepository.java
│   └── ScoreHistoryRepository.java
├── service/
│   ├── GamificationService.java
│   ├── BadgeService.java
│   └── LeaderboardService.java
├── controller/
│   └── GamificationController.java
└── mapper/
    ├── UserScoreMapper.java
    ├── BadgeMapper.java
    └── LeaderboardMapper.java

src/main/resources/db/migration/
└── V1_0_1__Create_Gamification_Tables.sql

src/test/java/org/example/learnlink/modules/gamification/
├── service/
│   ├── GamificationServiceTest.java
│   ├── BadgeServiceTest.java
│   └── LeaderboardServiceTest.java
└── controller/
    └── GamificationControllerTest.java
```

---

## 🔧 IMPLÉMENTATION ÉTAPE PAR ÉTAPE

### ÉTAPE 1 : Créer les Entités

#### 1.1 BadgeType (Énumération)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/BadgeType.java
package org.example.learnlink.modules.gamification.entity;

public enum BadgeType {
    // Contribution badges
    FIRST_POST,           // Premier post
    HELPFUL_CONTRIBUTOR,  // Contributeur utile (10 réponses acceptées)
    EXPERT,              // Expert (100 réponses acceptées)
    
    // Activity badges
    STREAK_WARRIOR,      // 7 jours consécutifs actif
    LEVEL_MASTER,        // Atteindre le niveau 10
    THOUSAND_POINTS,     // 1000 points gagnés
    
    // Social badges
    CONNECTOR,           // 10 connexions
    MENTOR,              // Aider 5 personnes
    COMMUNITY_LEADER,    // Dans le top 10 du leaderboard
}
```

#### 1.2 BadgeRarity (Énumération)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/BadgeRarity.java
package org.example.learnlink.modules.gamification.entity;

public enum BadgeRarity {
    COMMON,      // Commun (gris)
    UNCOMMON,    // Peu commun (vert)
    RARE,        // Rare (bleu)
    EPIC,        // Épique (violet)
    LEGENDARY    // Légendaire (doré)
}
```

#### 1.3 UserScore (Entité)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/UserScore.java
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
    
    /**
     * Ajoute des points et met à jour le niveau si nécessaire
     */
    public void addPoints(int points) {
        this.totalPoints += points;
        this.currentLevelPoints += points;
        
        // Vérifier si niveau suivant atteint
        while (this.currentLevelPoints >= this.pointsForNextLevel) {
            this.currentLevelPoints -= this.pointsForNextLevel;
            this.level++;
            // Les points requis augmentent avec le niveau
            this.pointsForNextLevel = calculatePointsForLevel(this.level);
        }
    }
    
    private static Integer calculatePointsForLevel(Integer level) {
        // Formule: 100 + (level * 50)
        return 100 + (level * 50);
    }
}
```

#### 1.4 Badge (Entité)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/Badge.java
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

#### 1.5 UserBadge (Entité)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/UserBadge.java
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

#### 1.6 ScoreHistory (Entité)

```java
// src/main/java/org/example/learnlink/modules/gamification/entity/ScoreHistory.java
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
    private String actionType;  // POST_CREATED, ANSWER_ACCEPTED, etc.
    
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

### ÉTAPE 2 : Créer les DTOs

#### 2.1 UserScoreResponse

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/UserScoreResponse.java
package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.util.List;
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
    private Double progressPercentage;  // currentLevelPoints / pointsForNextLevel * 100
    private List<BadgeResponse> unlockedBadges;
    private Integer totalBadges;
}
```

#### 2.2 BadgeResponse

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/BadgeResponse.java
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
    private Boolean earned;  // true si l'utilisateur a ce badge
}
```

#### 2.3 LeaderboardEntryResponse

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/LeaderboardEntryResponse.java
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

#### 2.4 AddPointsRequest

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/AddPointsRequest.java
package org.example.learnlink.modules.gamification.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsRequest {
    
    @NotBlank(message = "Action type is required")
    private String actionType;  // POST_CREATED, ANSWER_ACCEPTED, etc.
    
    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Integer points;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
```

#### 2.5 AchievementResponse

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/AchievementResponse.java
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
    private String earnedMessage;  // Ex: "Vous avez déverrouillé le badge..."
}
```

#### 2.6 ScoreHistoryResponse

```java
// src/main/java/org/example/learnlink/modules/gamification/dto/ScoreHistoryResponse.java
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

### ÉTAPE 3 : Créer les Repositories

#### 3.1 UserScoreRepository

```java
// src/main/java/org/example/learnlink/modules/gamification/repository/UserScoreRepository.java
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
    
    @Query(value = """
        SELECT * FROM user_scores 
        WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'
        ORDER BY total_points DESC 
        LIMIT ?1
        """, nativeQuery = true)
    List<UserScore> findWeeklyLeaderboard(int limit);
    
    @Query("SELECT us FROM UserScore us ORDER BY us.level DESC, us.totalPoints DESC")
    List<UserScore> findAllOrderedByLevelAndPoints();
}
```

#### 3.2 BadgeRepository

```java
// src/main/java/org/example/learnlink/modules/gamification/repository/BadgeRepository.java
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

#### 3.3 UserBadgeRepository

```java
// src/main/java/org/example/learnlink/modules/gamification/repository/UserBadgeRepository.java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    List<UserBadge> findByUserId(UUID userId);
    
    Optional<UserBadge> findByUserIdAndBadgeId(UUID userId, Long badgeId);
    
    Integer countByUserId(UUID userId);
    
    @Query("""
        SELECT COUNT(DISTINCT ub.badgeId) FROM UserBadge ub 
        WHERE ub.userId = ?1
        """)
    Integer countDistinctBadgesByUserId(UUID userId);
    
    boolean existsByUserIdAndBadgeId(UUID userId, Long badgeId);
}
```

#### 3.4 ScoreHistoryRepository

```java
// src/main/java/org/example/learnlink/modules/gamification/repository/ScoreHistoryRepository.java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {
    
    Page<ScoreHistory> findByUserId(UUID userId, Pageable pageable);
    
    List<ScoreHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<ScoreHistory> findByUserIdAndCreatedAtAfter(UUID userId, Instant startDate);
    
    Page<ScoreHistory> findByUserIdAndActionType(UUID userId, String actionType, Pageable pageable);
}
```

### ÉTAPE 4 : Créer les Services

#### 4.1 GamificationService

```java
// src/main/java/org/example/learnlink/modules/gamification/service/GamificationService.java
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
    private final BadgeService badgeService;
    private final GamificationMapper mapper;
    
    /**
     * Ajoute des points à un utilisateur
     */
    public UserScore addPoints(UUID userId, Integer points, String actionType, String description) {
        log.info("Adding {} points to user {} for action {}", points, userId, actionType);
        
        // Récupérer ou créer le score de l'utilisateur
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
        
        // Récupérer le niveau précédent
        int previousLevel = userScore.getLevel();
        
        // Ajouter les points
        userScore.addPoints(points);
        userScore = userScoreRepository.save(userScore);
        
        // Enregistrer dans l'historique
        ScoreHistory history = ScoreHistory.builder()
            .userId(userId)
            .points(points)
            .actionType(actionType)
            .description(description)
            .build();
        scoreHistoryRepository.save(history);
        
        // Vérifier les nouveaux badges si le niveau a augmenté
        if (userScore.getLevel() > previousLevel) {
            badgeService.checkAndAwardLevelBadges(userId, previousLevel, userScore.getLevel());
        }
        
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
        
        return mapper.toResponse(userScore);
    }
    
    /**
     * Récupère le score d'un utilisateur ou crée un score par défaut
     */
    public UserScore getOrCreateUserScore(UUID userId) {
        return userScoreRepository.findByUserId(userId)
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
    }
}
```

#### 4.2 BadgeService

```java
// src/main/java/org/example/learnlink/modules/gamification/service/BadgeService.java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.BadgeResponse;
import org.example.learnlink.modules.gamification.entity.*;
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
    private final GamificationMapper mapper;
    
    /**
     * Récupère tous les badges actifs
     */
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findByActiveTrue()
            .stream()
            .map(mapper::toResponse)
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
                    BadgeResponse response = mapper.toResponse(badge);
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
        // Vérifier si le badge n'est pas déjà déverrouillé
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
     * Vérifie et attribue les badges de niveau
     */
    public void checkAndAwardLevelBadges(UUID userId, int previousLevel, int newLevel) {
        for (int level = previousLevel + 1; level <= newLevel; level++) {
            if (level == 5) {
                // Badge pour atteindre le niveau 5
                badgeRepository.findByCode("LEVEL_5")
                    .ifPresent(badge -> unlockBadge(userId, badge.getId()));
            } else if (level == 10) {
                // Badge pour atteindre le niveau 10
                badgeRepository.findByCode("LEVEL_10")
                    .ifPresent(badge -> unlockBadge(userId, badge.getId()));
            }
        }
    }
    
    /**
     * Compte les badges déverrouillés par un utilisateur
     */
    public Integer countUserBadges(UUID userId) {
        return userBadgeRepository.countByUserId(userId);
    }
}
```

#### 4.3 LeaderboardService

```java
// src/main/java/org/example/learnlink/modules/gamification/service/LeaderboardService.java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.UserBadgeRepository;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {
    
    private final UserScoreRepository userScoreRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final GamificationMapper mapper;
    private final UserService userService;  // À injecter
    
    /**
     * Récupère le classement global
     */
    public List<LeaderboardEntryResponse> getGlobalLeaderboard(int limit) {
        List<UserScore> topUsers = userScoreRepository.findTopByPoints(limit);
        return mapToLeaderboard(topUsers);
    }
    
    /**
     * Récupère le classement de la semaine
     */
    public List<LeaderboardEntryResponse> getWeeklyLeaderboard(int limit) {
        List<UserScore> weeklyUsers = userScoreRepository.findWeeklyLeaderboard(limit);
        return mapToLeaderboard(weeklyUsers);
    }
    
    /**
     * Récupère le rang d'un utilisateur
     */
    public Integer getUserRank(UUID userId) {
        List<UserScore> allUsers = userScoreRepository.findAllOrderedByLevelAndPoints();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserId().equals(userId)) {
                return i + 1;  // Le rang commence à 1
            }
        }
        return null;  // Utilisateur non trouvé
    }
    
    private List<LeaderboardEntryResponse> mapToLeaderboard(List<UserScore> userScores) {
        List<LeaderboardEntryResponse> entries = new java.util.ArrayList<>();
        int rank = 1;
        
        for (UserScore score : userScores) {
            // Récupérer les informations du profil utilisateur
            // TODO: Implémenter userService.getUserProfile(score.getUserId())
            
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

### ÉTAPE 5 : Créer le Contrôleur

#### 5.1 GamificationController

```java
// src/main/java/org/example/learnlink/modules/gamification/controller/GamificationController.java
package org.example.learnlink.modules.gamification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.*;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.gamification.service.BadgeService;
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
    
    /**
     * GET /api/v1/gamification/score
     * Récupère le score de l'utilisateur connecté
     */
    @GetMapping("/score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserScoreResponse> getMyScore(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }
    
    /**
     * GET /api/v1/gamification/score/{userId}
     * Récupère le score d'un utilisateur spécifique
     */
    @GetMapping("/score/{userId}")
    public ResponseEntity<UserScoreResponse> getUserScore(@PathVariable UUID userId) {
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }
    
    /**
     * POST /api/v1/gamification/points
     * Ajoute des points (utilisé en interne par les événements)
     */
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
    
    /**
     * GET /api/v1/gamification/badges
     * Récupère tous les badges disponibles
     */
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }
    
    /**
     * GET /api/v1/gamification/badges/my
     * Récupère les badges de l'utilisateur connecté
     */
    @GetMapping("/badges/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeResponse>> getMyBadges(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
    
    /**
     * GET /api/v1/gamification/badges/{userId}
     * Récupère les badges d'un utilisateur spécifique
     */
    @GetMapping("/badges/{userId}")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable UUID userId) {
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
    
    /**
     * GET /api/v1/gamification/leaderboard
     * Récupère le classement global
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
        @RequestParam(defaultValue = "50") int limit) {
        
        List<LeaderboardEntryResponse> leaderboard = 
            leaderboardService.getGlobalLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * GET /api/v1/gamification/leaderboard/weekly
     * Récupère le classement de la semaine
     */
    @GetMapping("/leaderboard/weekly")
    public ResponseEntity<List<LeaderboardEntryResponse>> getWeeklyLeaderboard(
        @RequestParam(defaultValue = "50") int limit) {
        
        List<LeaderboardEntryResponse> leaderboard = 
            leaderboardService.getWeeklyLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * GET /api/v1/gamification/leaderboard/rank
     * Récupère le rang de l'utilisateur connecté
     */
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

## 🗄️ BASE DE DONNÉES

Créer le fichier de migration:

```sql
-- src/main/resources/db/migration/V1_0_1__Create_Gamification_Tables.sql

-- Table des scores utilisateurs
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

-- Table des badges
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
CREATE INDEX idx_badge_type ON badges(type);

-- Table des badges déverrouillés par les utilisateurs
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

-- Table de l'historique des points
CREATE TABLE IF NOT EXISTS score_history (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    points INTEGER NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_score_history_user_id ON score_history(user_id);
CREATE INDEX idx_score_history_created_at ON score_history(created_at);
CREATE INDEX idx_score_history_action_type ON score_history(action_type);

-- Insérer des badges par défaut
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

## 📡 API REST

### Endpoints créés

```
GET    /api/v1/gamification/score              - Mon score
GET    /api/v1/gamification/score/{userId}     - Score d'un utilisateur
POST   /api/v1/gamification/points             - Ajouter des points
GET    /api/v1/gamification/badges             - Tous les badges
GET    /api/v1/gamification/badges/my          - Mes badges
GET    /api/v1/gamification/badges/{userId}    - Badges d'un utilisateur
GET    /api/v1/gamification/leaderboard        - Classement global
GET    /api/v1/gamification/leaderboard/weekly - Classement hebdomadaire
GET    /api/v1/gamification/leaderboard/rank   - Mon rang
```

---

## 🔄 INTÉGRATION AVEC LES ÉVÉNEMENTS

### Mettre à jour le CommunityGamificationListener

```java
// src/main/java/org/example/learnlink/modules/community/listener/CommunityGamificationListener.java

@Component
@RequiredArgsConstructor
@Slf4j
public class CommunityGamificationListener {
    
    private final GamificationService gamificationService;
    
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        int points = switch (event.getPostType()) {
            case SUMMARY -> 10;
            case TUTORIAL -> 15;
            case DISCUSSION -> 8;
        };
        
        gamificationService.addPoints(
            event.getUserId(), 
            points, 
            "POST_CREATED", 
            "Post créé: " + event.getPostType()
        );
    }
    
    @EventListener
    public void handleAnswerAcceptedEvent(AnswerAcceptedEvent event) {
        gamificationService.addPoints(
            event.getAnswerAuthorId(), 
            50, 
            "ANSWER_ACCEPTED", 
            "Réponse acceptée pour la question: " + event.getQuestionId()
        );
    }
    
    @EventListener
    public void handleAnswerUpvotedEvent(AnswerUpvotedEvent event) {
        gamificationService.addPoints(
            event.getAnswerAuthorId(), 
            5, 
            "ANSWER_UPVOTED", 
            "Réponse upvotée"
        );
    }
}
```

---

## ✅ CHECKLIST D'IMPLÉMENTATION

- [ ] Créer les énumérations (BadgeType, BadgeRarity)
- [ ] Créer les entités (UserScore, Badge, UserBadge, ScoreHistory)
- [ ] Créer les DTOs (UserScoreResponse, BadgeResponse, etc.)
- [ ] Créer les Repositories
- [ ] Créer les Services (GamificationService, BadgeService, LeaderboardService)
- [ ] Créer le Mapper (GamificationMapper)
- [ ] Créer le Contrôleur
- [ ] Créer la migration SQL
- [ ] Tester les endpoints
- [ ] Intégrer avec les événements existants
- [ ] Ajouter les tests unitaires
- [ ] Documenter l'API

---

## 🧪 TESTS

### Exemple de test unitaire

```java
// src/test/java/org/example/learnlink/modules/gamification/service/GamificationServiceTest.java

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {
    
    @Mock
    private UserScoreRepository userScoreRepository;
    
    @Mock
    private ScoreHistoryRepository scoreHistoryRepository;
    
    @Mock
    private BadgeService badgeService;
    
    @InjectMocks
    private GamificationService gamificationService;
    
    @Test
    void testAddPoints() {
        UUID userId = UUID.randomUUID();
        Integer points = 50;
        
        // Arrange
        UserScore userScore = UserScore.builder()
            .userId(userId)
            .totalPoints(0)
            .level(1)
            .build();
        
        when(userScoreRepository.findByUserId(userId))
            .thenReturn(Optional.of(userScore));
        
        // Act
        UserScore result = gamificationService.addPoints(userId, points, "TEST", "Test description");
        
        // Assert
        assertThat(result.getTotalPoints()).isEqualTo(points);
        verify(scoreHistoryRepository).save(any(ScoreHistory.class));
    }
}
```

---

**Document généré: 16 février 2026**

