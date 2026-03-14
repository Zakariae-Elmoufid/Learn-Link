# Community Module API

This document lists all Community module endpoints with inputs and outputs.

## Base Paths

- Posts: `/api/community/posts`
- Questions: `/api/community/questions`
- Answers: `/api/community/answers`
- Comments: `/api/community/comments`

## Auth

Endpoints that create, update, delete, like, unlike, vote, or accept require authenticated user context (`@AuthenticationPrincipal CustomUserDetails`).

## Request DTOs

### CreatePostRequest
```json
{
  "title": "string (5-255)",
  "content": "string (10-5000)",
  "type": "SUMMARY | TUTORIAL | DISCUSSION",
  "category": "MATHEMATICS | SCIENCE | LANGUAGES | PROGRAMMING | HISTORY | LITERATURE | PHYSICS | CHEMISTRY | BIOLOGY | ECONOMICS | OTHER"
}
```

### UpdatePostRequest
```json
{
  "title": "string (5-255)",
  "content": "string (10-5000)",
  "category": "MATHEMATICS | SCIENCE | LANGUAGES | PROGRAMMING | HISTORY | LITERATURE | PHYSICS | CHEMISTRY | BIOLOGY | ECONOMICS | OTHER"
}
```

### AskQuestionRequest
```json
{
  "title": "string (5-255)",
  "content": "string (10-5000)"
}
```

### ProvideAnswerRequest
```json
{
  "content": "string (10-5000)"
}
```

### AddCommentRequest
```json
{
  "content": "string (1-1000)"
}
```

## Response DTOs

### PostResponse
```json
{
  "id": "number",
  "userId": "number",
  "title": "string",
  "content": "string",
  "type": "SUMMARY|TUTORIAL|DISCUSSION",
  "category": "PostCategory",
  "viewCount": "number",
  "likesCount": "number",
  "commentsCount": "number",
  "createdAt": "yyyy-MM-dd'T'HH:mm:ss",
  "updatedAt": "yyyy-MM-dd'T'HH:mm:ss",
  "likedByCurrentUser": "boolean"
}
```

### QuestionResponse
```json
{
  "id": "number",
  "userId": "number",
  "title": "string",
  "content": "string",
  "viewCount": "number",
  "isResolved": "boolean",
  "acceptedAnswerId": "number|null",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "answers": ["AnswerResponse"]
}
```

### AnswerResponse
```json
{
  "id": "number",
  "questionId": "number",
  "userId": "number",
  "content": "string",
  "voteCount": "number",
  "upvoteCount": "number",
  "downvoteCount": "number",
  "isAccepted": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "votedByCurrentUser": "boolean|null"
}
```

### CommentResponse
```json
{
  "id": "number",
  "postId": "number|null",
  "answerId": "number|null",
  "userId": "number",
  "content": "string",
  "likesCount": "number",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### PageResponse<T>
```json
{
  "content": ["T"],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "last": false
}
```

### VoteType
- `UPVOTE`
- `DOWNVOTE`

## Endpoints

## 1) Posts (12)

1. `POST /api/community/posts`
- Input: body `CreatePostRequest`
- Output: `201 Created` + `PostResponse`

2. `GET /api/community/posts/{postId}`
- Input: path `postId`
- Output: `200 OK` + `PostResponse`

3. `GET /api/community/posts?page={page}&size={size}`
- Input: query `page` (default `0`), `size` (default `20`)
- Output: `200 OK` + `PageResponse<PostResponse>`

4. `GET /api/community/posts/category/{category}?page={page}&size={size}`
- Input: path `category` (`PostCategory`), query `page`, `size`
- Output: `200 OK` + `Page<PostResponse>`

5. `GET /api/community/posts/popular?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<PostResponse>`

6. `GET /api/community/posts/trending?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<PostResponse>`

7. `GET /api/community/posts/user/{userId}?page={page}&size={size}`
- Input: path `userId`, query `page`, `size`
- Output: `200 OK` + `Page<PostResponse>`

8. `GET /api/community/posts/search?keyword={keyword}&category={category}&type={type}&page={page}&size={size}`
- Input: query `keyword` (optional), `category` (optional), `type` (optional), `page`, `size`
- Output: `200 OK` + `Page<PostResponse>`

9. `PUT /api/community/posts/{postId}`
- Input: path `postId`, body `UpdatePostRequest`
- Output: `200 OK` + `PostResponse`

10. `DELETE /api/community/posts/{postId}`
- Input: path `postId`
- Output: `204 No Content`

11. `POST /api/community/posts/{postId}/like`
- Input: path `postId`
- Output: `200 OK` (empty body)

12. `DELETE /api/community/posts/{postId}/like`
- Input: path `postId`
- Output: `200 OK` (empty body)

## 2) Questions (10)

1. `POST /api/community/questions`
- Input: body `AskQuestionRequest`
- Output: `201 Created` + `QuestionResponse`

2. `GET /api/community/questions/{questionId}`
- Input: path `questionId`
- Output: `200 OK` + `QuestionResponse`

3. `GET /api/community/questions?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

