# Notification API Documentation

This document providing all necessary information for the frontend to integrate with the LearnLink notification system.

## 📋 Base URL
`http://localhost:8081/api/notifications`

**Authentication**: All endpoints require a Bearer Token in the `Authorization` header.
`Authorization: Bearer <your-jwt-token>`

---

## 🔔 Notification Endpoints

### 1. Get Unread Notifications (Paginated)
Recommended endpoint for the notifications list.
- **Method**: `GET`
- **URL**: `/api/notifications/unread`
- **Params**:
    - `page` (optional, default: 0)
    - `size` (optional, default: 20)
- **Success Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 10,
      "type": "POST_LIKED",
      "typeName": "Post Liked",
      "title": "Someone liked your post!",
      "message": "A user liked your post.",
      "data": {
        "postId": 19,
        "likerId": 11,
        "link": "/community/posts/19"
      },
      "isRead": false,
      "createdAt": "2026-03-17T11:15:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get All Notifications
- **Method**: `GET`
- **URL**: `/api/notifications`

### 3. Get Unread Count (Badge Counter)
Use this for the notification bell icon badge.
- **Method**: `GET`
- **URL**: `/api/notifications/count`
- **Success Response**: `{"userId": 18, "unreadCount": 5}`

### 4. Mark One as Read
- **Method**: `PATCH`
- **URL**: `/api/notifications/{id}/read`

### 5. Mark All as Read
- **Method**: `PATCH`
- **URL**: `/api/notifications/read-all`

---

## 📱 Real-time Integration (WebSocket)

For real-time delivery without refreshing, connect to the WebSocket and subscribe to:

- **Library**: STOMP.js + SockJS
- **Connection URL**: `ws://localhost:8081/ws`
- **Subscription Path**: `/user/queue/notifications`
- **Payload**: Same as a single notification object in the REST response.

---

## 🛠️ Dev Testing
*Only available in dev/test profiles.*

### Manually Fire a Notification
- **Method**: `POST`
- **URL**: `/api/test/notifications/fire?type={TYPE}`
- **Types**: `POST_LIKED`, `POST_COMMENTED`, `QUESTION_ANSWERED`, `ANSWER_ACCEPTED`, `ANSWER_VOTED`, `CONNECTION_REQUEST`, `BADGE_EARNED`, `POINTS_EARNED`.

---

## 🧩 Notification Types Reference

| Type | Description | Link Data |
| :--- | :--- | :--- |
| `POST_LIKED` | When someone likes your post | `/community/posts/{id}` |
| `POST_COMMENTED` | When someone comments on your post/answer | `/community/posts/{id}` |
| `QUESTION_ANSWERED` | When someone answers your question | `/community/questions/{id}` |
| `ANSWER_ACCEPTED` | When your answer is marked as best | `/community/questions/{id}` |
| `ANSWER_VOTED` | When someone upvotes your answer | `/community/answers/{id}` |
| `CONNECTION_REQUEST` | When someone sends you a request | `/connections/requests` |
| `CONNECTION_ACCEPTED` | When your request is accepted | `/connections` |
| `BADGE_EARNED` | When you earn a new badge | `/profile/badges` |
| `POINTS_EARNED` | When you get points (significantly) | `/profile` |
| `NEW_MESSAGE` | When you receive a direct message | `/messages/{id}` |
