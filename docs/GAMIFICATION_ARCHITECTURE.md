# Architecture Détaillée - Module Gamification

## 📊 Diagramme des Classes

```
┌─────────────────────────────────────────────────────────────────┐
│                        CONTROLLER LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│  GamificationController  │  BadgeController  │  LeaderboardCtrl  │
│  UserBadgeController     │                                       │
└──────────┬───────────────────────────────────┬──────────────────┘
           │                                   │
           ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                         SERVICE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│ GamificationService      │  BadgeService     │  UserBadgeService│
│ LeaderboardService       │                                       │
└──────────┬───────────────────────────────────┬──────────────────┘
           │                                   │
           ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│ UserScoreRepository  │  BadgeRepository  │  UserBadgeRepository │
│ LeaderboardRepository                                            │
└──────────┬───────────────────────────────────┬──────────────────┘
           │                                   │
           ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ENTITY LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│ UserScore (existant)     │  Badge      │  UserBadge            │
│ BadgeType (enum)         │  BadgeRarity (enum)                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📝 Détails des Classes

### ENTITÉS (entity package)

#### **Badge.java**
```java
@Entity
@Table(name = "badges")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── id: Long (PK)
├── code: String (UNIQUE)
├── name: String
├── description: String
├── iconUrl: String
├── type: BadgeType (ENUM)
├── rarity: BadgeRarity (ENUM)
├── pointsRequired: Integer
├── active: Boolean
└── createdAt: Instant (@PrePersist)
```

**Comportement:**
- @PrePersist: Définit createdAt à Instant.now()
- Unique constraint sur code
- Soft delete via active flag

#### **UserBadge.java**
```java
@Entity
@Table(name = "user_badges")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── id: Long (PK)
├── userId: Long (INDEXED)
├── badgeId: Long (FK, INDEXED)
├── earnedAt: Instant (@PrePersist)
└── UNIQUE(userId, badgeId)
```

**Comportement:**
- Composite unique constraint (userId, badgeId)
- FK ON DELETE CASCADE
- @PrePersist: earnedAt = Instant.now()

#### **BadgeType.java** (Enum)
```java
enum BadgeType {
    ACTION,        // Récompense pour action simple
    ACHIEVEMENT,   // Accomplissement
    MILESTONE      // Jalon important
}
```

#### **BadgeRarity.java** (Enum)
```java
enum BadgeRarity {
    COMMON,        // 0-10%
    UNCOMMON,      // 10-25%
    RARE,          // 25-50%
    EPIC,          // 50-75%
    LEGENDARY      // 75-100%
}
```

### EXCEPTIONS (exception package)

#### **BadgeNotFoundException.java**
```java
public class BadgeNotFoundException extends RuntimeException {
    public BadgeNotFoundException(String message)
    public BadgeNotFoundException(String message, Throwable cause)
}
```

**Utilisation:**
- Levée dans BadgeRepository.findById().orElseThrow()
- Levée dans BadgeService quand badge not found

#### **UserScoreNotFoundException.java**
```java
public class UserScoreNotFoundException extends RuntimeException {
    public UserScoreNotFoundException(String message)
    public UserScoreNotFoundException(String message, Throwable cause)
}
```

**Utilisation:**
- Levée dans LeaderboardService
- Levée dans GamificationService

### REPOSITORIES (repository package)

#### **BadgeRepository.java**
```java
@Repository
extends JpaRepository<Badge, Long>
├── findByCode(String code): Optional<Badge>
├── findByActive(Boolean): List<Badge>
├── findByRarity(String, Pageable): Page<Badge>
├── findByType(String, Pageable): Page<Badge>
└── countByActive(Boolean): long
```

**Queries:**
- Toutes sont auto-générées par Spring Data
- Nécessitent des indexes pour performance

#### **UserBadgeRepository.java**
```java
@Repository
extends JpaRepository<UserBadge, Long>
├── findByUserId(Long): List<UserBadge>
├── findByUserIdAndBadgeId(Long, Long): Optional<UserBadge>
├── countByUserId(Long): long
├── @Query: countUniqueBadgesByUserId(Long): long
└── existsByUserIdAndBadgeId(Long, Long): boolean
```

**Queries:**
- findByUserId: Index sur user_id
- existsByUserIdAndBadgeId: Utilisé pour vérifier duplicatas
- Custom @Query pour count DISTINCT

#### **LeaderboardRepository.java**
```java
@Repository
@RequiredArgsConstructor
├── getGlobalLeaderboard(Pageable): Page<LeaderboardEntryResponse>
│   └── SQL: SELECT user_id, level, total_points, COUNT badges
│       ORDER BY total_points DESC, level DESC
└── getWeeklyLeaderboard(int limit): List<LeaderboardEntryResponse>
    └── SQL: WHERE updated_at >= CURRENT_DATE - 7 DAYS
