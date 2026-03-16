# LearnLink API Documentation

## Table of Contents
1. [Admin Dashboard API](#admin-dashboard-api)
2. [Student Dashboard API](#student-dashboard-api)
3. [Admin User Management API](#admin-user-management-api)
4. [Admin Content Moderation API](#admin-content-moderation-api)

---

## Admin Dashboard API

### Overview
Admin dashboard endpoints provide comprehensive platform statistics and analytics for administrators. These endpoints are protected with `@PreAuthorize("hasRole('ADMIN')")`.

### Base URL
```
/api/admin/dashboard
```

---

### 1. Get Dashboard Statistics
**Endpoint:** `GET /api/admin/dashboard/stats`

**Authorization:** Admin role required

**Description:** Returns comprehensive platform statistics including user data, content metrics, engagement stats, and gamification data.

**Request:**
```http
GET /api/admin/dashboard/stats HTTP/1.1
Host: localhost:8081
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "totalUsers": 150,
  "activeUsersLast7Days": 45,
  "activeUsersLast30Days": 85,
  "newUsersThisWeek": 12,
  "newUsersThisMonth": 35,
  "totalPosts": 234,
  "totalQuestions": 89,
  "totalAnswers": 156,
  "totalComments": 445,
  "postsThisWeek": 23,
  "totalTasks": 567,
  "completedTasks": 345,
  "taskCompletionRate": 60.85,
  "totalConnections": 234,
  "totalPointsAwarded": 5600,
  "badgesEarned": 178,
  "topSubjects": [
    {
      "subjectId": 1,
      "subjectName": "Mathematics",
      "postCount": 45,
      "questionCount": 23
    },
    {
      "subjectId": 2,
      "subjectName": "Physics",
      "postCount": 38,
      "questionCount": 19
    }
  ],
  "generatedAt": "2026-03-15T14:30:00"
}
```

**Response Codes:**
- `200 OK` - Statistics retrieved successfully
- `401 Unauthorized` - User not authenticated
- `403 Forbidden` - User is not an admin

---

## Student Dashboard API

### Overview
Student dashboard endpoints provide personalized user statistics, recent activity feed, and content creation metrics.

### Base URL
```
/api/dashboard
```

---

### 1. Get My Dashboard (Authenticated User)
**Endpoint:** `GET /api/dashboard`

**Authorization:** Authenticated user required

**Description:** Returns the authenticated user's personalized dashboard with statistics, recent activities, and content metrics.

**Request Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `activityLimit` | Integer | 10 | Number of recent activities to return (1-100) |

**Request:**
```http
GET /api/dashboard?activityLimit=10 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "statistics": {
    "totalPoints": 850,
    "level": 5,
    "pointsForNextLevel": 150,
    "currentLevelPoints": 50,
    "totalBadgesEarned": 8,
    "activeConnections": 12,
    "totalPostsCreated": 23,
    "totalQuestionsAsked": 15,
    "totalAnswersProvided": 34,
    "totalCommentsCreated": 67,
    "questionsResolved": 8,
    "answersAccepted": 12
  },
  "recentActivities": [
    {
      "type": "answer",
      "title": "Answered Question",
      "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
      "createdAt": "2026-03-15T10:30:00",
      "pointsEarned": 25,
      "badgeColor": "gold"
    },
    {
      "type": "post",
      "title": "How to Learn Calculus Effectively",
      "description": "A comprehensive guide to mastering calculus concepts...",
      "createdAt": "2026-03-15T09:15:00",
      "pointsEarned": 10,
      "badgeColor": "gold"
    },
    {
      "type": "comment",
      "title": "Added Comment",
      "description": "Great explanation! This helps a lot.",
      "createdAt": "2026-03-15T08:45:00",
      "pointsEarned": 1,
      "badgeColor": "bronze"
    },
    {
      "type": "connection",
      "title": "New Connection",
      "description": "Connected with another learner",
      "createdAt": "2026-03-15T07:20:00",
      "pointsEarned": 15,
      "badgeColor": "gold"
    },
    {
      "type": "question",
      "title": "asked a question",
      "description": "What is the best way to approach linear algebra problems?",
      "createdAt": "2026-03-15T06:50:00",
      "pointsEarned": 5,
      "badgeColor": "silver"
    }
  ],
  "contentCreationStats": {
    "totalPostsCreated": 23,
    "totalQuestionsAsked": 15,
    "totalAnswersProvided": 34,
    "totalCommentsCreated": 67,
    "totalPostLikes": 156,
    "totalAnswersAccepted": 12,
    "questionsResolved": 8,
    "averageLikesPerPost": 6,
    "averageCommentsPerQuestion": 4,
    "engagementScore": 1245
  }
}
```

**Response Codes:**
- `200 OK` - Dashboard retrieved successfully
- `400 Bad Request` - Invalid activity limit parameter
- `401 Unauthorized` - User not authenticated
- `500 Internal Server Error` - Error retrieving dashboard data

---

### 2. Get User Dashboard (Public View)
**Endpoint:** `GET /api/dashboard/{userId}`

**Authorization:** No authentication required

**Description:** Returns a public view of any user's dashboard. Shows limited information for privacy.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The ID of the user whose dashboard to retrieve |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `activityLimit` | Integer | 5 | Number of recent activities to return (1-50) |

**Request:**
```http
GET /api/dashboard/42?activityLimit=5 HTTP/1.1
Host: localhost:8081
```

**Response (200 OK):**
```json
{
  "statistics": {
    "totalPoints": 850,
    "level": 5,
    "pointsForNextLevel": 150,
    "currentLevelPoints": 50,
    "totalBadgesEarned": 8,
    "activeConnections": 12,
    "totalPostsCreated": 23,
    "totalQuestionsAsked": 15,
    "totalAnswersProvided": 34,
    "totalCommentsCreated": 67,
    "questionsResolved": 8,
    "answersAccepted": 12
  },
  "recentActivities": [
    {
      "type": "post",
      "title": "How to Learn Calculus Effectively",
      "description": "A comprehensive guide to mastering calculus concepts...",
      "createdAt": "2026-03-15T10:30:00",
      "pointsEarned": 10,
      "badgeColor": "gold"
    },
    {
      "type": "answer",
      "title": "Answered Question",
      "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
      "createdAt": "2026-03-15T09:15:00",
      "pointsEarned": 25,
      "badgeColor": "gold"
    }
  ],
  "contentCreationStats": {
    "totalPostsCreated": 23,
    "totalQuestionsAsked": 15,
    "totalAnswersProvided": 34,
    "totalCommentsCreated": 67,
    "totalPostLikes": 156,
    "totalAnswersAccepted": 12,
    "questionsResolved": 8,
    "averageLikesPerPost": 6,
    "averageCommentsPerQuestion": 4,
    "engagementScore": 1245
  }
}
```

**Response Codes:**
- `200 OK` - Dashboard retrieved successfully
- `400 Bad Request` - Invalid user ID or parameters
- `404 Not Found` - User not found
- `500 Internal Server Error` - Error retrieving dashboard data

---

## Complete Admin Endpoints Summary

All admin endpoints require authentication with Admin role (`@PreAuthorize("hasRole('ADMIN')")`), unless otherwise noted.

### Admin Endpoints by Category:
- **Dashboard**: 1 endpoint
- **User Management**: 5 endpoints  
- **Moderator Management**: 5 endpoints
- **Content Moderation**: 24 endpoints (Posts, Comments, Questions, Answers, Logs)

**Total Admin Endpoints: 35**

---

## Admin User Management API

### Overview
Admin user management endpoints allow admins to view and manage user accounts with filtering and pagination support.

### Base URL
```
/api/admin/users
```

---

### 1. Get All Users
**Endpoint:** `GET /api/admin/users`

**Authorization:** Admin or Moderator role required

**Description:** Retrieves a paginated list of all users with optional filtering by role, status, and search term.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `role` | String | null | Filter by user role (STUDENT, INSTRUCTOR, ADMIN) |
| `active` | Boolean | null | Filter by active status (true/false) |
| `search` | String | null | Search by email or username |
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Number of results per page |
| `sortBy` | String | createdAt | Field to sort by (createdAt, email, username) |
| `sortDirection` | String | desc | Sort direction (asc/desc) |

**Request:**
```http
GET /api/admin/users?role=STUDENT&page=0&size=20&sortBy=createdAt&sortDirection=desc HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "STUDENT",
      "active": true,
      "createdAt": "2026-01-15T10:30:00",
      "lastLogin": "2026-03-14T15:45:00",
      "totalPoints": 850,
      "level": 5
    },
    {
      "id": 2,
      "username": "jane_smith",
      "email": "jane@example.com",
      "firstName": "Jane",
      "lastName": "Smith",
      "role": "INSTRUCTOR",
      "active": true,
      "createdAt": "2026-02-10T08:20:00",
      "lastLogin": "2026-03-15T12:00:00",
      "totalPoints": 1250,
      "level": 7
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### 2. Get User by ID
**Endpoint:** `GET /api/admin/users/{userId}`

**Authorization:** Admin or Moderator role required

**Description:** Retrieves detailed information about a specific user.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The ID of the user to retrieve |

**Request:**
```http
GET /api/admin/users/42 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "id": 42,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT",
  "active": true,
  "createdAt": "2026-01-15T10:30:00",
  "lastLogin": "2026-03-14T15:45:00",
  "totalPoints": 850,
  "level": 5,
  "bio": "Passionate learner interested in mathematics and physics",
  "profileImageUrl": "https://example.com/images/profile42.jpg",
  "subjects": ["Mathematics", "Physics", "Chemistry"],
  "badgesEarned": 8,
  "postCount": 23,
  "questionCount": 15,
  "answerCount": 34
}
```

---

### 3. Activate User
**Endpoint:** `PATCH /api/admin/users/{userId}/activate`

**Authorization:** Admin role required

**Description:** Activates a deactivated user account.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The ID of the user to activate |

**Request:**
```http
PATCH /api/admin/users/42/activate HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "id": 42,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT",
  "active": true,
  "createdAt": "2026-01-15T10:30:00",
  "lastLogin": "2026-03-14T15:45:00",
  "totalPoints": 850,
  "level": 5
}
```

---

### 4. Deactivate User
**Endpoint:** `PATCH /api/admin/users/{userId}/deactivate`

**Authorization:** Admin role required

**Description:** Deactivates a user account, preventing them from logging in.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The ID of the user to deactivate |

**Request:**
```http
PATCH /api/admin/users/42/deactivate HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "id": 42,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT",
  "active": false,
  "createdAt": "2026-01-15T10:30:00",
  "lastLogin": "2026-03-14T15:45:00",
  "totalPoints": 850,
  "level": 5
}
```

---

### 5. Change User Role
**Endpoint:** `PATCH /api/admin/users/{userId}/role`

**Authorization:** Admin role required

**Description:** Changes the role of a user (STUDENT, INSTRUCTOR, ADMIN, MODERATOR).

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The ID of the user |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `role` | String | Yes | New role (STUDENT, INSTRUCTOR, ADMIN, MODERATOR) |

**Request:**
```http
PATCH /api/admin/users/42/role?role=INSTRUCTOR HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "id": 42,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "INSTRUCTOR",
  "active": true,
  "createdAt": "2026-01-15T10:30:00",
  "lastLogin": "2026-03-14T15:45:00",
  "totalPoints": 850,
  "level": 5
}
```

---

## Admin Moderator Management API

### Overview
Admin moderators management endpoints allow admins to create, view, and manage moderator accounts with permission assignments.

### Base URL
```
/api/admin/moderators
```

**Authorization:** Admin role required (not accessible to moderators)

---

### 1. Get All Moderators
**Endpoint:** `GET /api/admin/moderators`

**Description:** Retrieves a list of all moderators with their permissions.

**Request:**
```http
GET /api/admin/moderators HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 100,
    "username": "moderator1",
    "email": "mod1@example.com",
    "firstName": "John",
    "lastName": "Moderator",
    "createdAt": "2026-02-15T10:30:00",
    "permissions": [
      {
        "id": 1,
        "permission": "MODERATE_POSTS",
        "description": "Can moderate posts"
      },
      {
        "id": 2,
        "permission": "MODERATE_COMMENTS",
        "description": "Can moderate comments"
      }
    ]
  }
]
```

---

### 2. Get Moderator by User ID
**Endpoint:** `GET /api/admin/moderators/{userId}`

**Description:** Retrieves a specific moderator's details including their permissions.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The user ID of the moderator |

**Request:**
```http
GET /api/admin/moderators/100 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 100,
  "username": "moderator1",
  "email": "mod1@example.com",
  "firstName": "John",
  "lastName": "Moderator",
  "createdAt": "2026-02-15T10:30:00",
  "permissions": [
    {
      "id": 1,
      "permission": "MODERATE_POSTS",
      "description": "Can moderate posts"
    },
    {
      "id": 2,
      "permission": "MODERATE_COMMENTS",
      "description": "Can moderate comments"
    }
  ]
}
```

---

### 3. Get Moderator Permissions
**Endpoint:** `GET /api/admin/moderators/{userId}/permissions`

**Description:** Retrieves current and available permissions for a moderator.

**Request:**
```http
GET /api/admin/moderators/100/permissions HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "moderatorId": 100,
  "currentPermissions": [
    {
      "id": 1,
      "permission": "MODERATE_POSTS",
      "description": "Can moderate posts",
      "assigned": true
    },
    {
      "id": 2,
      "permission": "MODERATE_COMMENTS",
      "description": "Can moderate comments",
      "assigned": true
    },
    {
      "id": 3,
      "permission": "MODERATE_QUESTIONS",
      "description": "Can moderate questions",
      "assigned": false
    }
  ],
  "availablePermissions": [
    "MODERATE_POSTS",
    "MODERATE_COMMENTS",
    "MODERATE_QUESTIONS",
    "MODERATE_ANSWERS",
    "VIEW_MODERATION_LOGS",
    "MANAGE_USERS"
  ]
}
```

---

### 4. Create Moderator
**Endpoint:** `POST /api/admin/moderators`

**Description:** Assigns moderator role to a user with specified permissions.

**Request Body:**
```json
{
  "userId": 50,
  "permissions": ["MODERATE_POSTS", "MODERATE_COMMENTS", "MODERATE_QUESTIONS"]
}
```

**Request:**
```http
POST /api/admin/moderators HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "userId": 50,
  "permissions": ["MODERATE_POSTS", "MODERATE_COMMENTS", "MODERATE_QUESTIONS"]
}
```

**Response (200 OK):**
```json
{
  "id": 2,
  "userId": 50,
  "username": "new_moderator",
  "email": "newmod@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "createdAt": "2026-03-15T14:30:00",
  "permissions": [
    {
      "id": 4,
      "permission": "MODERATE_POSTS",
      "description": "Can moderate posts"
    },
    {
      "id": 5,
      "permission": "MODERATE_COMMENTS",
      "description": "Can moderate comments"
    },
    {
      "id": 6,
      "permission": "MODERATE_QUESTIONS",
      "description": "Can moderate questions"
    }
  ]
}
```

---

### 5. Update Moderator Permissions
**Endpoint:** `PUT /api/admin/moderators/{userId}/permissions`

**Description:** Updates the permissions assigned to a moderator.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The user ID of the moderator |

**Request Body:**
```json
{
  "permissions": ["MODERATE_POSTS", "MODERATE_COMMENTS", "MODERATE_ANSWERS", "VIEW_MODERATION_LOGS"]
}
```

**Request:**
```http
PUT /api/admin/moderators/100/permissions HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "permissions": ["MODERATE_POSTS", "MODERATE_COMMENTS", "MODERATE_ANSWERS", "VIEW_MODERATION_LOGS"]
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 100,
  "username": "moderator1",
  "email": "mod1@example.com",
  "firstName": "John",
  "lastName": "Moderator",
  "createdAt": "2026-02-15T10:30:00",
  "permissions": [
    {
      "id": 1,
      "permission": "MODERATE_POSTS",
      "description": "Can moderate posts"
    },
    {
      "id": 2,
      "permission": "MODERATE_COMMENTS",
      "description": "Can moderate comments"
    },
    {
      "id": 3,
      "permission": "MODERATE_ANSWERS",
      "description": "Can moderate answers"
    },
    {
      "id": 4,
      "permission": "VIEW_MODERATION_LOGS",
      "description": "Can view moderation logs"
    }
  ]
}
```

---

### 6. Remove Moderator
**Endpoint:** `DELETE /api/admin/moderators/{userId}`

**Description:** Removes moderator role from a user, reverting them to STUDENT role.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | The user ID of the moderator to remove |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `reason` | String | No | Reason for removing moderator role |

**Request:**
```http
DELETE /api/admin/moderators/100?reason=No+longer+needed HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (204 No Content):**
```
HTTP/1.1 204 No Content
```

