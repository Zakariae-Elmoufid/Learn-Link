# 🚀 GUIDE DE DÉMARRAGE RAPIDE - MODULE GAMIFICATION

## Phase 1 : Configuration de base (30 minutes)

### 1. Créer la structure des dossiers

```bash
mkdir -p src/main/java/org/example/learnlink/modules/gamification/{entity,dto,repository,service,controller,mapper}
mkdir -p src/test/java/org/example/learnlink/modules/gamification/{service,controller}
```

### 2. Créer les énumérations

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/entity/BadgeType.java`

```java
package org.example.learnlink.modules.gamification.entity;

public enum BadgeType {
    FIRST_POST,
    HELPFUL_CONTRIBUTOR,
    EXPERT,
    STREAK_WARRIOR,
    LEVEL_MASTER,
    THOUSAND_POINTS,
    CONNECTOR,
    MENTOR,
    COMMUNITY_LEADER
}
```

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/entity/BadgeRarity.java`

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

### 3. Créer l'entité UserScore

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/entity/UserScore.java`

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

## Phase 2 : DTOs (30 minutes)

### Créer les DTOs

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/dto/UserScoreResponse.java`

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

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/dto/AddPointsRequest.java`

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

## Phase 3 : Repositories (20 minutes)

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/repository/UserScoreRepository.java`

```java
package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    Optional<UserScore> findByUserId(UUID userId);
}
```

---

## Phase 4 : Service (45 minutes)

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/service/GamificationService.java`

```java
package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;
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
        
        log.info("Points added successfully. New total: {}, Level: {}", 
            userScore.getTotalPoints(), userScore.getLevel());
        
        return userScore;
    }
    
    public UserScoreResponse getUserScore(UUID userId) {
        UserScore userScore = userScoreRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User score not found"));
        
        return UserScoreResponse.builder()
            .userId(userScore.getUserId())
            .totalPoints(userScore.getTotalPoints())
            .level(userScore.getLevel())
            .currentLevelPoints(userScore.getCurrentLevelPoints())
            .pointsForNextLevel(userScore.getPointsForNextLevel())
            .progressPercentage((double) userScore.getCurrentLevelPoints() / 
                userScore.getPointsForNextLevel() * 100)
            .build();
    }
}
```

---

## Phase 5 : Contrôleur (30 minutes)

**Fichier**: `src/main/java/org/example/learnlink/modules/gamification/controller/GamificationController.java`

```java
package org.example.learnlink.modules.gamification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {
    
    private final GamificationService gamificationService;
    
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
}
```

---

## Phase 6 : Migration de Base de Données (20 minutes)

**Fichier**: `src/main/resources/db/migration/V1_0_1__Create_Gamification_Tables.sql`

```sql
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
```

---

## Phase 7 : Intégration aux Événements (30 minutes)

**Mettre à jour**: `src/main/java/org/example/learnlink/modules/community/listener/CommunityGamificationListener.java`

```java
package org.example.learnlink.modules.community.listener;

import org.example.learnlink.modules.community.event.*;
import org.example.learnlink.modules.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityGamificationListener {
    
    private final GamificationService gamificationService;
    
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.info("Post created event received: userId={}", event.getUserId());
        
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
}
```

---

## ✅ Commandes de test

### 1. Compiler le projet

```bash
mvn clean compile
```

### 2. Créer une base de données de test

```bash
# La migration s'exécutera automatiquement au démarrage
mvn spring-boot:run
```

### 3. Tester via cURL

```bash
# Récupérer mon score
curl -X GET http://localhost:8081/api/v1/gamification/score \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Récupérer le score d'un utilisateur
curl -X GET http://localhost:8081/api/v1/gamification/score/{userId}

# Ajouter des points (Admin seulement)
curl -X POST http://localhost:8081/api/v1/gamification/points \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "actionType": "TEST",
    "points": 50,
    "description": "Test action"
  }'
```

---

## 📋 Checklist à cocher

- [ ] Phase 1 ✅ Énumérations et UserScore créées
- [ ] Phase 2 ✅ DTOs créés
- [ ] Phase 3 ✅ Repositories créés
- [ ] Phase 4 ✅ Service créé
- [ ] Phase 5 ✅ Contrôleur créé
- [ ] Phase 6 ✅ Migration DB créée
- [ ] Phase 7 ✅ Intégration aux événements
- [ ] ✅ Compilation réussie (mvn clean compile)
- [ ] ✅ Tests de l'API

---

## 🔗 Prochaines étapes

Une fois cette structure de base en place, vous pouvez ajouter:

1. **Badges** - Voir le guide complet pour les entités Badge et UserBadge
2. **Leaderboard** - Voir le guide complet pour LeaderboardService
3. **Historique des points** - Voir le guide complet pour ScoreHistory
4. **Notifications** - Notifier l'utilisateur quand il gagne des points/badges
5. **Tests unitaires** - Ajouter les tests

Consultez `GAMIFICATION_IMPLEMENTATION_GUIDE.md` pour le guide complet avec toutes les fonctionnalités.