```

**Caractéristiques:**
- SQL native queries pour performance
- EntityManager.createNativeQuery()
- Convertion manuelle des Object[] en DTOs

### DTOs (dto package)

#### **BadgeResponse.java**
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── id: Long
├── code: String
├── name: String
├── description: String
├── iconUrl: String
├── type: String
├── rarity: String
├── pointsRequired: Integer
├── active: Boolean
└── createdAt: Instant
```

**Utilisation:**
- Response API pour GET /badges
- Mappage depuis Badge entity

#### **CreateBadgeRequest.java**
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── code: String
├── name: String
├── description: String
├── iconUrl: String
├── type: String (enum name)
├── rarity: String (enum name)
└── pointsRequired: Integer
```

**Validation:**
- À ajouter: @NotBlank, @Min, etc.

#### **UserBadgeResponse.java**
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── badgeId: Long
├── code: String
├── name: String
├── iconUrl: String
├── rarity: String
└── earnedAt: Instant
```

**Utilisation:**
- Response API pour GET /user-badges/{userId}

#### **LeaderboardEntryResponse.java**
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── userId: Long
├── username: String
├── level: Integer
├── totalPoints: Integer
├── rank: Integer
└── badgeCount: Long
```

**Utilisation:**
- Response API pour leaderboards
- Convertie depuis native query results

#### **UserPublicProfileResponse.java**
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
├── userId: Long
├── username: String
├── level: Integer
├── totalPoints: Integer
├── rank: Integer
├── badgeCount: Long
└── badges: List<UserBadgeResponse>
```

**Utilisation:**
- Profil public d'un utilisateur
- Combination de UserScore + badges + leaderboard

### SERVICES (service package)

#### **BadgeService.java** (Interface)
```java
├── createBadge(CreateBadgeRequest): Badge
├── getBadgeById(Long): BadgeResponse
├── getBadgeByCode(String): BadgeResponse
├── getAllBadges(): List<BadgeResponse>
├── getActiveBadges(): List<BadgeResponse>
├── updateBadge(Long, CreateBadgeRequest): Badge
└── deleteBadge(Long): void
```

#### **BadgeServiceImp.java** (Implémentation)
```java
@Service @RequiredArgsConstructor @Slf4j @Transactional
├── private final BadgeRepository badgeRepository
├── createBadge()
│   ├── log.info("Creating badge...")
│   ├── Build Badge from request
│   ├── badgeRepository.save()
│   └── log.info("Badge created")
├── getBadgeById()
│   ├── badgeRepository.findById()
│   ├── .orElseThrow(BadgeNotFoundException)
│   └── mapToResponse()
├── updateBadge()
│   ├── Get existing badge
│   ├── Update fields
│   ├── Save
│   └── Log
└── mapToResponse(Badge): BadgeResponse
    └── Builder conversion
```

#### **UserBadgeService.java** (Interface)
```java
├── awardBadgeToUser(Long userId, Long badgeId): void
├── getUserBadges(Long userId): List<UserBadgeResponse>
├── getUserBadgeCount(Long userId): long
└── userHasBadge(Long userId, Long badgeId): boolean
```

#### **UserBadgeServiceImp.java** (Implémentation)
```java
@Service @RequiredArgsConstructor @Slf4j @Transactional
├── private final UserBadgeRepository userBadgeRepository
├── private final BadgeRepository badgeRepository
├── private final UserScoreRepository userScoreRepository
├── awardBadgeToUser()
│   ├── Verify user exists
│   ├── Verify badge exists
│   ├── Check if already has badge
│   ├── Create and save UserBadge
│   └── Log
├── getUserBadges()
│   ├── Find all by userId
│   ├── Stream and load Badge data
│   └── mapToResponse()
├── getUserBadgeCount()
│   └── userBadgeRepository.countByUserId()
└── userHasBadge()
    └── userBadgeRepository.existsByUserIdAndBadgeId()
```

#### **LeaderboardService.java** (Interface)
```java
├── getGlobalLeaderboard(int limit): List<LeaderboardEntryResponse>
├── getWeeklyLeaderboard(int limit): List<LeaderboardEntryResponse>
├── getUserRank(Long userId): Integer
└── getUserRankPercentage(Long userId): Long
```

#### **LeaderboardServiceImp.java** (Implémentation)
```java
@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly=true)
├── private final LeaderboardRepository leaderboardRepository
├── private final UserScoreRepository userScoreRepository
├── getGlobalLeaderboard()
│   ├── leaderboardRepository.getGlobalLeaderboard(pageable)
│   └── Stream et limit
├── getWeeklyLeaderboard()
│   ├── leaderboardRepository.getWeeklyLeaderboard(limit)
├── getUserRank()
│   ├── Get user's total points
│   ├── Get all scores
│   ├── Count how many have more points
│   └── Return rank (1-based)
└── getUserRankPercentage()
    ├── Get rank
    ├── Calculate percentage
    └── Return 0-100
```

