# Guide d'Intégration du Module Gamification

## 📌 Vue d'ensemble

Ce guide explique comment intégrer le module de gamification avec les autres modules (Community, Planner, Matching).

## 🔗 Intégration avec Community Module

### Quand un post est créé
```java
// Dans PostServiceImp.java
@Override
public Post createPost(Long userId, CreatePostRequest request) {
    // ... créer le post ...
    Post post = postRepository.save(newPost);
    
    // Ajouter points
    AddPointsRequest pointsRequest = AddPointsRequest.builder()
            .points(8)
            .actionType("POST_CREATED")
            .build();
    gamificationService.addPoints(userId, pointsRequest);
    
    return post;
}
```

### Quand une réponse est acceptée
```java
// Dans AnswerServiceImp.java
@Override
public Answer acceptAnswer(Long questionId, Long answerId, Long userId) {
    // ... accepter la réponse ...
    Answer answer = answerRepository.save(acceptedAnswer);
    
    // Ajouter points au répondeur
    AddPointsRequest pointsRequest = AddPointsRequest.builder()
            .points(25)
            .actionType("ANSWER_ACCEPTED")
            .build();
    gamificationService.addPoints(answer.getUserId(), pointsRequest);
    
    // Optionnel: Attribuer badge
    userBadgeService.awardBadgeToUser(answer.getUserId(), 2); // HELPFUL_EXPERT
    
    return answer;
}
```

### Quand un post reçoit un like
```java
// Dans PostServiceImp.java
@Override
public void likePost(Long postId, Long userId) {
    // ... ajouter le like ...
    
    // Ajouter points à l'auteur du post
    Post post = postRepository.findById(postId).orElseThrow();
    AddPointsRequest pointsRequest = AddPointsRequest.builder()
            .points(2)
            .actionType("POST_LIKED")
            .build();
    gamificationService.addPoints(post.getUserId(), pointsRequest);
}
```

## 🔗 Intégration avec Planner Module

### Quand une tâche est complétée
```java
// Dans TaskServiceImp.java
@Override
public Task completeTask(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    
    task.setCompleted(true);
    task.setCompletedAt(Instant.now());
    task = taskRepository.save(task);
    
    // Ajouter points
    AddPointsRequest pointsRequest = AddPointsRequest.builder()
            .points(10)
            .actionType("TASK_COMPLETED")
            .build();
    gamificationService.addPoints(userId, pointsRequest);
    
    return task;
}
```

### Streak counter
```java
// Optionnel: Ajouter 5 points par jour de streak
// Vérifier si une tâche a été complétée aujourd'hui

public void checkAndRewardStreak(Long userId) {
    LocalDate today = LocalDate.now();
    boolean completedToday = taskRepository.existsByUserIdAndCompletedAtBetween(
            userId,
            today.atStartOfDay().toInstant(ZoneOffset.UTC),
            today.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
    );
    
    if (completedToday) {
        AddPointsRequest pointsRequest = AddPointsRequest.builder()
                .points(5)
                .actionType("DAILY_STREAK")
                .build();
        gamificationService.addPoints(userId, pointsRequest);
    }
}
```

## 🔗 Intégration avec Matching Module

### Quand une connexion est établie
```java
// Dans StudentMatchServiceImp.java
@Override
public void acceptMatch(Long matchId, Long userId) {
    StudentMatch match = matchRepository.findById(matchId).orElseThrow();
    
    match.setStatus(MatchStatus.ACCEPTED);
    matchRepository.save(match);
    
    // Ajouter points aux deux utilisateurs
    AddPointsRequest pointsRequest = AddPointsRequest.builder()
            .points(5)
            .actionType("CONNECTION_MADE")
            .build();
    
    gamificationService.addPoints(userId, pointsRequest);
    gamificationService.addPoints(match.getStudentId(), pointsRequest);
    
    // Optionnel: Attribuer badge
    userBadgeService.awardBadgeToUser(userId, 7); // SOCIAL_BUTTERFLY
    userBadgeService.awardBadgeToUser(match.getStudentId(), 7);
}
```

## 🎁 Attribution Automatique de Badges

### À la création du premier post
```java
// Dans PostServiceImp.java
public void checkAndAwardBadges(Long userId) {
    long postCount = postRepository.countByUserId(userId);
    
    if (postCount == 1) {
        userBadgeService.awardBadgeToUser(userId, 1); // FIRST_POST
    }
}
```

