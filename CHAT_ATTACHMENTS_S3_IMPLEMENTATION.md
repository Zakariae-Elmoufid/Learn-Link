# Chat Attachments with AWS S3 (Private Bucket) - Implementation Guide

## Overview

This document explains how to implement file attachments for **Group Chat** and **1-to-1 Chat** using your existing **private** AWS S3 bucket `learnlink-bucket`.

---

## Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Client    │────▶│  Spring Boot API │────▶│     AWS S3      │
│  (Frontend) │     │   S3Client       │     │  learnlink-     │
│             │     │   (upload/delete)│     │    bucket       │
│             │◀────│                  │◀────│   (PRIVATE)     │
│             │     │   S3Presigner    │     │                 │
│             │     │   (signed URLs)  │     │                 │
└─────────────┘     └──────────────────┘     └─────────────────┘
```

### Why Both S3Client AND S3Presigner?

| Component       | Purpose                        | Operations                                                                    |
| --------------- | ------------------------------ | ----------------------------------------------------------------------------- |
| **S3Client**    | Direct S3 operations           | `upload()`, `delete()`, `download()`                                          |
| **S3Presigner** | Generate temporary signed URLs | `getPresignedUrl()` - allows clients to access private files for limited time |

**Private Bucket Flow:**

1. **Upload**: `S3Client.putObject()` uploads file to S3
2. **Access**: `S3Presigner.presignGetObject()` generates a temporary URL (e.g., valid 1 hour)
3. **Client**: Uses the signed URL to download directly from S3

---

## 1. Configuration (Already Done)

### 1.1 Your .env file

```env
AWS_S3_BUCKET_NAME=learnlink-bucket
AWS_ACCESS_KEY_ID=AKIA5Z6Q6K7X5G2Z3QG
AWS_SECRET_ACCESS_KEY=G8n5X9v1Z2Y3W4X5A6B7C8D9E0F1G2H3I4J5K6L7M8N9O0P1Q2R3S4T5U6V7W8X9Y0Z1
AWS_REGION=us-east-1
```

### 1.2 S3 Bucket Folder Structure

```
learnlink-bucket/
├── profile/              # Profile pictures (existing)
│   └── {userId}_{filename}
├── post/                 # Post attachments
│   └── {postId}_{filename}
├── attachment-group/     # Group chat attachments
│   └── {groupId}/
│       └── {messageId}_{filename}
└── attachment/           # 1-to-1 chat attachments
    └── {conversationId}/
        └── {messageId}_{filename}
```

---

## 2. Add S3Presigner Bean

**Update** `src/main/java/org/example/learnlink/config/AwsS3Config.java`:

```java
package org.example.learnlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Bean
    public S3Client s3Client(
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey
    ) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}