---

## Admin Content Moderation API - Extended

### Overview
Comprehensive content moderation endpoints allowing admins and moderators to manage user-generated content. Features include hiding, restoring content, viewing moderation logs, and managing all content types.

### Base URL
```
/api/admin/moderation
```

---

### Posts Moderation Endpoints
**Endpoint:** `GET /api/admin/moderation/posts`

**Authorization:** Admin or Moderator role required

**Description:** Retrieves all posts including hidden ones for moderation review.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Number of results per page |
| `sort` | String | createdAt | Field to sort by |

**Request:**
```http
GET /api/admin/moderation/posts?page=0&size=20 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 42,
      "username": "john_doe",
      "title": "How to Learn Calculus",
      "content": "Here's a comprehensive guide to mastering calculus...",
      "category": "TUTORIAL",
      "type": "TUTORIAL",
      "viewCount": 234,
      "likes": 45,
      "comments": 12,
      "hidden": false,
      "createdAt": "2026-03-14T10:30:00",
      "updatedAt": "2026-03-15T08:45:00"
    }
  ],
  "totalElements": 234,
  "totalPages": 12,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### 2. Get Hidden Posts
**Endpoint:** `GET /api/admin/moderation/posts/hidden`

**Authorization:** Admin role required (not Moderator)

**Description:** Retrieves only soft-deleted (hidden) posts. Admin-only endpoint.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Number of results per page |
| `sort` | String | hiddenAt | Field to sort by |

**Request:**
```http
GET /api/admin/moderation/posts/hidden?page=0&size=20 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 5,
      "userId": 50,
      "username": "user_name",
      "title": "Inappropriate Post",
      "content": "...",
      "hidden": true,
      "hiddenBy": 1,
      "hiddenAt": "2026-03-15T12:00:00",
      "hiddenReason": "Violates community guidelines"
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": false
}
```

---

### 3. Hide a Post
**Endpoint:** `PATCH /api/admin/moderation/posts/{id}/hide`

**Authorization:** Admin or Moderator role required

**Description:** Soft-deletes a post making it invisible to regular users while preserving the data.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | Yes | The ID of the post to hide |

**Request Body:**
```json
{
  "reason": "Violates community guidelines - contains offensive language"
}
```

**Request:**
```http
PATCH /api/admin/moderation/posts/5/hide HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "reason": "Violates community guidelines - contains offensive language"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Post hidden successfully",
  "action": "HIDDEN",
  "targetId": 5,
  "targetType": "POST",
  "reason": "Violates community guidelines - contains offensive language",
  "performedBy": 1,
  "performedAt": "2026-03-15T14:30:00"
}
```

---

### 4. Restore a Hidden Post
**Endpoint:** `PATCH /api/admin/moderation/posts/{id}/restore`

**Authorization:** Admin role required

**Description:** Restores a soft-deleted post making it visible again. Admin only.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | Yes | The ID of the post to restore |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `reason` | String | null | Reason for restoration (optional) |

**Request:**
```http
PATCH /api/admin/moderation/posts/5/restore?reason=Appeal+approved HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Post restored successfully",
  "action": "RESTORED",
  "targetId": 5,
  "targetType": "POST",
  "reason": "Appeal approved",
  "performedBy": 1,
  "performedAt": "2026-03-15T15:00:00"
}
```

---

### 5. Permanently Delete a Post
**Endpoint:** `DELETE /api/admin/moderation/posts/{id}`

**Authorization:** Admin role required

**Description:** Permanently removes a post from the system. Cannot be undone.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | Yes | The ID of the post to permanently delete |

**Request Body:**
```json
{
  "reason": "Spam content - permanent removal"
}
```

**Request:**
```http
DELETE /api/admin/moderation/posts/5 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "reason": "Spam content - permanent removal"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Post permanently deleted",
  "action": "PERMANENTLY_DELETED",
  "targetId": 5,
  "targetType": "POST",
  "reason": "Spam content - permanent removal",
  "performedBy": 1,
  "performedAt": "2026-03-15T15:15:00"
}
```

---

### Questions Moderation Endpoints

All questions can be moderated using the same patterns as posts and comments:

- `GET /api/admin/moderation/questions` - Get all questions
- `GET /api/admin/moderation/questions/hidden` - Get hidden questions (Admin only)
- `PATCH /api/admin/moderation/questions/{id}/hide` - Hide a question
- `PATCH /api/admin/moderation/questions/{id}/restore` - Restore a question (Admin only)
- `DELETE /api/admin/moderation/questions/{id}` - Permanently delete a question (Admin only)

---

### Answers Moderation Endpoints

All answers can be moderated using the same patterns:

- `GET /api/admin/moderation/answers` - Get all answers
- `GET /api/admin/moderation/answers/hidden` - Get hidden answers (Admin only)
- `PATCH /api/admin/moderation/answers/{id}/hide` - Hide an answer
- `PATCH /api/admin/moderation/answers/{id}/restore` - Restore an answer (Admin only)
- `DELETE /api/admin/moderation/answers/{id}` - Permanently delete an answer (Admin only)

---

### 20. Get Moderation Logs
**Endpoint:** `GET /api/admin/moderation/logs`

**Authorization:** Admin role required

**Description:** Retrieves all moderation action logs showing what was moderated and by whom.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Number of results per page |
| `sort` | String | createdAt | Field to sort by |

**Request:**
```http
GET /api/admin/moderation/logs?page=0&size=20 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "action": "HIDDEN",
      "contentType": "POST",
      "contentId": 5,
      "moderatorId": 1,
      "moderatorName": "admin_user",
      "reason": "Violates community guidelines",
      "createdAt": "2026-03-15T14:30:00"
    },
    {
      "id": 2,
      "action": "RESTORED",
      "contentType": "COMMENT",
      "contentId": 12,
      "moderatorId": 1,
      "moderatorName": "admin_user",
      "reason": "Appeal approved",
      "createdAt": "2026-03-15T13:15:00"
    },
    {
      "id": 3,
      "action": "PERMANENTLY_DELETED",
      "contentType": "QUESTION",
      "contentId": 8,
      "moderatorId": 1,
      "moderatorName": "admin_user",
      "reason": "Spam content",
      "createdAt": "2026-03-15T12:00:00"
    }
  ],
  "totalElements": 156,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### 21. Get Moderation Logs by Moderator