### À atteindre 1000 points
```java
// Dans GamificationServiceImp.java (après addPoints)
UserScore userScore = userScoreRepository.findByUserId(userId).orElseThrow();

if (userScore.getTotalPoints() >= 1000 && 
    !userBadgeService.userHasBadge(userId, 3)) { // COMMUNITY_LEADER
    userBadgeService.awardBadgeToUser(userId, 3);
}

if (userScore.getLevel() >= 10 && 
    !userBadgeService.userHasBadge(userId, 5)) { // LEGEND
    userBadgeService.awardBadgeToUser(userId, 5);
}
```

### À 100 questions posées
```java
// Dans QuestionServiceImp.java
public void checkAndAwardBadges(Long userId) {
    long questionCount = questionRepository.countByUserId(userId);
    
    if (questionCount == 100 && 
        !userBadgeService.userHasBadge(userId, 6)) { // QUESTION_MASTER
        userBadgeService.awardBadgeToUser(userId, 6);
    }
}
```

## 📊 Tableau Synthétique des Points

| Action | Points | Service | Conditions |
|--------|--------|---------|-----------|
| Post créé | 8 | Community | À chaque post |
| Réponse acceptée | 25 | Community | Une fois par réponse |
| Post liké | 2 | Community | À chaque like |
| Tâche complétée | 10 | Planner | Une fois par tâche |
| Connexion établie | 5 | Matching | Une fois par paire |
| Streak journalier | 5 | Planner | Une fois par jour |

## 🏆 Tableau Synthétique des Badges

| Badge | Condition | Points | Type |
|-------|-----------|--------|------|
| First Step | 1er post | - | ACTION |
| Helpful Expert | 50 votes utiles | 500 | ACHIEVEMENT |
| Community Leader | 1000 points | 1000 | MILESTONE |
| Streak Master | 30 jours streak | 800 | MILESTONE |
| Legend | Niveau 10 | 2000 | MILESTONE |
| Question Master | 100 questions | 600 | ACHIEVEMENT |
| Social Butterfly | 50 connexions | 300 | ACHIEVEMENT |
| Knowledge Seeker | 5 challenges | 400 | ACHIEVEMENT |

## 💾 Injection du Service dans les autres modules

### Exemple dans Community Module
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImp implements PostService {
    
    private final PostRepository postRepository;
    private final GamificationService gamificationService; // ← Ajouter
    private final UserBadgeService userBadgeService;       // ← Ajouter
    
    // ... reste du code ...
}
```

## 🔐 Vérifications Avant Intégration

✅ GamificationService est injecté
✅ GamificationServiceImp implémente l'interface
✅ AddPointsRequest est créé avec les bons paramètres
✅ userId est valide (existant dans user_scores)
✅ Transactions @Transactional sont utilisées
✅ Points sont positifs

## 📝 Exemple Complet - Integration Community

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImp implements PostService {
    
    private final PostRepository postRepository;
    private final GamificationService gamificationService;
    private final UserBadgeService userBadgeService;
    
    @Override
    public Post createPost(Long userId, CreatePostRequest request) {
        log.info("Creating post for user: {}", userId);
        
        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .type(PostType.valueOf(request.getType()))
                .category(request.getCategory())
                .build();
        
        post = postRepository.save(post);
        
        // ← GAMIFICATION
        try {
            AddPointsRequest pointsRequest = AddPointsRequest.builder()
                    .points(8)
                    .actionType("POST_CREATED")
                    .build();
            gamificationService.addPoints(userId, pointsRequest);
            log.info("Points awarded for post creation");
            
            // Vérifier badge first post
            long postCount = postRepository.countByUserId(userId);
            if (postCount == 1) {
                userBadgeService.awardBadgeToUser(userId, 1); // FIRST_POST
                log.info("Badge FIRST_POST awarded");
            }
        } catch (Exception e) {
            log.warn("Failed to award gamification: {}", e.getMessage());
            // Ne pas bloquer la création du post
        }
        
        return post;
    }
}
```

## 🎯 Points d'Attention

1. **Try-catch**: Gamification ne doit pas bloquer les opérations principales
2. **Async**: Considérez l'exécution asynchrone des attributions de badges
3. **Tests**: Tester l'intégration avec du MockMvc
4. **Erreurs**: Logger les erreurs de gamification sans les propager

## 📞 Support

Pour questions sur l'intégration:
1. Voir `GAMIFICATION_ENDPOINTS.md` pour tous les endpoints
2. Voir `GAMIFICATION_IMPLEMENTATION_SUMMARY.md` pour l'architecture
3. Vérifier les exemples cURL dans la documentation

---

**Bon codage! 🚀**

