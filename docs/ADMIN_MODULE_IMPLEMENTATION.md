# Admin Module Implementation Plan

## 📋 Overview

This document describes the implementation plan for the Admin module in LearnLink. The module will provide administrative capabilities for managing users, moderating content, and viewing platform analytics.

---

## 🏗️ Module Structure

```
src/main/java/org/example/learnlink/modules/admin/
├── controller/
│   ├── AdminDashboardController.java
│   ├── AdminUserController.java
│   ├── AdminModerationController.java
│   ├── AdminModeratorController.java
│   └── AdminAnalyticsController.java
├── dto/
│   ├── request/
│   │   ├── UserStatusUpdateRequest.java
│   │   ├── ModerationActionRequest.java
│   │   ├── CreateModeratorRequest.java
│   │   └── UpdateModeratorPermissionsRequest.java
│   ├── response/
│   │   ├── DashboardStatsResponse.java
│   │   ├── AdminUserResponse.java
│   │   ├── AdminUserListResponse.java
│   │   ├── ModerationActionResponse.java
│   │   ├── ModeratorResponse.java
│   │   ├── ModeratorPermissionsResponse.java
│   │   ├── EngagementAnalyticsResponse.java
│   │   ├── UserGrowthResponse.java
│   │   └── ContentAnalyticsResponse.java
├── entity/
│   ├── ModerationLog.java
│   └── ModeratorPermission.java
├── mapper/
│   └── AdminMapper.java
├── repository/
│   ├── ModerationLogRepository.java
│   └── ModeratorPermissionRepository.java
└── service/
    ├── AdminDashboardService.java
    ├── AdminDashboardServiceImpl.java
    ├── AdminUserService.java
    ├── AdminUserServiceImpl.java
    ├── AdminModerationService.java
    ├── AdminModerationServiceImpl.java
    ├── AdminModeratorService.java
    ├── AdminModeratorServiceImpl.java
    ├── AdminAnalyticsService.java
    └── AdminAnalyticsServiceImpl.java
```

---

## 🔒 Security Configuration

All admin endpoints will be secured with role-based access control:

- **ADMIN role**: Full access to all admin endpoints
- **MODERATOR role**: Access only to moderation endpoints

```java
// Security annotations to be used on controllers
@PreAuthorize("hasRole('ADMIN')")           // For admin-only endpoints
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // For moderation endpoints
```

---

## 📊 Feature 1: Dashboard (Statistics Overview)

### Endpoint

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| GET | `/api/admin/dashboard/stats` | Get overview statistics | ADMIN |

### Response DTO: `DashboardStatsResponse`

```java
public class DashboardStatsResponse {
    // User Statistics
    private Long totalUsers;
    private Long activeUsersLast7Days;
    private Long activeUsersLast30Days;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    
    // Content Statistics
    private Long totalPosts;
    private Long totalQuestions;
    private Long totalAnswers;
    private Long totalComments;
    private Long postsThisWeek;
    
    // Task Statistics
    private Long totalTasks;
    private Long completedTasks;
    private Double taskCompletionRate;
    
    // Engagement Statistics
    private Long totalConnections;
    private Long totalStudyGroups;
    private Long activeStudySessions;
    
    // Gamification Statistics
    private Long totalPointsAwarded;
    private Long badgesEarned;
    
    // Top Subjects
    private List<SubjectStatDto> topSubjects;
    
    // Timestamp
    private LocalDateTime generatedAt;
}
```

### Database Queries Required

```sql
-- Total users
SELECT COUNT(*) FROM users;

-- Active users (last 7 days) - based on login or activity
SELECT COUNT(DISTINCT user_id) FROM user_scores 
WHERE updated_at >= NOW() - INTERVAL '7 days';

-- New users this week
SELECT COUNT(*) FROM users 
WHERE created_at >= DATE_TRUNC('week', NOW());

-- Total posts
SELECT COUNT(*) FROM community_posts;

-- Posts this week
SELECT COUNT(*) FROM community_posts 
WHERE created_at >= DATE_TRUNC('week', NOW());

-- Task completion rate
SELECT 
    COUNT(*) FILTER (WHERE completed = true)::DECIMAL / COUNT(*) * 100 
FROM tasks;

-- Top subjects (from user profiles)
SELECT subject, COUNT(*) as count 
FROM student_subjects 
GROUP BY subject 
ORDER BY count DESC 
LIMIT 5;
```

### Service Interface

```java
public interface AdminDashboardService {
    DashboardStatsResponse getDashboardStats();
    DashboardStatsResponse getCachedDashboardStats(); // With Redis caching
}
```

---

## 👥 Feature 2: User List (View All Users)

### Endpoints

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| GET | `/api/admin/users` | Get paginated user list | ADMIN |
| GET | `/api/admin/users/{id}` | Get user details | ADMIN |
| GET | `/api/admin/users/search` | Search users | ADMIN |