**Endpoint:** `GET /api/admin/moderation/logs/moderator/{moderatorId}`

**Authorization:** Admin role required

**Description:** Retrieves moderation logs for a specific moderator to track their actions.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `moderatorId` | Long | Yes | The user ID of the moderator |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Number of results per page |

**Request:**
```http
GET /api/admin/moderation/logs/moderator/100?page=0&size=20 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 5,
      "action": "HIDDEN",
      "contentType": "POST",
      "contentId": 15,
      "moderatorId": 100,
      "moderatorName": "moderator1",
      "reason": "Inappropriate content",
      "createdAt": "2026-03-15T11:45:00"
    },
    {
      "id": 6,
      "action": "HIDDEN",
      "contentType": "COMMENT",
      "contentId": 34,
      "moderatorId": 100,
      "moderatorName": "moderator1",
      "reason": "Offensive language",
      "createdAt": "2026-03-15T10:30:00"
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## Complete Admin Endpoints Quick Reference

### Dashboard (1 endpoint)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/dashboard/stats` | Admin | Get dashboard statistics |

### User Management (5 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/users` | Admin/Mod | Get all users |
| GET | `/api/admin/users/{userId}` | Admin/Mod | Get user by ID |
| PATCH | `/api/admin/users/{userId}/activate` | Admin | Activate user |
| PATCH | `/api/admin/users/{userId}/deactivate` | Admin | Deactivate user |
| PATCH | `/api/admin/users/{userId}/role` | Admin | Change user role |

