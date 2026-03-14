# LearnLink Backend

## Planner Module API (Frontend Consumption)

Base path:
- `/api/planner/tasks`

Auth:
- Endpoints that use `@AuthenticationPrincipal` require a logged-in user token.

## Enums

`TaskPriority`
- `LOW`
- `MEDIUM`
- `HIGH`

`TaskStatus`
- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

## Request DTO

### TaskRequest
```json
{
  "title": "string (required, 3-255)",
  "description": "string (optional, max 2000)",
  "startTime": "yyyy-MM-dd'T'HH:mm:ss (required)",
  "endTime": "yyyy-MM-dd'T'HH:mm:ss (required)",
  "priority": "LOW | MEDIUM | HIGH (required)",
  "subject": "string (optional, max 100)",
  "tags": ["string"]
}
```

## Response DTO

### TaskResponse
```json
{
  "id": 1,
  "userId": 10,
  "title": "Study algebra",
  "description": "Chapter 3 exercises",
  "startTime": "2026-03-13T09:00:00",
  "endTime": "2026-03-13T10:30:00",
  "priority": "HIGH",
  "status": "PENDING",
  "completed": false,
  "completedAt": null,
  "subject": "Math",
  "tags": ["algebra", "exam"],
  "createdAt": "2026-03-13T08:00:00",
  "updatedAt": "2026-03-13T08:00:00",
  "isOverdue": false
}
```

## Endpoints

1. `POST /api/planner/tasks`
- Input: body `TaskRequest`
- Output: `201 Created` + `TaskResponse`

2. `GET /api/planner/tasks/{taskId}`
- Input: path `taskId`
- Output: `200 OK` + `TaskResponse`

3. `GET /api/planner/tasks`
- Input: none
- Output: `200 OK` + `List<TaskResponse>` (current user tasks)

4. `GET /api/planner/tasks/active`
- Input: none
- Output: `200 OK` + `List<TaskResponse>`

5. `GET /api/planner/tasks/today`
- Input: none
- Output: `200 OK` + `List<TaskResponse>`

6. `GET /api/planner/tasks/range?startTime={startTime}&endTime={endTime}`
- Input: query `startTime` and `endTime` (ISO datetime)
- Output: `200 OK` + `List<TaskResponse>`

7. `GET /api/planner/tasks/overdue`
- Input: none
- Output: `200 OK` + `List<TaskResponse>`

8. `PUT /api/planner/tasks/{taskId}`
- Input: path `taskId`, body `TaskRequest`
- Output: `200 OK` + `TaskResponse`

9. `POST /api/planner/tasks/{taskId}/complete`
- Input: path `taskId`
- Output: `200 OK` + `TaskResponse`

10. `DELETE /api/planner/tasks/{taskId}`
- Input: path `taskId`
- Output: `204 No Content`

## Common Error Responses

- `400 Bad Request`: Validation errors in request body/query params.
- `401 Unauthorized`: Missing or invalid auth token.
- `404 Not Found`: Task does not exist.

## Gamification Module API (Frontend Consumption)

Base paths:
- `/api/gamification`
- `/api/gamification/leaderboard`
- `/api/gamification/user-badges`
- `/api/gamification/badges`

Auth:
- `/api/gamification/score` uses current authenticated user.
- Other endpoints accept userId in path/query and may still require auth by security config.

### Gamification Endpoints

1. `GET /api/gamification/score`
- Input: none (uses current user token)
- Output: `200 OK` + `UserScoreResponse`

2. `GET /api/gamification/score/{userId}`
- Input: path `userId`
- Output: `200 OK` + `UserScoreResponse`

3. `GET /api/gamification/profile/{userId}`
- Input: path `userId`
- Output: `200 OK` + `UserPublicProfileResponse`

4. `POST /api/gamification/points?userId={userId}`
- Input: query `userId`, body `AddPointsRequest`
- Output: `201 Created` + `UserScoreResponse`

### Leaderboard Endpoints

5. `GET /api/gamification/leaderboard/global?limit={limit}`
- Input: query `limit` (default `100`)
- Output: `200 OK` + `List<LeaderboardEntryResponse>`