### Request Parameters for List

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 20 | Page size (max 100) |
| sort | String | createdAt,desc | Sort field and direction |
| role | String | null | Filter by role |
| active | Boolean | null | Filter by active status |
| search | String | null | Search by email or username |

### Response DTO: `AdminUserResponse`

```java
public class AdminUserResponse {
    private Long id;
    private String email;
    private String username;
    private UserRole role;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    
    // Profile info (if exists)
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String academicLevel;
    
    // Activity stats
    private Integer totalPoints;
    private Integer level;
    private Long tasksCreated;
    private Long tasksCompleted;
    private Long postsCreated;
    private Long connectionsCount;
    
    // Last activity
    private LocalDateTime lastActivityAt;
}
```

### Response DTO: `AdminUserListResponse`

```java
public class AdminUserListResponse {
    private List<AdminUserResponse> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
```

### Service Interface

```java
public interface AdminUserService {
    AdminUserListResponse getAllUsers(Pageable pageable, UserRole role, Boolean active, String search);
    AdminUserResponse getUserById(Long userId);
    AdminUserResponse updateUserStatus(Long userId, UserStatusUpdateRequest request);
    void deleteUser(Long userId);
}
```

### Repository Queries (to add to UserRepository)

```java
@Query("SELECT u FROM User u WHERE " +
       "(:role IS NULL OR u.role = :role) AND " +
       "(:active IS NULL OR u.active = :active) AND " +
       "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
       "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
Page<User> findAllWithFilters(
    @Param("role") UserRole role,
    @Param("active") Boolean active,
    @Param("search") String search,
    Pageable pageable
);

@Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
long countNewUsersSince(@Param("since") LocalDateTime since);

@Query("SELECT COUNT(DISTINCT us.userId) FROM UserScore us WHERE us.updatedAt >= :since")
long countActiveUsersSince(@Param("since") Instant since);
```

---

## 🔄 Feature 3: Activate/Deactivate Users

### Endpoints

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| PATCH | `/api/admin/users/{id}/status` | Update user status | ADMIN |
| POST | `/api/admin/users/{id}/activate` | Activate user | ADMIN |
| POST | `/api/admin/users/{id}/deactivate` | Deactivate user | ADMIN |

### Request DTO: `UserStatusUpdateRequest`

```java
public class UserStatusUpdateRequest {
    @NotNull
    private Boolean active;
    
    private String reason;  // Optional reason for status change
}
```

### Response DTO: `AdminUserResponse`

Returns the updated user details (same as Feature 2).

### Service Implementation Logic

```java
@Transactional
public AdminUserResponse updateUserStatus(Long userId, UserStatusUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    
    // Prevent self-deactivation
    if (currentUserId.equals(userId) && !request.getActive()) {
        throw new AdminOperationException("Cannot deactivate your own account");
    }
    
    // Prevent deactivating other admins (optional business rule)
    if (user.getRole() == UserRole.ADMIN && !request.getActive()) {
        throw new AdminOperationException("Cannot deactivate admin accounts");
    }
    
    user.setActive(request.getActive());
    user = userRepository.save(user);
    
    // Log the action
    logModerationAction(userId, request.getActive() ? "ACTIVATED" : "DEACTIVATED", request.getReason());
    
    // Send notification email (optional)
    if (!request.getActive()) {
        emailService.sendAccountDeactivatedEmail(user.getEmail(), request.getReason());
    }
    
    return adminMapper.toAdminUserResponse(user);
}
```

---

## 🛡️ Feature 4: Post Moderation

### Endpoints

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| GET | `/api/admin/moderation/posts` | Get all posts for moderation | ADMIN, MODERATOR |
| GET | `/api/admin/moderation/posts/reported` | Get reported posts | ADMIN, MODERATOR |
| GET | `/api/admin/moderation/posts/hidden` | Get soft-deleted posts | ADMIN |
| PATCH | `/api/admin/moderation/posts/{id}/hide` | Soft delete (hide) a post | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/posts/{id}/restore` | Restore a hidden post | ADMIN |
| DELETE | `/api/admin/moderation/posts/{id}` | Permanently delete a post | ADMIN |
| PATCH | `/api/admin/moderation/comments/{id}/hide` | Soft delete a comment | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/comments/{id}/restore` | Restore a hidden comment | ADMIN |
| DELETE | `/api/admin/moderation/comments/{id}` | Permanently delete a comment | ADMIN |
| PATCH | `/api/admin/moderation/questions/{id}/hide` | Soft delete a question | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/questions/{id}/restore` | Restore a hidden question | ADMIN |
| DELETE | `/api/admin/moderation/questions/{id}` | Permanently delete a question | ADMIN |
| PATCH | `/api/admin/moderation/answers/{id}/hide` | Soft delete an answer | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/answers/{id}/restore` | Restore a hidden answer | ADMIN |
| DELETE | `/api/admin/moderation/answers/{id}` | Permanently delete an answer | ADMIN |
| GET | `/api/admin/moderation/logs` | Get moderation action logs | ADMIN |