### Moderator Management (6 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderators` | Admin | Get all moderators |
| GET | `/api/admin/moderators/{userId}` | Admin | Get moderator by ID |
| GET | `/api/admin/moderators/{userId}/permissions` | Admin | Get moderator permissions |
| POST | `/api/admin/moderators` | Admin | Create moderator |
| PUT | `/api/admin/moderators/{userId}/permissions` | Admin | Update permissions |
| DELETE | `/api/admin/moderators/{userId}` | Admin | Remove moderator |

### Content Moderation (22 endpoints)
#### Posts (5 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderation/posts` | Admin/Mod | Get posts |
| GET | `/api/admin/moderation/posts/hidden` | Admin | Get hidden posts |
| PATCH | `/api/admin/moderation/posts/{id}/hide` | Admin/Mod | Hide post |
| PATCH | `/api/admin/moderation/posts/{id}/restore` | Admin | Restore post |
| DELETE | `/api/admin/moderation/posts/{id}` | Admin | Delete post |

#### Comments (5 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderation/comments` | Admin/Mod | Get comments |
| GET | `/api/admin/moderation/comments/hidden` | Admin | Get hidden comments |
| PATCH | `/api/admin/moderation/comments/{id}/hide` | Admin/Mod | Hide comment |
| PATCH | `/api/admin/moderation/comments/{id}/restore` | Admin | Restore comment |
| DELETE | `/api/admin/moderation/comments/{id}` | Admin | Delete comment |

