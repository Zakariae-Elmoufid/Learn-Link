# Event Integration - Community & Gamification

## 📊 Intégration Complète

Vous avez maintenant une intégration complète entre le module Community et le module Gamification via des événements Spring!

## 🎯 Architecture des Événements

```
Community Module
    ↓ (publishes events)
Spring Event Publisher
    ↓ (broadcasts)
CommunityGamificationListener
    ↓ (listens & processes)
GamificationService
    ↓ (adds points)
UserScore (database)
    ↑
Utilisateur gagne des points!
```

## 📋 Événements Gérés

### 1️⃣ PostCreatedEvent
```
Quand: Un utilisateur crée un post
Points:
  - SUMMARY: 10 points
  - TUTORIAL: 15 points
  - DISCUSSION: 8 points
  - Autre: 5 points

Badges:
  - FIRST_POST (ID 1) - Automatiquement attribué
```

**Exemple de publication:**
```java
// Dans PostServiceImp.java
Post post = postRepository.save(newPost);
applicationEventPublisher.publishEvent(
    new PostCreatedEvent(userId, post.getId(), post.getType())
);
```

---

### 2️⃣ QuestionAskedEvent
```
Quand: Un utilisateur pose une question
Points: 5 points

ActionType: QUESTION_ASKED
```

**Exemple de publication:**
```java
// Dans QuestionServiceImp.java
Question question = questionRepository.save(newQuestion);
applicationEventPublisher.publishEvent(
    new QuestionAskedEvent(userId, question.getId())
);
```

---

### 3️⃣ AnswerProvidedEvent
```
Quand: Un utilisateur fournit une réponse
Points: 10 points

ActionType: ANSWER_PROVIDED
```

**Exemple de publication:**
```java
// Dans AnswerServiceImp.java
Answer answer = answerRepository.save(newAnswer);
applicationEventPublisher.publishEvent(
    new AnswerProvidedEvent(userId, answer.getId(), question.getId())
);
```

---

### 4️⃣ AnswerAcceptedEvent
```
Quand: La réponse est acceptée comme meilleure
Points: 25 points BONUS!

ActionType: ANSWER_ACCEPTED

Badges:
  - HELPFUL_EXPERT (ID 2) - Automatiquement attribué
```

**Exemple de publication:**
```java
// Dans AnswerServiceImp.java
answer.setAccepted(true);
Answer saved = answerRepository.save(answer);
applicationEventPublisher.publishEvent(
    new AnswerAcceptedEvent(
        answer.getId(), 
        answer.getUserId(),      // répondeur
        question.getUserId()     // poseur
    )
);
```

---

### 5️⃣ AnswerUpvotedEvent
```
Quand: Quelqu'un upvote une réponse
Points: 2 points par upvote

ActionType: ANSWER_UPVOTED
```

**Exemple de publication:**
```java
// Dans AnswerServiceImp.java
answer.incrementUpvotes();
answerRepository.save(answer);
applicationEventPublisher.publishEvent(
    new AnswerUpvotedEvent(answer.getId(), answer.getUserId(), voterId)
);
```

---

### 6️⃣ CommentAddedEvent
```
Quand: Un utilisateur ajoute un commentaire
Points: 2 points

ActionType: COMMENT_ADDED
```

**Exemple de publication:**
```java
// Dans CommentServiceImp.java
Comment comment = commentRepository.save(newComment);
applicationEventPublisher.publishEvent(
    new CommentAddedEvent(
        userId, 
        comment.getId(), 
        post?.getId(), 
        answer?.getId()
    )
);
```

---

### 7️⃣ PostLikedEvent
```
Quand: Quelqu'un like un post
Points: 1 point (pour l'auteur du post)

ActionType: POST_LIKED
```

**Exemple de publication:**
```java
// Dans PostServiceImp.java
post.incrementLikes();
postRepository.save(post);
applicationEventPublisher.publishEvent(
    new PostLikedEvent(post.getId(), post.getUserId(), userId)
);
```

## 🔧 Comment Implémenter les Événements

### Étape 1: Créer les Event Classes
Dans `community/event/`:

```java
@Getter
@AllArgsConstructor
public class PostCreatedEvent extends ApplicationEvent {
    private final Long userId;
    private final UUID postId;
    private final PostType postType;
    
    public PostCreatedEvent(Object source, Long userId, UUID postId, PostType postType) {
        super(source);
        this.userId = userId;
        this.postId = postId;
        this.postType = postType;
    }
}
```

### Étape 2: Publier les Événements
Dans les services (`*ServiceImp.java`):

```java
@Service
@RequiredArgsConstructor
public class PostServiceImp implements PostService {
    private final ApplicationEventPublisher eventPublisher;
    
    public Post createPost(CreatePostRequest request) {
        Post post = postRepository.save(newPost);
        
        // Publier l'événement
        eventPublisher.publishEvent(
            new PostCreatedEvent(this, request.getUserId(), post.getId(), post.getType())
        );
        
        return post;
    }
}
```