### Soft Delete Strategy

Instead of permanently deleting content, moderation uses **soft delete** (hiding content):

- **Moderators** can only **hide** (soft delete) content
- **Admins** can **restore** hidden content or **permanently delete** it
- All hidden content remains in the database with `hidden = true` and `hiddenAt` timestamp
- Hidden content is not visible to regular users but can be reviewed by admins

### Entity: `ModerationLog`

```java
@Entity
@Table(name = "moderation_logs")
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
    private Long targetUserId;  // User who owns the content
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot;  // Snapshot of deleted content for audit
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

### Enums

```java
public enum ModerationActionType {
    POST_HIDDEN,
    POST_RESTORED,
    POST_PERMANENTLY_DELETED,
    COMMENT_HIDDEN,
    COMMENT_RESTORED,
    COMMENT_PERMANENTLY_DELETED,
    QUESTION_HIDDEN,
    QUESTION_RESTORED,
    QUESTION_PERMANENTLY_DELETED,
    ANSWER_HIDDEN,
    ANSWER_RESTORED,
    ANSWER_PERMANENTLY_DELETED,
    USER_ACTIVATED,
    USER_DEACTIVATED,
    USER_BANNED,
    MODERATOR_CREATED,
    MODERATOR_PERMISSIONS_UPDATED,
    MODERATOR_REMOVED
}

public enum ModerationTargetType {
    POST,
    COMMENT,
    QUESTION,
    ANSWER,
    USER,
    MODERATOR
}
```

### Request DTO: `ModerationActionRequest`

```java
public class ModerationActionRequest {
    @NotBlank
    @Size(min = 5, max = 500)
    private String reason;  // Reason for moderation action
    
    private Boolean notifyUser;  // Whether to notify the content owner
}
```

### Response DTO: `ModerationActionResponse`

```java
public class ModerationActionResponse {
    private Long id;
    private ModerationActionType actionType;
    private ModerationTargetType targetType;
    private Long targetId;
    private String reason;
    private LocalDateTime actionAt;
    private String moderatorUsername;
    private Boolean userNotified;
}
```

### Service Interface

```java
public interface AdminModerationService {
    // Post moderation
    Page<PostResponse> getAllPostsForModeration(Pageable pageable);
    Page<PostResponse> getHiddenPosts(Pageable pageable);
    ModerationActionResponse hidePost(Long postId, Long moderatorId, ModerationActionRequest request);
    ModerationActionResponse restorePost(Long postId, Long adminId, String reason);
    ModerationActionResponse permanentlyDeletePost(Long postId, Long adminId, ModerationActionRequest request);
    
    // Comment moderation
    ModerationActionResponse hideComment(Long commentId, Long moderatorId, ModerationActionRequest request);
    ModerationActionResponse restoreComment(Long commentId, Long adminId, String reason);
    ModerationActionResponse permanentlyDeleteComment(Long commentId, Long adminId, ModerationActionRequest request);
    
    // Question moderation
    ModerationActionResponse hideQuestion(Long questionId, Long moderatorId, ModerationActionRequest request);
    ModerationActionResponse restoreQuestion(Long questionId, Long adminId, String reason);
    ModerationActionResponse permanentlyDeleteQuestion(Long questionId, Long adminId, ModerationActionRequest request);
    
    // Answer moderation
    ModerationActionResponse hideAnswer(Long answerId, Long moderatorId, ModerationActionRequest request);
    ModerationActionResponse restoreAnswer(Long answerId, Long adminId, String reason);
    ModerationActionResponse permanentlyDeleteAnswer(Long answerId, Long adminId, ModerationActionRequest request);
    
    // Logs
    Page<ModerationLog> getModerationLogs(Pageable pageable);
}
```

### Service Implementation Logic for Soft Delete (Hide) Post

```java
@Transactional
public ModerationActionResponse hidePost(Long postId, Long moderatorId, ModerationActionRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));
    
    if (post.getHidden()) {
        throw new ContentAlreadyHiddenException("Post is already hidden");
    }
    
    // Soft delete - set hidden flag
    post.setHidden(true);
    post.setHiddenAt(LocalDateTime.now());
    post.setHiddenBy(moderatorId);
    post.setHiddenReason(request.getReason());
    postRepository.save(post);
    
    // Log the moderation action
    ModerationLog log = ModerationLog.builder()
        .moderatorId(moderatorId)
        .actionType(ModerationActionType.POST_HIDDEN)
        .targetType(ModerationTargetType.POST)
        .targetId(postId)
        .targetUserId(post.getUserId())
        .reason(request.getReason())
        .createdAt(LocalDateTime.now())
        .build();
    moderationLogRepository.save(log);
    
    // Notify user if requested
    if (request.getNotifyUser()) {
        notificationService.sendModerationNotification(
            post.getUserId(),
            "Your post has been hidden by a moderator",
            request.getReason()
        );
    }
    
    return buildModerationActionResponse(log);
}