6. `GET /api/gamification/leaderboard/weekly?limit={limit}`
- Input: query `limit` (default `50`)
- Output: `200 OK` + `List<LeaderboardEntryResponse>`

7. `GET /api/gamification/leaderboard/rank/{userId}`
- Input: path `userId`
- Output: `200 OK` + `Integer`

8. `GET /api/gamification/leaderboard/rank-percentage/{userId}`
- Input: path `userId`
- Output: `200 OK` + `Long`

### User Badge Endpoints

9. `GET /api/gamification/user-badges/{userId}`
- Input: path `userId`
- Output: `200 OK` + `List<UserBadgeResponse>`

10. `GET /api/gamification/user-badges/{userId}/count`
- Input: path `userId`
- Output: `200 OK` + `Long`

11. `GET /api/gamification/user-badges/{userId}/has/{badgeId}`
- Input: path `userId`, path `badgeId`
- Output: `200 OK` + `Boolean`

12. `POST /api/gamification/user-badges/{userId}/award/{badgeId}`
- Input: path `userId`, path `badgeId`
- Output: `201 Created`

### Badge Catalog Endpoints

13. `GET /api/gamification/badges/{badgeId}`
- Input: path `badgeId`
- Output: `200 OK` + `BadgeResponse`

14. `GET /api/gamification/badges/code/{code}`
- Input: path `code`
- Output: `200 OK` + `BadgeResponse`

15. `GET /api/gamification/badges`
- Input: none
- Output: `200 OK` + `List<BadgeResponse>`

16. `GET /api/gamification/badges/active`
- Input: none
- Output: `200 OK` + `List<BadgeResponse>`

17. `POST /api/gamification/badges`
- Input: body `CreateBadgeRequest`
- Output: `201 Created` + `BadgeResponse`

18. `PUT /api/gamification/badges/{badgeId}`
- Input: path `badgeId`, body `CreateBadgeRequest`
- Output: `200 OK` + `BadgeResponse`

19. `DELETE /api/gamification/badges/{badgeId}`
- Input: path `badgeId`
- Output: `204 No Content`

### Request DTOs

#### AddPointsRequest
```json
{
  "actionType": "string (required)",
  "points": 10,
  "description": "string (optional, max 500)"
}
```

#### CreateBadgeRequest
```json
{
  "code": "string",
  "name": "string",
  "description": "string",
  "iconUrl": "string",
  "type": "string",
  "rarity": "string",
  "pointsRequired": 100
}
```

### Response DTOs

#### UserScoreResponse
```json
{
  "userId": 1,
  "totalPoints": 250,
  "level": 3,
  "currentLevelPoints": 50,
  "pointsForNextLevel": 100,
  "progressPercentage": 50.0
}
```

#### UserPublicProfileResponse
```json
{
  "userId": 1,
  "username": "john",
  "level": 3,
  "totalPoints": 250,
  "rank": 12,
  "badgeCount": 4,
  "badges": [
    {
      "badgeId": 2,
      "code": "FIRST_POST",
      "name": "First Post",
      "iconUrl": "https://...",
      "rarity": "COMMON",
      "earnedAt": "2026-03-14T08:00:00Z"
    }
  ]
}
```

#### LeaderboardEntryResponse
```json
{
  "userId": 1,
  "username": "john",
  "level": 3,
  "totalPoints": 250,
  "rank": 12,
  "badgeCount": 4
}
```

#### UserBadgeResponse
```json
{
  "badgeId": 2,
  "code": "FIRST_POST",
  "name": "First Post",
  "iconUrl": "https://...",
  "rarity": "COMMON",
  "earnedAt": "2026-03-14T08:00:00Z"
}
```

#### BadgeResponse
```json
{
  "id": 2,
  "code": "FIRST_POST",
  "name": "First Post",
  "description": "Awarded for creating first post",
  "iconUrl": "https://...",
  "type": "ENGAGEMENT",
  "rarity": "COMMON",
  "pointsRequired": 0,
  "active": true,
  "createdAt": "2026-03-14T08:00:00Z"
}
```