#### **GamificationService.java** (Interface - Mise à Jour)
```java
├── addPoints(Long userId, AddPointsRequest): UserScore
├── getUserScore(Long userId): UserScoreResponse
└── getUserPublicProfile(Long userId): UserPublicProfileResponse [NEW]
```

#### **GamificationServiceImp.java** (Implémentation - Mise à Jour)
```java
@Service @RequiredArgsConstructor @Slf4j @Transactional
├── private final UserScoreRepository userScoreRepository
├── private final UserBadgeService userBadgeService [NEW]
├── private final LeaderboardService leaderboardService [NEW]
├── getUserPublicProfile() [NEW]
│   ├── Get user score
│   ├── Get rank from leaderboardService
│   ├── Get badges from userBadgeService
│   └── Build UserPublicProfileResponse
└── [existingMethods...]
```

### CONTROLLERS (controller package)

#### **GamificationController.java** (Mise à Jour)
```java
@RestController @RequestMapping("/api/gamification")
├── GET /score (header: X-User-Id)
│   └── gamificationService.getUserScore(userId)
├── GET /score/{userId}
│   └── gamificationService.getUserScore(userId)
├── GET /profile/{userId} [NEW]
│   └── gamificationService.getUserPublicProfile(userId)
└── POST /points
    └── gamificationService.addPoints(userId, request)
```

#### **BadgeController.java**
```java
@RestController @RequestMapping("/api/gamification/badges")
├── GET /{badgeId}
│   └── badgeService.getBadgeById(badgeId)
├── GET /code/{code}
│   └── badgeService.getBadgeByCode(code)
├── GET
│   └── badgeService.getAllBadges()
├── GET /active
│   └── badgeService.getActiveBadges()
├── POST
│   └── badgeService.createBadge(request)
├── PUT /{badgeId}
│   └── badgeService.updateBadge(badgeId, request)
└── DELETE /{badgeId}
    └── badgeService.deleteBadge(badgeId)
```

#### **UserBadgeController.java**
```java
@RestController @RequestMapping("/api/gamification/user-badges")
├── GET /{userId}
│   └── userBadgeService.getUserBadges(userId)
├── GET /{userId}/count
│   └── userBadgeService.getUserBadgeCount(userId)
├── GET /{userId}/has/{badgeId}
│   └── userBadgeService.userHasBadge(userId, badgeId)
└── POST /{userId}/award/{badgeId}
    └── userBadgeService.awardBadgeToUser(userId, badgeId)
```

#### **LeaderboardController.java**
```java
@RestController @RequestMapping("/api/gamification/leaderboard")
├── GET /global?limit=100
│   └── leaderboardService.getGlobalLeaderboard(limit)
├── GET /weekly?limit=50
│   └── leaderboardService.getWeeklyLeaderboard(limit)
├── GET /rank/{userId}
│   └── leaderboardService.getUserRank(userId)
└── GET /rank-percentage/{userId}
    └── leaderboardService.getUserRankPercentage(userId)
```

## 🔄 Flux d'Exécution - Ajouter des Points

```
POST /api/gamification/points
    ↓
GamificationController.addPoints()
    ├── gamificationService.addPoints(userId, request)
    │   ├── userScoreRepository.findByUserId(userId)
    │   ├── UserScore.addPoints(points)
    │   │   ├── totalPoints += points
    │   │   ├── currentLevelPoints += points
    │   │   ├── while currentLevelPoints >= pointsForNextLevel:
    │   │   │   ├── currentLevelPoints -= pointsForNextLevel
    │   │   │   ├── level++
    │   │   │   └── pointsForNextLevel = 100 + (level * 50)
    │   └── userScoreRepository.save(userScore)
    └── ResponseEntity.status(201).body(userScoreResponse)
```

## 🔄 Flux d'Exécution - Attribuer Badge

```
POST /api/gamification/user-badges/{userId}/award/{badgeId}
    ↓
UserBadgeController.awardBadgeToUser()
    ├── userBadgeService.awardBadgeToUser(userId, badgeId)
    │   ├── userScoreRepository.findByUserId(userId)
    │   │   └── .orElseThrow(UserScoreNotFoundException)
    │   ├── badgeRepository.findById(badgeId)
    │   │   └── .orElseThrow(BadgeNotFoundException)
    │   ├── userBadgeRepository.existsByUserIdAndBadgeId()
    │   │   └── if true, return (already has badge)
    │   └── userBadgeRepository.save(new UserBadge())
    └── ResponseEntity.status(201).build()
```

## 📦 Configuration Maven

**Dépendances requises** (dans pom.xml):
```xml
<!-- Spring Data JPA - DejaVu inclus -->
<!-- Lombok - DejaVu inclus -->
<!-- Jakarta Persistence - DejaVu inclus -->
```

**Aucune nouvelle dépendance requise!**

---

**Architecture complète et bien structurée!** ✨