@Transactional
@PreAuthorize("hasRole('ADMIN')")
public ModerationActionResponse restorePost(Long postId, Long adminId, String reason) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));
    
    if (!post.getHidden()) {
        throw new ContentNotHiddenException("Post is not hidden");
    }
    
    // Restore the post
    post.setHidden(false);
    post.setHiddenAt(null);
    post.setHiddenBy(null);
    post.setHiddenReason(null);
    postRepository.save(post);
    
    // Log the restoration
    ModerationLog log = ModerationLog.builder()
        .moderatorId(adminId)
        .actionType(ModerationActionType.POST_RESTORED)
        .targetType(ModerationTargetType.POST)
        .targetId(postId)
        .targetUserId(post.getUserId())
        .reason(reason)
        .createdAt(LocalDateTime.now())
        .build();
    moderationLogRepository.save(log);
    
    // Notify user that their post was restored
    notificationService.sendModerationNotification(
        post.getUserId(),
        "Your post has been restored",
        reason
    );
    
    return buildModerationActionResponse(log);
}

@Transactional
@PreAuthorize("hasRole('ADMIN')")
public ModerationActionResponse permanentlyDeletePost(Long postId, Long adminId, ModerationActionRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));
    
    // Create snapshot before permanent deletion
    String contentSnapshot = String.format(
        "Title: %s\nContent: %s\nType: %s\nCategory: %s",
        post.getTitle(), post.getContent(), post.getType(), post.getCategory()
    );
    
    // Log the moderation action
    ModerationLog log = ModerationLog.builder()
        .moderatorId(adminId)
        .actionType(ModerationActionType.POST_PERMANENTLY_DELETED)
        .targetType(ModerationTargetType.POST)
        .targetId(postId)
        .targetUserId(post.getUserId())
        .reason(request.getReason())
        .contentSnapshot(contentSnapshot)  // Keep snapshot for audit
        .createdAt(LocalDateTime.now())
        .build();
    moderationLogRepository.save(log);
    
    // Permanently delete associated data
    commentRepository.deleteByPostId(postId);
    postLikeRepository.deleteByPostId(postId);
    postRepository.delete(post);
    
    return buildModerationActionResponse(log);
}
```

---

## 📈 Feature 5: Analytics (Engagement Charts)

### Endpoints

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| GET | `/api/admin/analytics/user-growth` | User registration trends | ADMIN |
| GET | `/api/admin/analytics/engagement` | Platform engagement metrics | ADMIN |
| GET | `/api/admin/analytics/content` | Content creation metrics | ADMIN |
| GET | `/api/admin/analytics/subjects` | Subject popularity | ADMIN |

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| period | String | WEEK | Time period: DAY, WEEK, MONTH, YEAR |
| startDate | LocalDate | null | Custom start date |
| endDate | LocalDate | null | Custom end date |

### Response DTO: `UserGrowthResponse`

```java
public class UserGrowthResponse {
    private List<TimeSeriesDataPoint> registrations;
    private List<TimeSeriesDataPoint> activeUsers;
    private Long totalNewUsers;
    private Double growthRate;  // Percentage compared to previous period
    private String period;
}

public class TimeSeriesDataPoint {
    private LocalDate date;
    private Long value;
    private String label;  // Formatted date label
}
```

### Response DTO: `EngagementAnalyticsResponse`

```java
public class EngagementAnalyticsResponse {
    // Task Engagement
    private List<TimeSeriesDataPoint> tasksCreated;
    private List<TimeSeriesDataPoint> tasksCompleted;
    private Double avgTaskCompletionRate;
    
    // Social Engagement
    private List<TimeSeriesDataPoint> newConnections;
    private List<TimeSeriesDataPoint> messagesExchanged;
    private Long totalActiveConversations;
    
    // Community Engagement
    private List<TimeSeriesDataPoint> postsCreated;
    private List<TimeSeriesDataPoint> questionsAsked;
    private List<TimeSeriesDataPoint> answersProvided;
    private Double avgResponseTime;  // Average time to first answer
    