### Étape 3: Écouter les Événements
Le `CommunityGamificationListener` écoute déjà tous les événements!

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class CommunityGamificationListener {
    private final GamificationService gamificationService;
    
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        // Ajouter les points
        gamificationService.addPoints(event.getUserId(), request);
        
        // Attribuer les badges
        userBadgeService.awardBadgeToUser(event.getUserId(), badgeId);
    }
}
```

## 📊 Résumé des Points par Événement

| Événement | Points | ActionType | Badge |
|-----------|--------|-----------|-------|
| Post SUMMARY | 10 | POST_CREATED | FIRST_POST |
| Post TUTORIAL | 15 | POST_CREATED | FIRST_POST |
| Post DISCUSSION | 8 | POST_CREATED | FIRST_POST |
| Question Asked | 5 | QUESTION_ASKED | - |
| Answer Provided | 10 | ANSWER_PROVIDED | - |
| Answer Accepted | 25 | ANSWER_ACCEPTED | HELPFUL_EXPERT |
| Answer Upvoted | 2 | ANSWER_UPVOTED | - |
| Comment Added | 2 | COMMENT_ADDED | - |
| Post Liked | 1 | POST_LIKED | - |

## 🎯 Mise en Place Complète

### Pour PostServiceImp
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImp implements PostService {
    
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public Post createPost(Long userId, CreatePostRequest request) {
        log.info("Creating post: userId={}", userId);
        
        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .type(PostType.valueOf(request.getType()))
                .category(request.getCategory())
                .build();
        
        post = postRepository.save(post);
        
        // ✅ Publier l'événement
        eventPublisher.publishEvent(
            new PostCreatedEvent(this, userId, post.getId(), post.getType())
        );
        
        return post;
    }
    
    @Override
    public void likePost(UUID postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
        
        // ✅ Publier l'événement
        eventPublisher.publishEvent(
            new PostLikedEvent(this, postId, post.getUserId(), userId)
        );
    }
}
```

### Pour QuestionServiceImp
```java
@Override
public Question askQuestion(Long userId, CreateQuestionRequest request) {
    Question question = Question.builder()
            .userId(userId)
            .title(request.getTitle())
            .content(request.getContent())
            .tags(request.getTags())
            .build();
    
    question = questionRepository.save(question);
    
    // ✅ Publier l'événement
    eventPublisher.publishEvent(
        new QuestionAskedEvent(this, userId, question.getId())
    );
    
    return question;
}
```

### Pour AnswerServiceImp
```java
@Override
public Answer provideAnswer(Long userId, UUID questionId, String content) {
    Answer answer = Answer.builder()
            .userId(userId)
            .questionId(questionId)
            .content(content)
            .build();
    
    answer = answerRepository.save(answer);
    
    // ✅ Publier l'événement
    eventPublisher.publishEvent(
        new AnswerProvidedEvent(this, userId, answer.getId(), questionId)
    );
    
    return answer;
}

@Override
public void acceptAnswer(UUID answerId) {
    Answer answer = answerRepository.findById(answerId).orElseThrow();
    answer.setAccepted(true);
    answer = answerRepository.save(answer);
    
    Question question = questionRepository.findById(answer.getQuestionId()).orElseThrow();
    
    // ✅ Publier l'événement
    eventPublisher.publishEvent(
        new AnswerAcceptedEvent(this, answerId, answer.getUserId(), question.getUserId())
    );
}

@Override
public void upvoteAnswer(UUID answerId, Long userId) {
    Answer answer = answerRepository.findById(answerId).orElseThrow();
    answer.setUpvotes(answer.getUpvotes() + 1);
    answerRepository.save(answer);
    
    // ✅ Publier l'événement
    eventPublisher.publishEvent(
        new AnswerUpvotedEvent(this, answerId, answer.getUserId(), userId)
    );
}
```

### Pour CommentServiceImp
```java
@Override
public Comment addComment(Long userId, UUID postId, UUID answerId, String content) {
    Comment comment = Comment.builder()
            .userId(userId)
            .postId(postId)
            .answerId(answerId)
            .content(content)
            .build();
    
    comment = commentRepository.save(comment);
    
    // ✅ Publier l'événement
    eventPublisher.publishEvent(
        new CommentAddedEvent(this, userId, comment.getId(), postId, answerId)
    );
    
    return comment;
}
```

## ✅ Avantages du Pattern Événements

✅ **Découplage**: Community ne connaît pas Gamification
✅ **Extensibilité**: Ajouter d'autres listeners facilement
✅ **Testabilité**: Mocker les événements dans les tests
✅ **Asynchrone**: Les événements peuvent être traités en async
✅ **Maintenabilité**: Logique métier séparée de la gamification

## 🔄 Flux Complet d'Exemple

```
1. Utilisateur crée un post
   POST /api/community/posts { title, content, type: "SUMMARY" }
   
2. PostServiceImp.createPost()
   ├─ Sauvegarde le post en DB
   ├─ Publie PostCreatedEvent(userId=1, postId=uuid, postType=SUMMARY)
   └─ Retourne le post créé

3. Spring Event Dispatcher
   └─ Dispatch l'événement aux listeners

4. CommunityGamificationListener.handlePostCreatedEvent()
   ├─ Récoit l'événement
   ├─ Appelle gamificationService.addPoints(userId=1, points=10)
   │   └─ UserScore.totalPoints += 10
   └─ Appelle userBadgeService.awardBadgeToUser(userId=1, badgeId=1)
       └─ Crée UserBadge si pas déjà existant

5. Utilisateur reçoit les récompenses
   ├─ +10 points (visible dans GET /api/gamification/score/1)
   ├─ Badge "First Step" débloqué (visible dans /api/gamification/profile/1)
   └─ Peut être notifié via notification module

✅ GAMIFICATION COMPLÈTE!
```

## 📞 Checklist Intégration

Pour chaque service de Community, assurez-vous d'avoir:

- [ ] `@RequiredArgsConstructor` injection
- [ ] `ApplicationEventPublisher` injecté
- [ ] Événement publié après chaque action importante
- [ ] Logging de l'événement
- [ ] Gestion correcte des IDs (userId, postId, etc.)

---

**L'intégration est maintenant COMPLÈTE!** 🎉

Tous les événements Community sont maintenant liés à la gamification automatiquement!