#### Questions (5 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderation/questions` | Admin/Mod | Get questions |
| GET | `/api/admin/moderation/questions/hidden` | Admin | Get hidden questions |
| PATCH | `/api/admin/moderation/questions/{id}/hide` | Admin/Mod | Hide question |
| PATCH | `/api/admin/moderation/questions/{id}/restore` | Admin | Restore question |
| DELETE | `/api/admin/moderation/questions/{id}` | Admin | Delete question |

#### Answers (5 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderation/answers` | Admin/Mod | Get answers |
| GET | `/api/admin/moderation/answers/hidden` | Admin | Get hidden answers |
| PATCH | `/api/admin/moderation/answers/{id}/hide` | Admin/Mod | Hide answer |
| PATCH | `/api/admin/moderation/answers/{id}/restore` | Admin | Restore answer |
| DELETE | `/api/admin/moderation/answers/{id}` | Admin | Delete answer |

#### Moderation Logs (2 endpoints)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/moderation/logs` | Admin | Get all logs |
| GET | `/api/admin/moderation/logs/moderator/{id}` | Admin | Get moderator logs |

---

## ModerationActionRequest Body Format

Used for all hide/delete operations:

```json
{
  "reason": "Brief explanation of the moderation action"
}
```

**Example reasons:**
- "Violates community guidelines"
- "Contains offensive language"
- "Spam content"
- "Irrelevant to the discussion"
- "Copyright infringement"
- "Appeals approved - content restored"