    // Gamification
    private List<TimeSeriesDataPoint> pointsAwarded;
    private List<TimeSeriesDataPoint> badgesEarned;
    
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

### Response DTO: `ContentAnalyticsResponse`

```java
public class ContentAnalyticsResponse {
    private List<ContentTypeStats> byType;
    private List<ContentCategoryStats> byCategory;
    private List<TopContentItem> topPosts;
    private List<TopContentItem> topQuestions;
    
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
    
    private String period;
}

public class ContentTypeStats {
    private PostType type;
    private Long count;
    private Long views;
    private Long likes;
    private Double avgEngagementRate;
}

public class ContentCategoryStats {
    private PostCategory category;
    private Long count;
    private Long views;
    private Double growthRate;
}

public class TopContentItem {
    private Long id;
    private String title;
    private String authorUsername;
    private Long views;
    private Long likes;
    private Long comments;
}
```

### Service Interface

```java
public interface AdminAnalyticsService {
    UserGrowthResponse getUserGrowthAnalytics(String period, LocalDate startDate, LocalDate endDate);
    EngagementAnalyticsResponse getEngagementAnalytics(String period, LocalDate startDate, LocalDate endDate);
    ContentAnalyticsResponse getContentAnalytics(String period, LocalDate startDate, LocalDate endDate);
    List<SubjectStatDto> getSubjectPopularity(int limit);
}
```

### Repository Queries for Analytics

```java
// In UserRepository
@Query("SELECT FUNCTION('DATE', u.createdAt) as date, COUNT(u) as count " +
       "FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
       "GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY date")
List<Object[]> countUserRegistrationsByDay(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);

// In PostRepository
@Query("SELECT p.type, COUNT(p) as count, SUM(p.viewCount) as views, SUM(p.likesCount) as likes " +
       "FROM Post p WHERE p.createdAt BETWEEN :startDate AND :endDate " +
       "GROUP BY p.type")
List<Object[]> getPostStatsByType(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);

// In TaskRepository
@Query("SELECT FUNCTION('DATE', t.createdAt) as date, " +
       "COUNT(t) as created, " +
       "SUM(CASE WHEN t.completed = true THEN 1 ELSE 0 END) as completed " +
       "FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
       "GROUP BY FUNCTION('DATE', t.createdAt)")
List<Object[]> getTaskStatsByDay(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
```

---

## � Feature 6: Moderator Management

Admins can create moderators and manage their permissions.

### Endpoints

| Method | URL | Description | Role |
|--------|-----|-------------|------|
| GET | `/api/admin/moderators` | List all moderators | ADMIN |
| GET | `/api/admin/moderators/{id}` | Get moderator details | ADMIN |
| POST | `/api/admin/moderators` | Create a new moderator | ADMIN |
| PUT | `/api/admin/moderators/{id}/permissions` | Update moderator permissions | ADMIN |
| DELETE | `/api/admin/moderators/{id}` | Remove moderator role | ADMIN |
| GET | `/api/admin/moderators/{id}/activity` | Get moderator's activity log | ADMIN |

### Permission Types

```java
public enum ModeratorPermission {
    // Content Moderation
    HIDE_POSTS,           // Can hide posts
    HIDE_COMMENTS,        // Can hide comments
    HIDE_QUESTIONS,       // Can hide questions
    HIDE_ANSWERS,         // Can hide answers
    
    // User Management (limited)
    VIEW_USER_DETAILS,    // Can view user details
    WARN_USERS,           // Can send warnings to users
    
    // Reports
    VIEW_REPORTS,         // Can view reported content
    RESOLVE_REPORTS       // Can mark reports as resolved
}
```

### Entity: `ModeratorPermission`

```java
@Entity
@Table(name = "moderator_permissions")
public class ModeratorPermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "moderator_permission_list",
        joinColumns = @JoinColumn(name = "moderator_permission_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<ModeratorPermission> permissions = new HashSet<>();
    
    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;  // Admin who granted moderator role
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "notes")
    private String notes;  // Admin notes about this moderator
}
```

### Request DTO: `CreateModeratorRequest`

```java
public class CreateModeratorRequest {
    @NotNull
    private Long userId;  // Existing user to promote to moderator
    
    @NotEmpty
    private Set<ModeratorPermission> permissions;
    
    private String notes;  // Optional notes about why this user is being made a moderator
}
```

### Request DTO: `UpdateModeratorPermissionsRequest`

```java
public class UpdateModeratorPermissionsRequest {
    @NotEmpty
    private Set<ModeratorPermission> permissions;
    
    private String reason;  // Reason for permission change
}
```

### Response DTO: `ModeratorResponse`

```java
public class ModeratorResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String profilePictureUrl;
    
    private Set<ModeratorPermission> permissions;
    
    private Long assignedByUserId;
    private String assignedByUsername;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
    
    private String notes;
    
    // Activity stats
    private Long totalModerationActions;
    private Long actionsThisWeek;
    private LocalDateTime lastActionAt;
}
```

### Response DTO: `ModeratorPermissionsResponse`

```java
public class ModeratorPermissionsResponse {
    private Long userId;
    private Set<ModeratorPermission> currentPermissions;
    private Set<ModeratorPermission> availablePermissions;
    private LocalDateTime lastUpdated;
}
```

### Service Interface

```java
public interface AdminModeratorService {
    List<ModeratorResponse> getAllModerators();
    ModeratorResponse getModeratorById(Long userId);
    ModeratorResponse createModerator(Long adminId, CreateModeratorRequest request);
    ModeratorResponse updateModeratorPermissions(Long adminId, Long moderatorUserId, UpdateModeratorPermissionsRequest request);
    void removeModerator(Long adminId, Long moderatorUserId, String reason);
    Page<ModerationLog> getModeratorActivity(Long moderatorUserId, Pageable pageable);
    ModeratorPermissionsResponse getModeratorPermissions(Long userId);
}
```

### Service Implementation

```java
@Service
@RequiredArgsConstructor
public class AdminModeratorServiceImpl implements AdminModeratorService {

    private final UserRepository userRepository;
    private final ModeratorPermissionRepository moderatorPermissionRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final AdminMapper adminMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ModeratorResponse createModerator(Long adminId, CreateModeratorRequest request) {
        // Verify user exists
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        
        // Check if user is already a moderator
        if (moderatorPermissionRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyModeratorException(request.getUserId());
        }
        
        // Check if user is an admin (cannot demote admin to moderator)
        if (user.getRole() == UserRole.ADMIN) {
            throw new AdminOperationException("Cannot assign moderator role to an admin");
        }
        
        // Update user role
        user.setRole(UserRole.MODERATOR);
        userRepository.save(user);
        
        // Create moderator permissions
        ModeratorPermissionEntity permissions = ModeratorPermissionEntity.builder()
            .userId(request.getUserId())
            .permissions(request.getPermissions())
            .assignedBy(adminId)
            .assignedAt(LocalDateTime.now())
            .notes(request.getNotes())
            .build();
        moderatorPermissionRepository.save(permissions);
        
        // Log the action
        ModerationLog log = ModerationLog.builder()
            .moderatorId(adminId)
            .actionType(ModerationActionType.MODERATOR_CREATED)
            .targetType(ModerationTargetType.MODERATOR)
            .targetId(request.getUserId())
            .targetUserId(request.getUserId())
            .reason("Moderator created with permissions: " + request.getPermissions())
            .createdAt(LocalDateTime.now())
            .build();
        moderationLogRepository.save(log);
        
        // Notify the user
        eventPublisher.publishEvent(new ModeratorAssignedEvent(this, request.getUserId(), request.getPermissions()));
        
        return adminMapper.toModeratorResponse(user, permissions);
    }

    @Override
    @Transactional
    public ModeratorResponse updateModeratorPermissions(Long adminId, Long moderatorUserId, 
            UpdateModeratorPermissionsRequest request) {
        
        ModeratorPermissionEntity permissions = moderatorPermissionRepository.findByUserId(moderatorUserId)
            .orElseThrow(() -> new ModeratorNotFoundException(moderatorUserId));
        
        Set<ModeratorPermission> oldPermissions = new HashSet<>(permissions.getPermissions());
        
        // Update permissions
        permissions.setPermissions(request.getPermissions());
        permissions.setUpdatedAt(LocalDateTime.now());
        moderatorPermissionRepository.save(permissions);
        
        // Log the change
        ModerationLog log = ModerationLog.builder()
            .moderatorId(adminId)
            .actionType(ModerationActionType.MODERATOR_PERMISSIONS_UPDATED)
            .targetType(ModerationTargetType.MODERATOR)
            .targetId(moderatorUserId)
            .targetUserId(moderatorUserId)
            .reason(String.format("Permissions changed from %s to %s. Reason: %s", 
                oldPermissions, request.getPermissions(), request.getReason()))
            .createdAt(LocalDateTime.now())
            .build();
        moderationLogRepository.save(log);
        
        User user = userRepository.findById(moderatorUserId)
            .orElseThrow(() -> new UserNotFoundException(moderatorUserId));
        
        return adminMapper.toModeratorResponse(user, permissions);
    }

    @Override
    @Transactional
    public void removeModerator(Long adminId, Long moderatorUserId, String reason) {
        User user = userRepository.findById(moderatorUserId)
            .orElseThrow(() -> new UserNotFoundException(moderatorUserId));
        
        if (user.getRole() != UserRole.MODERATOR) {
            throw new AdminOperationException("User is not a moderator");
        }
        
        // Remove permissions
        moderatorPermissionRepository.deleteByUserId(moderatorUserId);
        
        // Revert user role to STUDENT
        user.setRole(UserRole.STUDENT);
        userRepository.save(user);
        
        // Log the action
        ModerationLog log = ModerationLog.builder()
            .moderatorId(adminId)
            .actionType(ModerationActionType.MODERATOR_REMOVED)
            .targetType(ModerationTargetType.MODERATOR)
            .targetId(moderatorUserId)
            .targetUserId(moderatorUserId)
            .reason(reason)
            .createdAt(LocalDateTime.now())
            .build();
        moderationLogRepository.save(log);
        
        // Notify the user
        eventPublisher.publishEvent(new ModeratorRemovedEvent(this, moderatorUserId, reason));
    }
    
    @Override
    public boolean hasPermission(Long userId, ModeratorPermission permission) {
        return moderatorPermissionRepository.findByUserId(userId)
            .map(p -> p.getPermissions().contains(permission))
            .orElse(false);
    }
}
```

### Permission Check in Moderation Service

```java
@Service
@RequiredArgsConstructor
public class AdminModerationServiceImpl implements AdminModerationService {

    private final AdminModeratorService moderatorService;
    
    @Override
    @Transactional
    public ModerationActionResponse hidePost(Long postId, Long moderatorId, ModerationActionRequest request) {
        // Check if moderator has permission to hide posts
        if (!moderatorService.hasPermission(moderatorId, ModeratorPermission.HIDE_POSTS)) {
            throw new InsufficientPermissionException("You don't have permission to hide posts");
        }
        
        // ... rest of the implementation
    }
}
```

---

## �🗄️ Database Migration

A new migration file will be created for the Admin module:

```sql
-- V1_4_0__Create_Admin_Module.sql

-- =====================================================
-- Soft Delete Columns for Content Tables
-- =====================================================

-- Add soft delete columns to posts
ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;
ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden_by BIGINT REFERENCES users(id);
ALTER TABLE community_posts ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

CREATE INDEX idx_posts_hidden ON community_posts(hidden);

-- Add soft delete columns to comments
ALTER TABLE community_comments ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE community_comments ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;
ALTER TABLE community_comments ADD COLUMN IF NOT EXISTS hidden_by BIGINT REFERENCES users(id);
ALTER TABLE community_comments ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

CREATE INDEX idx_comments_hidden ON community_comments(hidden);

-- Add soft delete columns to questions
ALTER TABLE community_questions ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE community_questions ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;
ALTER TABLE community_questions ADD COLUMN IF NOT EXISTS hidden_by BIGINT REFERENCES users(id);
ALTER TABLE community_questions ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

CREATE INDEX idx_questions_hidden ON community_questions(hidden);

-- Add soft delete columns to answers
ALTER TABLE community_answers ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE community_answers ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;
ALTER TABLE community_answers ADD COLUMN IF NOT EXISTS hidden_by BIGINT REFERENCES users(id);
ALTER TABLE community_answers ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

CREATE INDEX idx_answers_hidden ON community_answers(hidden);

-- =====================================================
-- Moderation Logs Table
-- =====================================================

CREATE TABLE moderation_logs (
    id BIGSERIAL PRIMARY KEY,
    moderator_id BIGINT NOT NULL REFERENCES users(id),
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    target_user_id BIGINT REFERENCES users(id),
    reason VARCHAR(500),
    content_snapshot TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_moderation_logs_moderator ON moderation_logs(moderator_id);
CREATE INDEX idx_moderation_logs_target_user ON moderation_logs(target_user_id);
CREATE INDEX idx_moderation_logs_created_at ON moderation_logs(created_at);
CREATE INDEX idx_moderation_logs_action_type ON moderation_logs(action_type);

-- =====================================================
-- Moderator Permissions Tables
-- =====================================================

CREATE TABLE moderator_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    assigned_by BIGINT NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    notes VARCHAR(500)
);

CREATE INDEX idx_moderator_permissions_user ON moderator_permissions(user_id);

CREATE TABLE moderator_permission_list (
    moderator_permission_id BIGINT NOT NULL REFERENCES moderator_permissions(id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (moderator_permission_id, permission)
);

CREATE INDEX idx_moderator_permission_list_permission ON moderator_permission_list(permission);
```

---

## 📚 API Endpoints Summary

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| **Dashboard** |
| GET | `/api/admin/dashboard/stats` | Get dashboard statistics | ADMIN |
| **User Management** |
| GET | `/api/admin/users` | List all users (paginated) | ADMIN |
| GET | `/api/admin/users/{id}` | Get user details | ADMIN |
| GET | `/api/admin/users/search` | Search users | ADMIN |
| PATCH | `/api/admin/users/{id}/status` | Update user status | ADMIN |
| POST | `/api/admin/users/{id}/activate` | Activate user | ADMIN |
| POST | `/api/admin/users/{id}/deactivate` | Deactivate user | ADMIN |
| **Content Moderation (Soft Delete)** |
| GET | `/api/admin/moderation/posts` | List posts for moderation | ADMIN, MODERATOR |
| GET | `/api/admin/moderation/posts/hidden` | List hidden posts | ADMIN |
| PATCH | `/api/admin/moderation/posts/{id}/hide` | Hide (soft delete) a post | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/posts/{id}/restore` | Restore a hidden post | ADMIN |
| DELETE | `/api/admin/moderation/posts/{id}` | Permanently delete a post | ADMIN |
| PATCH | `/api/admin/moderation/comments/{id}/hide` | Hide a comment | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/comments/{id}/restore` | Restore a hidden comment | ADMIN |
| DELETE | `/api/admin/moderation/comments/{id}` | Permanently delete a comment | ADMIN |
| PATCH | `/api/admin/moderation/questions/{id}/hide` | Hide a question | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/questions/{id}/restore` | Restore a hidden question | ADMIN |
| DELETE | `/api/admin/moderation/questions/{id}` | Permanently delete a question | ADMIN |
| PATCH | `/api/admin/moderation/answers/{id}/hide` | Hide an answer | ADMIN, MODERATOR |
| PATCH | `/api/admin/moderation/answers/{id}/restore` | Restore a hidden answer | ADMIN |
| DELETE | `/api/admin/moderation/answers/{id}` | Permanently delete an answer | ADMIN |
| GET | `/api/admin/moderation/logs` | Get moderation logs | ADMIN |
| **Moderator Management** |
| GET | `/api/admin/moderators` | List all moderators | ADMIN |
| GET | `/api/admin/moderators/{id}` | Get moderator details | ADMIN |
| POST | `/api/admin/moderators` | Create a new moderator | ADMIN |
| PUT | `/api/admin/moderators/{id}/permissions` | Update moderator permissions | ADMIN |
| DELETE | `/api/admin/moderators/{id}` | Remove moderator role | ADMIN |
| GET | `/api/admin/moderators/{id}/activity` | Get moderator activity log | ADMIN |
| **Analytics** |
| GET | `/api/admin/analytics/user-growth` | User growth trends | ADMIN |
| GET | `/api/admin/analytics/engagement` | Engagement metrics | ADMIN |
| GET | `/api/admin/analytics/content` | Content analytics | ADMIN |
| GET | `/api/admin/analytics/subjects` | Subject popularity | ADMIN |

---

## 🔄 Caching Strategy

Use Redis caching for expensive analytics queries:

```java
@Cacheable(value = "dashboard-stats", key = "'stats'", unless = "#result == null")
public DashboardStatsResponse getDashboardStats() { ... }

@Cacheable(value = "analytics-user-growth", key = "#period + '-' + #startDate + '-' + #endDate")
public UserGrowthResponse getUserGrowthAnalytics(...) { ... }
```

Cache TTL Configuration:
- Dashboard stats: 5 minutes
- Analytics data: 15 minutes
- User lists: No caching (real-time data)

---

## ✅ Implementation Checklist

### Phase 1: Setup
- [ ] Create module package structure
- [ ] Create database migration (V1_4_0__Create_Admin_Module.sql)
  - [ ] Add soft delete columns to content tables
  - [ ] Create moderation_logs table
  - [ ] Create moderator_permissions tables
- [ ] Create ModerationLog entity
- [ ] Create ModeratorPermissionEntity
- [ ] Create enums (ModerationActionType, ModerationTargetType, ModeratorPermission)

### Phase 2: DTOs & Mappers
- [ ] Create all request DTOs
  - [ ] UserStatusUpdateRequest
  - [ ] ModerationActionRequest
  - [ ] CreateModeratorRequest
  - [ ] UpdateModeratorPermissionsRequest
- [ ] Create all response DTOs
  - [ ] DashboardStatsResponse
  - [ ] AdminUserResponse, AdminUserListResponse
  - [ ] ModerationActionResponse
  - [ ] ModeratorResponse, ModeratorPermissionsResponse
  - [ ] Analytics DTOs
- [ ] Create AdminMapper

### Phase 3: Repositories
- [ ] Create ModerationLogRepository
- [ ] Create ModeratorPermissionRepository
- [ ] Add new queries to UserRepository
- [ ] Add soft delete queries to PostRepository, CommentRepository, etc.
- [ ] Add analytics queries to existing repositories

### Phase 4: Services
- [ ] Implement AdminDashboardService
- [ ] Implement AdminUserService
- [ ] Implement AdminModerationService (with soft delete)
- [ ] Implement AdminModeratorService
- [ ] Implement AdminAnalyticsService

### Phase 5: Controllers
- [ ] Implement AdminDashboardController
- [ ] Implement AdminUserController
- [ ] Implement AdminModerationController
- [ ] Implement AdminModeratorController
- [ ] Implement AdminAnalyticsController

### Phase 6: Update Existing Entities
- [ ] Add soft delete fields to Post entity
- [ ] Add soft delete fields to Comment entity
- [ ] Add soft delete fields to Question entity
- [ ] Add soft delete fields to Answer entity
- [ ] Update existing queries to exclude hidden content

### Phase 7: Testing
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] Security tests for role-based access
- [ ] Test moderator permission checks
- [ ] Test soft delete and restore functionality

---

## 🧪 Testing Considerations

1. **Security Tests**: Verify that non-admin users cannot access admin endpoints
2. **Role-based Access**: Verify moderator-only access to moderation endpoints
3. **Permission Checks**: Verify moderators can only perform actions they have permissions for
4. **Soft Delete**: Verify hidden content is not visible to regular users
5. **Restore Functionality**: Verify admins can restore hidden content
6. **Data Integrity**: Verify that moderation actions are properly logged
7. **Analytics Accuracy**: Verify calculations match expected values
8. **Pagination**: Test with large datasets

---

Please review this updated implementation plan. Once approved, I will proceed with the implementation.
