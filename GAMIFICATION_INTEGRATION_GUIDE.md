# 🔌 GUIDE D'INTÉGRATION - MODULE GAMIFICATION

## Vue d'ensemble de l'intégration

Le module de gamification s'intègre avec les autres modules via un système **d'événements Spring**. Quand une action se produit (création de post, acceptation de réponse, etc.), un événement est publié et le listener de gamification le capture pour ajouter des points.

---

## Architecture d'intégration

```
┌─────────────────────────────────────┐
│      User Action                    │
│  (Create Post, Answer Question)     │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│   Module (Community, Matching)      │
│   Publishes Event                   │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│   Event Published                   │
│   (PostCreatedEvent, etc)           │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│   CommunityGamificationListener     │
│   Captures Event via @EventListener │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│   GamificationService               │
│   addPoints(userId, points, ...)    │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│   UserScore Updated                 │
│   Database saved                    │
└─────────────────────────────────────┘
```

---

## Événements et Points Associés

### Événements du Module Community

| Événement | Points | Condition |
|-----------|--------|-----------|
| `PostCreatedEvent` (SUMMARY) | 10 | Post créé avec résumé |
| `PostCreatedEvent` (TUTORIAL) | 15 | Post créé avec tutoriel |
| `PostCreatedEvent` (DISCUSSION) | 8 | Post créé pour discussion |
| `QuestionAskedEvent` | 5 | Question posée |
| `AnswerProvidedEvent` | 10 | Réponse fournie |
| `AnswerAcceptedEvent` | 50 | Réponse acceptée comme meilleure |
| `AnswerUpvotedEvent` | 5 | Réponse upvotée |
| `PostLikedEvent` | 2 | Post liké |
| `CommentCreatedEvent` | 3 | Commentaire créé |

### Événements du Module Matching

| Événement | Points |
|-----------|--------|
| `ConnectionRequestAcceptedEvent` | 10 |
| `StudyGroupCreatedEvent` | 25 |

### Événements du Module Planner

| Événement | Points |
|-----------|--------|
| `TaskCompletedEvent` | 10 |
| `DailyStreakEvent` | 25 |

---

## Points d'intégration détaillés

### 1. Intégration avec le Module Community