---

## Moderation Action Response Format

All moderation actions return:

```json
{
  "success": true,
  "message": "Action completed successfully",
  "action": "HIDDEN|RESTORED|PERMANENTLY_DELETED",
  "targetId": 5,
  "targetType": "POST|COMMENT|QUESTION|ANSWER",
  "reason": "Violates community guidelines",
  "performedBy": 1,
  "performedAt": "2026-03-15T14:30:00"
}
```

---

## Admin Permissions

### Role-Based Access

| Role | Endpoints Accessible |
|------|----------------------|
| **Admin** | All 35 admin endpoints |
| **Moderator** | Get users, Get content for moderation, Hide/Restore content (limited) |
| **Student** | None (no admin access) |

### Permission Types

- `MODERATE_POSTS` - Can hide/restore posts
- `MODERATE_COMMENTS` - Can hide/restore comments
- `MODERATE_QUESTIONS` - Can hide/restore questions
- `MODERATE_ANSWERS` - Can hide/restore answers
- `VIEW_MODERATION_LOGS` - Can view moderation logs
- `MANAGE_USERS` - Can manage user accounts
- `MANAGE_ROLES` - Can assign/change user roles
- `PERMANENT_DELETE` - Can permanently delete content (Admin only)

---
**Endpoint:** `GET /api/admin/moderation/comments`