```

---

## 3. Enhanced S3StorageService

**Replace** `src/main/java/org/example/learnlink/modules/media/S3StorageService.java`:

```java
package org.example.learnlink.modules.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.media.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;

    // Folder constants matching your bucket structure
    public static final String FOLDER_PROFILE = "profile";
    public static final String FOLDER_POST = "post";
    public static final String FOLDER_ATTACHMENT_GROUP = "attachment-group";
    public static final String FOLDER_ATTACHMENT = "attachment";

    // Allowed file types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // ========== EXISTING METHOD (FIXED) ==========

    /**
     * Generic upload - used for profile pictures.
     */
    public String upload(String folder, MultipartFile file) throws IOException {
        String key = folder + "/" + sanitizeFilename(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        log.info("File uploaded: {}", key);

        // Return presigned URL for private bucket
        return generatePresignedUrl(key);
    }

    /**
     * Download file as bytes.
     */
    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(awsProperties.getS3().getBucket())
                        .key(key)
                        .build());
        return objectAsBytes.asByteArray();
    }

    // ========== GROUP CHAT ATTACHMENTS ==========

    /**
     * Upload attachment for group chat message.
     * Stored in: attachment-group/{groupId}/{messageId}_{filename}
     */
    public UploadResult uploadGroupChatAttachment(MultipartFile file, Long groupId, String messageId) {
        validateFile(file);
        String key = String.format("%s/%d/%s_%s",
                FOLDER_ATTACHMENT_GROUP, groupId, messageId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== DIRECT (1-TO-1) CHAT ATTACHMENTS ==========

    /**
     * Upload attachment for direct message.
     * Stored in: attachment/{conversationId}/{messageId}_{filename}
     */
    public UploadResult uploadDirectChatAttachment(MultipartFile file, Long conversationId, String messageId) {
        validateFile(file);
        String key = String.format("%s/%d/%s_%s",
                FOLDER_ATTACHMENT, conversationId, messageId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== PROFILE PICTURES ==========

    /**
     * Upload profile picture.
     * Stored in: profile/{userId}_{filename}
     */
    public UploadResult uploadProfilePicture(MultipartFile file, Long userId) {
        String key = String.format("%s/%d_%s",
                FOLDER_PROFILE, userId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== POST ATTACHMENTS ==========

    /**
     * Upload post attachment.
     * Stored in: post/{postId}_{filename}
     */
    public UploadResult uploadPostAttachment(MultipartFile file, Long postId) {
        validateFile(file);
        String key = String.format("%s/%d_%s",
                FOLDER_POST, postId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== CORE UPLOAD METHOD ==========

    private UploadResult uploadFileWithResult(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded: {}", key);

            // For private bucket, generate presigned URL
            String url = generatePresignedUrl(key);

            return UploadResult.builder()
                    .key(key)
                    .url(url)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload file: {}", key, e);
            throw new FileUploadException("Failed to upload file", e);
        }
    }

    // ========== PRESIGNED URL GENERATION (FOR PRIVATE BUCKET) ==========

    /**
     * Generate presigned URL valid for 1 hour.
     * This allows clients to access private files temporarily.
     */
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, Duration.ofHours(1));
    }

    /**
     * Generate presigned URL with custom duration.
     */
    public String generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Refresh presigned URL for an existing file.
     * Use this when returning messages (URLs may have expired).
     */
    public String refreshUrl(String key) {
        return generatePresignedUrl(key, Duration.ofHours(1));
    }

    // ========== DELETE OPERATIONS ==========

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("File deleted: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", key, e);
        }
    }

    public void deleteGroupAttachments(Long groupId) {
        deleteByPrefix(FOLDER_ATTACHMENT_GROUP + "/" + groupId + "/");
    }

    public void deleteConversationAttachments(Long conversationId) {
        deleteByPrefix(FOLDER_ATTACHMENT + "/" + conversationId + "/");
    }

    private void deleteByPrefix(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            for (S3Object s3Object : listResponse.contents()) {
                deleteFile(s3Object.key());
            }
            log.info("Deleted all files with prefix: {}", prefix);
        } catch (Exception e) {
            log.error("Failed to delete files with prefix: {}", prefix, e);
        }
    }

    // ========== VALIDATION ==========

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds 10MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("File type not allowed: " + contentType);
        }
    }

    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return UUID.randomUUID().toString();
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
```

---

## 4. Supporting Classes

### 4.1 UploadResult DTO

**Create** `src/main/java/org/example/learnlink/modules/media/dto/UploadResult.java`:

```java
package org.example.learnlink.modules.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResult {
    private String key;       // S3 key (e.g., "attachment-group/1/abc_file.pdf")
    private String url;       // Presigned URL for download
    private String fileName;  // Original filename
    private String contentType;
    private Long size;
}
```

### 4.2 FileUploadException

**Create** `src/main/java/org/example/learnlink/modules/media/exception/FileUploadException.java`:

```java
package org.example.learnlink.modules.media.exception;

public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## 5. REST Controller for Group Chat Attachments

**Create** `src/main/java/org/example/learnlink/modules/messaging/controller/GroupChatAttachmentController.java`:

```java
package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CurrentUser;
import org.example.learnlink.modules.auth.security.UserPrincipal;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/messages/attachments")
@RequiredArgsConstructor
@Tag(name = "Group Chat Attachments")
public class GroupChatAttachmentController {

    private final S3StorageService s3StorageService;
    private final IGroupMessageService groupMessageService;

    /**
     * Upload file and send message with attachment.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment and send group message")
    public ResponseEntity<GroupMessageResponse> uploadAndSendMessage(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false) String content,
            @CurrentUser UserPrincipal currentUser
    ) {
        String tempMessageId = UUID.randomUUID().toString();

        // Upload to S3
        UploadResult uploadResult = s3StorageService.uploadGroupChatAttachment(
                file, groupId, tempMessageId);

        // Create message with attachment
        GroupMessageRequest request = GroupMessageRequest.builder()
                .content(content != null ? content : file.getOriginalFilename())
                .type(file.getContentType().startsWith("image/") ? "IMAGE" : "FILE")
                .attachmentUrl(uploadResult.getKey())  // Store KEY, not URL
                .attachmentName(uploadResult.getFileName())
                .build();

        GroupMessageResponse response = groupMessageService.sendMessage(
                groupId, request, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Upload file only (without sending message).
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment only")
    public ResponseEntity<UploadResult> uploadOnly(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal currentUser
    ) {
        String tempId = UUID.randomUUID().toString();
        UploadResult result = s3StorageService.uploadGroupChatAttachment(file, groupId, tempId);
        return ResponseEntity.ok(result);
    }
}
```