**Fichier à modifier**: `src/main/java/org/example/learnlink/modules/community/listener/CommunityGamificationListener.java`

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
    
    /**
     * Handle post created event - award points based on post type
     */
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.info("Post created event received: userId={}, postId={}, postType={}",
            event.getUserId(), event.getPostId(), event.getPostType());
        
        int points = switch (event.getPostType()) {
            case SUMMARY -> 10;
            case TUTORIAL -> 15;
            case DISCUSSION -> 8;
        };
        
        gamificationService.addPoints(
            event.getUserId(), 
            points, 
            "POST_CREATED", 
            String.format("Post créé (%s)", event.getPostType())
        );
    }
    
    /**
     * Handle question asked event
     */
    @EventListener
    public void handleQuestionAskedEvent(QuestionAskedEvent event) {
        log.info("Question asked event received: userId={}, questionId={}",
            event.getUserId(), event.getQuestionId());
        
        gamificationService.addPoints(
            event.getUserId(), 
            5, 
            "QUESTION_ASKED", 
            "Question posée"
        );
    }
    
    /**
     * Handle answer provided event
     */
    @EventListener
    public void handleAnswerProvidedEvent(AnswerProvidedEvent event) {
        log.info("Answer provided event received: userId={}, answerId={}",
            event.getUserId(), event.getAnswerId());
        
        gamificationService.addPoints(
            event.getUserId(), 
            10, 
            "ANSWER_PROVIDED", 
            "Réponse fournie"
        );
    }
    
    /**
     * Handle answer accepted event - award bonus points
     */
    @EventListener
    public void handleAnswerAcceptedEvent(AnswerAcceptedEvent event) {
        log.info("Answer accepted event received: answerId={}, answerAuthorId={}",
            event.getAnswerId(), event.getAnswerAuthorId());
        
        gamificationService.addPoints(
            event.getAnswerAuthorId(), 
            50, 
            "ANSWER_ACCEPTED", 
            "Réponse acceptée comme meilleure"
        );
    }
    
    /**
     * Handle answer upvoted event
     */
    @EventListener
    public void handleAnswerUpvotedEvent(AnswerUpvotedEvent event) {
        log.info("Answer upvoted event received: answerId={}, answerAuthorId={}",
            event.getAnswerId(), event.getAnswerAuthorId());
        
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

### 2. Intégration avec le Module Matching

**Créer un nouveau fichier**: `src/main/java/org/example/learnlink/modules/matching/listener/MatchingGamificationListener.java`

```java
package org.example.learnlink.modules.matching.listener;

import org.example.learnlink.modules.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingGamificationListener {
    
    private final GamificationService gamificationService;
    
    /**
     * Handle connection request accepted event
     */
    @EventListener
    public void handleConnectionRequestAcceptedEvent(ConnectionRequestAcceptedEvent event) {
        log.info("Connection request accepted: userId={}", event.getConnectorUserId());
        
        // Points pour celui qui a accepté
        gamificationService.addPoints(
            event.getConnectorUserId(), 
            10, 
            "CONNECTION_ACCEPTED", 
            "Connexion acceptée"
        );
    }
    
    /**
     * Handle study group created event
     */
    @EventListener
    public void handleStudyGroupCreatedEvent(StudyGroupCreatedEvent event) {
        log.info("Study group created: creatorId={}, groupId={}", 
            event.getCreatorId(), event.getGroupId());
        
        // Points pour le créateur du groupe
        gamificationService.addPoints(
            event.getCreatorId(), 
            25, 
            "STUDY_GROUP_CREATED", 
            "Groupe d'étude créé"
        );
    }
}
```

---

### 3. Intégration avec le Module Planner

**Créer un nouveau fichier**: `src/main/java/org/example/learnlink/modules/planner/listener/PlannerGamificationListener.java`

```java
package org.example.learnlink.modules.planner.listener;

import org.example.learnlink.modules.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlannerGamificationListener {
    
    private final GamificationService gamificationService;
    
    /**
     * Handle task completed event
     */
    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        log.info("Task completed: userId={}, taskId={}", 
            event.getUserId(), event.getTaskId());
        
        gamificationService.addPoints(
            event.getUserId(), 
            10, 
            "TASK_COMPLETED", 
            "Tâche complétée"
        );
    }
    
    /**
     * Handle daily streak event
     */
    @EventListener
    public void handleDailyStreakEvent(DailyStreakEvent event) {
        log.info("Daily streak milestone: userId={}, days={}", 
            event.getUserId(), event.getStreakDays());
        
        // Bonus points basé sur le nombre de jours
        int points = switch (event.getStreakDays()) {
            case 7 -> 50;   // Une semaine
            case 14 -> 100; // Deux semaines
            case 30 -> 200; // Un mois
            default -> 25;
        };
        
        gamificationService.addPoints(
            event.getUserId(), 
            points, 
            "DAILY_STREAK", 
            String.format("Streak de %d jours", event.getStreakDays())
        );
    }
}
```

---

## Ajouter des événements manquants

Si des événements n'existent pas encore, ils doivent être créés dans le module respectif.

### Exemple: PostCreatedEvent (si absent)

**Fichier**: `src/main/java/org/example/learnlink/modules/community/event/PostCreatedEvent.java`

```java
package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.UUID;

@Getter
public class PostCreatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final Long postId;
    private final PostType postType;
    
    public PostCreatedEvent(Object source, UUID userId, Long postId, PostType postType) {
        super(source);
        this.userId = userId;
        this.postId = postId;
        this.postType = postType;
    }
    
    public enum PostType {
        SUMMARY, TUTORIAL, DISCUSSION
    }
}
```

### Publier l'événement dans le Service

**Dans**: `src/main/java/org/example/learnlink/modules/community/service/PostServiceImpl.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {
    
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public Post createPost(UUID userId, CreatePostRequest request) {
        // ... créer le post ...
        Post post = postRepository.save(newPost);
        
        // Publier l'événement
        eventPublisher.publishEvent(new PostCreatedEvent(
            this, 
            userId, 
            post.getId(), 
            PostCreatedEvent.PostType.valueOf(request.getType())
        ));
        
        return post;
    }
}
```

---

## Configuration Spring (Application.properties)

Ajouter si nécessaire:

```properties
# Gamification Configuration
gamification.enabled=true
gamification.level-points-formula=100+(level*50)
gamification.max-level=50
```

---

## Dépendances Maven

Vérifier que `pom.xml` contient:

```xml
<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Boot Starter Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## Schéma de flux complet

```
1. User posts a question
   ↓
2. PostServiceImpl.createPost() saves post to DB
   ↓
3. PostServiceImpl publishes PostCreatedEvent
   ↓
4. Spring Event System dispatches event
   ↓
5. CommunityGamificationListener.handlePostCreatedEvent() captures event
   ↓
6. GamificationService.addPoints() is called
   ↓
7. UserScore is updated with new points
   ↓
8. User's level increases if threshold reached
   ↓
9. BadgeService checks for new badge unlocks
   ↓
10. Notification sent to user (optional, future phase)
```

---

## Tests d'intégration

### Tester l'ajout de points

```java
@SpringBootTest
@Transactional
class GamificationIntegrationTest {
    
    @Autowired
    private GamificationService gamificationService;
    
    @Autowired
    private UserScoreRepository userScoreRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    void testAddPointsViaEvent() {
        UUID userId = UUID.randomUUID();
        
        // Ajouter des points
        gamificationService.addPoints(userId, 50, "TEST", "Test");
        
        // Vérifier
        UserScore score = userScoreRepository.findByUserId(userId).orElseThrow();
        assertThat(score.getTotalPoints()).isEqualTo(50);
    }
    
    @Test
    void testLevelUp() {
        UUID userId = UUID.randomUUID();
        
        // Ajouter 100 points pour passer au niveau 2
        gamificationService.addPoints(userId, 100, "TEST", "Test");
        
        UserScore score = userScoreRepository.findByUserId(userId).orElseThrow();
        assertThat(score.getLevel()).isEqualTo(2);
    }
}
```

---

## Logs recommandés

Pour déboguer l'intégration, activer les logs dans `application.properties`:

```properties
logging.level.org.example.learnlink.modules.gamification=DEBUG
logging.level.org.example.learnlink.modules.community.listener=DEBUG
logging.level.org.example.learnlink.modules.matching.listener=DEBUG
logging.level.org.example.learnlink.modules.planner.listener=DEBUG
```

---

## Points clés à retenir

1. **Événements asynchrones** - Les listeners traitent les événements de manière asynchrone
2. **Transactions** - Les services sont annotés avec `@Transactional`
3. **Injection de dépendances** - Utiliser `@RequiredArgsConstructor` avec Lombok
4. **Logging** - Toujours logger les opérations importantes
5. **Validation** - Valider les données entrantes avec `@Valid`
6. **Isolation des modules** - Chaque module reste indépendant
7. **Événements découpés** - Un événement = une action utilisateur