**Authorization:** Admin or Moderator role required

**Description:** Retrieves all comments including hidden ones for moderation review.

**Request:**
```http
GET /api/admin/moderation/comments?page=0&size=20 HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 42,
      "username": "john_doe",
      "postId": 5,
      "content": "Great explanation!",
      "hidden": false,
      "createdAt": "2026-03-14T10:30:00"
    }
  ],
  "totalElements": 445,
  "totalPages": 23,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true
}
```

---

### 7. Hide a Comment
**Endpoint:** `PATCH /api/admin/moderation/comments/{id}/hide`

**Authorization:** Admin or Moderator role required

**Description:** Soft-deletes a comment.

**Request:**
```http
PATCH /api/admin/moderation/comments/1/hide HTTP/1.1
Host: localhost:8081
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "reason": "Spam"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Comment hidden successfully",
  "action": "HIDDEN",
  "targetId": 1,
  "targetType": "COMMENT",
  "performedAt": "2026-03-15T14:30:00"
}
```

---

## Error Response Format

All endpoints return standardized error responses when something goes wrong:

**400 Bad Request:**
```json
{
  "error": "Invalid request",
  "message": "Activity limit must be between 1 and 100",
  "timestamp": "2026-03-15T14:30:00"
}
```

**401 Unauthorized:**
```json
{
  "error": "Unauthorized",
  "message": "User not authenticated",
  "timestamp": "2026-03-15T14:30:00"
}
```