4. `GET /api/community/questions/unresolved?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

5. `GET /api/community/questions/resolved?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

6. `GET /api/community/questions/user/{userId}?page={page}&size={size}`
- Input: path `userId`, query `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

7. `GET /api/community/questions/search?keyword={keyword}&page={page}&size={size}`
- Input: query `keyword` (required), `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

8. `GET /api/community/questions/viewed?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<QuestionResponse>`

9. `PUT /api/community/questions/{questionId}`
- Input: path `questionId`, body `AskQuestionRequest`
- Output: `200 OK` + `QuestionResponse`

10. `DELETE /api/community/questions/{questionId}`
- Input: path `questionId`
- Output: `204 No Content`

## 3) Answers (10)

1. `POST /api/community/answers?questionId={questionId}`
- Input: query `questionId` (required), body `ProvideAnswerRequest`
- Output: `201 Created` + `AnswerResponse`

2. `GET /api/community/answers/{answerId}`
- Input: path `answerId`
- Output: `200 OK` + `AnswerResponse`

3. `GET /api/community/answers/question/{questionId}`
- Input: path `questionId`
- Output: `200 OK` + `List<AnswerResponse>`

4. `GET /api/community/answers/user/{userId}?page={page}&size={size}`
- Input: path `userId`, query `page`, `size`
- Output: `200 OK` + `Page<AnswerResponse>`

5. `GET /api/community/answers/top?page={page}&size={size}`
- Input: query `page`, `size`
- Output: `200 OK` + `Page<AnswerResponse>`

6. `PUT /api/community/answers/{answerId}`
- Input: path `answerId`, body `ProvideAnswerRequest`
- Output: `200 OK` + `AnswerResponse`

7. `DELETE /api/community/answers/{answerId}`
- Input: path `answerId`
- Output: `204 No Content`

8. `POST /api/community/answers/{answerId}/accept?questionId={questionId}`
- Input: path `answerId`, query `questionId` (required)
- Output: `200 OK` (empty body)

9. `POST /api/community/answers/{answerId}/vote?voteType={UPVOTE|DOWNVOTE}`
- Input: path `answerId`, query `voteType` (required)
- Output: `200 OK` (empty body)

10. `DELETE /api/community/answers/{answerId}/vote`
- Input: path `answerId`
- Output: `200 OK` (empty body)

## 4) Comments (10)

1. `POST /api/community/comments/post/{postId}`
- Input: path `postId`, body `AddCommentRequest`
- Output: `201 Created` + `CommentResponse`

2. `POST /api/community/comments/answer/{answerId}`
- Input: path `answerId`, body `AddCommentRequest`
- Output: `201 Created` + `CommentResponse`

3. `GET /api/community/comments/{commentId}`
- Input: path `commentId`
- Output: `200 OK` + `CommentResponse`

4. `GET /api/community/comments/post/{postId}`
- Input: path `postId`
- Output: `200 OK` + `List<CommentResponse>`

5. `GET /api/community/comments/answer/{answerId}`
- Input: path `answerId`
- Output: `200 OK` + `List<CommentResponse>`

6. `GET /api/community/comments/user/{userId}?page={page}&size={size}`
- Input: path `userId`, query `page`, `size`
- Output: `200 OK` + `Page<CommentResponse>`

7. `PUT /api/community/comments/{commentId}`
- Input: path `commentId`, body `AddCommentRequest`
- Output: `200 OK` + `CommentResponse`

8. `DELETE /api/community/comments/{commentId}`
- Input: path `commentId`
- Output: `204 No Content`

9. `POST /api/community/comments/{commentId}/like`
- Input: path `commentId`
- Output: `200 OK` (empty body)

10. `DELETE /api/community/comments/{commentId}/like`
- Input: path `commentId`
- Output: `200 OK` (empty body)