---

## 6. REST Controller for Direct Chat Attachments

**Create** `src/main/java/org/example/learnlink/modules/messaging/controller/DirectChatAttachmentController.java`:

```java
package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CurrentUser;
import org.example.learnlink.modules.auth.security.UserPrincipal;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages/attachments")
@RequiredArgsConstructor
@Tag(name = "Direct Chat Attachments")
public class DirectChatAttachmentController {

    private final S3StorageService s3StorageService;

    /**
     * Upload attachment for direct message.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment for direct message")
    public ResponseEntity<UploadResult> uploadAttachment(
            @PathVariable Long conversationId,
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal currentUser
    ) {
        String tempMessageId = UUID.randomUUID().toString();
        UploadResult result = s3StorageService.uploadDirectChatAttachment(
                file, conversationId, tempMessageId);
        return ResponseEntity.ok(result);
    }
}
```

---

## 7. Important: Handling URL Expiration

Since presigned URLs expire (1 hour default), you have two options:

### Option A: Store S3 KEY in database, generate URL when fetching

**In your entity:**

```java
// Store the S3 key, not the full URL
@Column(name = "attachment_key")
private String attachmentKey;  // e.g., "attachment-group/1/uuid_file.pdf"
```

**In your service when returning messages:**

```java
public GroupMessageResponse toResponse(GroupMessage message) {
    String attachmentUrl = null;
    if (message.getAttachmentKey() != null) {
        // Generate fresh presigned URL
        attachmentUrl = s3StorageService.generatePresignedUrl(message.getAttachmentKey());
    }

    return GroupMessageResponse.builder()
            .id(message.getId())
            .attachmentUrl(attachmentUrl)  // Fresh URL
            .attachmentName(message.getAttachmentName())
            // ... other fields
            .build();
}
```

### Option B: Longer expiration time + Client refresh

Set presigned URL to expire in 7 days:

```java
public String generatePresignedUrl(String key) {
    return generatePresignedUrl(key, Duration.ofDays(7));
}
```

Add endpoint to refresh URL:

```java
@GetMapping("/refresh-url")
public ResponseEntity<String> refreshUrl(@RequestParam String key) {
    return ResponseEntity.ok(s3StorageService.generatePresignedUrl(key));
}
```

**Recommended: Option A** - Store key, generate fresh URL on fetch.

---

## 8. Frontend Integration

### Upload and Send Message

```javascript
async function sendMessageWithAttachment(groupId, file, content = "") {
  const formData = new FormData();
  formData.append("file", file);
  if (content) formData.append("content", content);

  const response = await fetch(
    `/api/v1/groups/${groupId}/messages/attachments`,
    {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    },
  );
  return response.json();
}
```

### Display Attachment

```jsx
function Attachment({ message }) {
  if (!message.attachmentUrl) return null;

  if (message.type === "IMAGE") {
    return <img src={message.attachmentUrl} alt={message.attachmentName} />;
  }

  return (
    <a href={message.attachmentUrl} target="_blank" download>
      📎 {message.attachmentName}
    </a>
  );
}
```

---

## 9. Summary

| Folder              | Purpose           | Method                         |
| ------------------- | ----------------- | ------------------------------ |
| `/profile`          | Profile pictures  | `uploadProfilePicture()`       |
| `/post`             | Post attachments  | `uploadPostAttachment()`       |
| `/attachment-group` | Group chat files  | `uploadGroupChatAttachment()`  |
| `/attachment`       | Direct chat files | `uploadDirectChatAttachment()` |

| Endpoint                                               | Method        | Description                    |
| ------------------------------------------------------ | ------------- | ------------------------------ |
| `POST /api/v1/groups/{id}/messages/attachments`        | Upload + Send | Upload file and create message |
| `POST /api/v1/groups/{id}/messages/attachments/upload` | Upload Only   | Upload file, get URL for later |
| `POST /api/v1/conversations/{id}/messages/attachments` | Upload        | Direct message attachment      |

**Key Points for Private Bucket:**

- Use `S3Client` for upload/delete operations
- Use `S3Presigner` to generate temporary download URLs
- Store S3 **key** in database, not the full URL
- Generate fresh presigned URL when returning messages to client