**403 Forbidden:**
```json
{
  "error": "Forbidden",
  "message": "User is not an admin",
  "timestamp": "2026-03-15T14:30:00"
}
```

**404 Not Found:**
```json
{
  "error": "Not found",
  "message": "User with id 999 not found",
  "timestamp": "2026-03-15T14:30:00"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred while processing your request",
  "timestamp": "2026-03-15T14:30:00"
}
```

---

## Authentication

All endpoints (except public dashboard) require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

---

## Rate Limiting

- Public endpoints: 100 requests per hour per IP
- Authenticated endpoints: 1000 requests per hour per user
- Admin endpoints: 500 requests per hour per admin

---

## Pagination

Paginated endpoints follow this convention:
- **Default page size:** 20
- **Maximum page size:** 100
- **Default sort:** createdAt (descending)

Example paginated response:
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## Timestamps

All timestamps are in ISO 8601 format (UTC):
```
2026-03-15T14:30:00Z
```

---

## Status Codes Reference

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid parameters |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error - Server error |

---

## Integration Examples

### JavaScript/Fetch

**Get My Dashboard:**
```javascript
const response = await fetch('http://localhost:8081/api/dashboard?activityLimit=10', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${jwtToken}`,
    'Content-Type': 'application/json'
  }
});

const dashboard = await response.json();
console.log(dashboard.statistics);
console.log(dashboard.recentActivities);
```

### JavaScript/Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081',
  headers: {
    'Authorization': `Bearer ${jwtToken}`
  }
});

// Get admin dashboard stats
const statsResponse = await api.get('/api/admin/dashboard/stats');
console.log(statsResponse.data);

// Get student dashboard
const dashboardResponse = await api.get('/api/dashboard?activityLimit=10');
console.log(dashboardResponse.data);
```

### React Hook

```javascript
import { useState, useEffect } from 'react';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/dashboard?activityLimit=10', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
      }
    })
    .then(res => res.json())
    .then(data => {
      setDashboard(data);
      setLoading(false);
    });
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Level {dashboard.statistics.level}</h1>
      <p>Points: {dashboard.statistics.totalPoints}</p>
      <p>Recent Activities: {dashboard.recentActivities.length}</p>
    </div>
  );
}
```

---

## Support

For API issues or questions, please contact:
- **Email:** api-support@learnlink.com
- **Documentation:** https://docs.learnlink.com
- **Issue Tracker:** https://github.com/learnlink/backend/issues
