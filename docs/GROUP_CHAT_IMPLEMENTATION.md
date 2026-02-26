# Group Chat Implementation Guide - LearnLink

## 📋 Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Database Schema](#3-database-schema)
4. [Entity Layer](#4-entity-layer)
5. [Repository Layer](#5-repository-layer)
6. [Service Layer](#6-service-layer)
7. [Controller Layer](#7-controller-layer)
8. [WebSocket Integration](#8-websocket-integration)
9. [DTOs](#9-dtos)
10. [Events](#10-events)
11. [Migration Script](#11-migration-script)
12. [Testing](#12-testing)
13. [Usage Examples](#13-usage-examples)

---

## 1. Overview

### 1.1 Feature Description

The Group Chat feature extends the existing 1-to-1 messaging system to support real-time communication within study groups. This feature integrates with the existing `StudyGroup` entity from the matching module.

### 1.2 Requirements (from Cahier des Charges)

| ID      | Fonctionnalité   | Description                     | Priorité     |
|---------|------------------|---------------------------------|--------------|
| F-MS-08 | Session d'étude  | Créer session de groupe         | ⭐⭐ Moyenne |
| F-MS-09 | Rejoindre session| Participer à une session        | ⭐⭐ Moyenne |
| F-MS-10 | Chat de groupe   | Discuter dans la session        | ⭐⭐ Moyenne |

### 1.3 Key Features

- ✅ Send messages to a study group
- ✅ Receive real-time group messages via WebSocket
- ✅ View group message history (paginated)
- ✅ Typing indicators for group members
- ✅ Read receipts at group level
- ✅ File attachments support
- ✅ Integration with existing StudyGroup memberships
- ✅ Gamification points for group participation

---

## 2. Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (React)                          │
├─────────────────────────────────────────────────────────────────┤
│  REST API                              │  WebSocket (STOMP)      │
│  - GET /api/groups/{id}/messages       │  - /app/group.send      │
│  - POST /api/groups/{id}/messages      │  - /app/group.typing    │
│  - PUT /api/groups/{id}/messages/read  │  - /topic/group/{id}    │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    Controller Layer                             │
│  GroupChatController (REST) + GroupChatWebSocketController      │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                     Service Layer                               │
│  GroupMessageService                                            │
│  - sendGroupMessage()                                           │
│  - getGroupMessages()                                           │
│  - markMessagesAsRead()                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    Repository Layer                             │
│  GroupMessageRepository + GroupMessageReadStatusRepository      │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    Database (PostgreSQL)                        │
│  group_messages + group_message_read_status                     │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 WebSocket Message Flow

```
┌──────────┐         ┌──────────┐         ┌──────────┐
│  User A  │         │  Server  │         │  User B  │
│ (Member) │         │          │         │ (Member) │
└────┬─────┘         └────┬─────┘         └────┬─────┘
     │                    │                    │
     │  SUBSCRIBE         │                    │
     │  /topic/group/123  │                    │
     │───────────────────>│                    │
     │                    │                    │
     │                    │  SUBSCRIBE         │
     │                    │  /topic/group/123  │
     │                    │<───────────────────│
     │                    │                    │
     │  SEND              │                    │
     │  /app/group.send   │                    │
     │  {groupId: 123,    │                    │
     │   content: "Hi!"}  │                    │
     │───────────────────>│                    │
     │                    │                    │
     │                    │  BROADCAST         │
     │                    │  /topic/group/123  │
     │<───────────────────│───────────────────>│
     │  MESSAGE           │  MESSAGE           │
     │                    │                    │
```

### 2.3 Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     messaging module                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  1-to-1 Chat    │    │       Group Chat (NEW)          │ │
│  │  ─────────────  │    │  ─────────────────────────────  │ │
│  │  Message.java   │    │  GroupMessage.java              │ │
│  │  MessageRepo    │    │  GroupMessageRepository         │ │
│  │  MessageService │    │  GroupMessageService            │ │
│  │  ChatWSCtrl     │    │  GroupChatWebSocketController   │ │
│  └────────┬────────┘    └─────────────┬───────────────────┘ │
│           │                           │                     │
│           └───────────┬───────────────┘                     │
│                       │                                     │
│               ┌───────▼───────┐                             │
│               │  Shared DTOs  │                             │
│               │  & Mappers    │                             │
│               └───────────────┘                             │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                     matching module                          │
│  ─────────────────────────────────────────────────────────  │
│   StudyGroup.java  │  GroupMembership.java  │  IStudyGroup  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Database Schema

### 3.1 Entity Relationship Diagram

```
┌─────────────────────┐       ┌─────────────────────────┐
│    study_groups     │       │      group_messages     │
├─────────────────────┤       ├─────────────────────────┤
│ id (PK)             │◄──────│ group_id (FK)           │
│ name                │       │ id (PK)                 │
│ description         │       │ sender_id (FK→users)    │
│ owner_id            │       │ content                 │
│ max_members         │       │ message_type            │
│ status              │       │ attachment_url          │
│ is_public           │       │ attachment_name         │
│ created_at          │       │ created_at              │
└─────────────────────┘       │ updated_at              │
         │                    └───────────┬─────────────┘
         │                                │
         │                                │
         ▼                                ▼
┌─────────────────────┐       ┌─────────────────────────┐
│  group_memberships  │       │ group_message_read_status│
├─────────────────────┤       ├─────────────────────────┤
│ id (PK)             │       │ id (PK)                 │
│ group_id (FK)       │       │ message_id (FK)         │
│ user_id (FK)        │       │ user_id (FK→users)      │
│ role                │       │ read_at                 │
│ status              │       └─────────────────────────┘
│ joined_at           │
└─────────────────────┘
```

### 3.2 Table Definitions

```sql
-- Group Messages Table
CREATE TABLE group_messages (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    content VARCHAR(4000) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Read Status Tracking (who has read which message)
CREATE TABLE group_message_read_status (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES group_messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(message_id, user_id)
);
```

---

## 4. Entity Layer

### 4.1 GroupMessage Entity

**File:** `src/main/java/org/example/learnlink/modules/messaging/entity/GroupMessage.java`

```java
package org.example.learnlink.modules.messaging.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnlink.modules.matching.entity.StudyGroup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a message in a study group chat.
 * Linked to the StudyGroup entity from the matching module.
 */
@Entity
@Table(name = "group_messages", indexes = {
    @Index(name = "idx_group_message_group", columnList = "group_id"),
    @Index(name = "idx_group_message_sender", columnList = "sender_id"),
    @Index(name = "idx_group_message_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The study group this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    /**
     * ID of the user who sent the message
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * Message content (up to 4000 characters)
     */
    @Column(nullable = false, length = 4000)
    private String content;

    /**
     * Type of message: TEXT, IMAGE, FILE, AUDIO, VIDEO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * URL to attached file (if any)
     */
    @Column(name = "attachment_url")
    private String attachmentUrl;

    /**
     * Original filename of attachment
     */
    @Column(name = "attachment_name")
    private String attachmentName;

    /**
     * Read statuses for this message
     */
    @OneToMany(mappedBy = "groupMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMessageReadStatus> readStatuses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if a specific user has read this message
     */
    public boolean isReadByUser(Long userId) {
        return readStatuses.stream()
                .anyMatch(rs -> rs.getUserId().equals(userId));
    }

    /**
     * Get the count of users who have read this message
     */
    public int getReadCount() {
        return readStatuses.size();
    }
}
```

### 4.2 GroupMessageReadStatus Entity

**File:** `src/main/java/org/example/learnlink/modules/messaging/entity/GroupMessageReadStatus.java`

```java
package org.example.learnlink.modules.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking which users have read which group messages.
 * Enables read receipts feature for group chats.
 */
@Entity
@Table(name = "group_message_read_status",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_message_user_read",
                columnNames = {"message_id", "user_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The message that was read
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private GroupMessage groupMessage;

    /**
     * ID of the user who read the message
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Timestamp when the message was read
     */
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
```

---

## 5. Repository Layer

### 5.1 GroupMessageRepository

**File:** `src/main/java/org/example/learnlink/modules/messaging/repository/GroupMessageRepository.java`

```java
package org.example.learnlink.modules.messaging.repository;

import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for GroupMessage entity operations.
 */
@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    /**
     * Find all messages for a group, ordered by creation time descending (newest first).
     * Paginated for performance.
     */
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.studyGroup.id = :groupId ORDER BY gm.createdAt DESC")
    Page<GroupMessage> findByGroupIdOrderByCreatedAtDesc(
            @Param("groupId") Long groupId,
            Pageable pageable
    );

    /**
     * Find messages created after a specific timestamp (for real-time sync).
     */
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.studyGroup.id = :groupId AND gm.createdAt > :since ORDER BY gm.createdAt ASC")
    List<GroupMessage> findByGroupIdAndCreatedAtAfter(
            @Param("groupId") Long groupId,
            @Param("since") LocalDateTime since
    );

    /**
     * Count unread messages for a user in a group.
     */
    @Query("""
        SELECT COUNT(gm) FROM GroupMessage gm 
        WHERE gm.studyGroup.id = :groupId 
        AND gm.senderId != :userId 
        AND NOT EXISTS (
            SELECT rs FROM GroupMessageReadStatus rs 
            WHERE rs.groupMessage = gm AND rs.userId = :userId
        )
    """)
    Long countUnreadMessages(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId
    );

    /**
     * Find the last message in a group.
     */
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.studyGroup.id = :groupId ORDER BY gm.createdAt DESC LIMIT 1")
    GroupMessage findLastMessageByGroupId(@Param("groupId") Long groupId);

    /**
     * Delete all messages in a group (for group deletion).
     */
    void deleteByStudyGroupId(Long groupId);
}
```

### 5.2 GroupMessageReadStatusRepository

**File:** `src/main/java/org/example/learnlink/modules/messaging/repository/GroupMessageReadStatusRepository.java`

```java
package org.example.learnlink.modules.messaging.repository;

import org.example.learnlink.modules.messaging.entity.GroupMessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupMessageReadStatus entity operations.
 */
@Repository
public interface GroupMessageReadStatusRepository extends JpaRepository<GroupMessageReadStatus, Long> {

    /**
     * Check if a user has read a specific message.
     */
    boolean existsByGroupMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Find read status for a specific message and user.
     */
    Optional<GroupMessageReadStatus> findByGroupMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Get all read statuses for a message (to show who has read it).
     */
    List<GroupMessageReadStatus> findByGroupMessageId(Long messageId);

    /**
     * Count how many users have read a message.
     */
    Long countByGroupMessageId(Long messageId);

    /**
     * Mark all unread messages in a group as read for a user.
     * Uses native query for bulk update efficiency.
     */
    @Modifying
    @Query(value = """
        INSERT INTO group_message_read_status (message_id, user_id, read_at)
        SELECT gm.id, :userId, CURRENT_TIMESTAMP
        FROM group_messages gm
        WHERE gm.group_id = :groupId
        AND gm.sender_id != :userId
        AND NOT EXISTS (
            SELECT 1 FROM group_message_read_status rs 
            WHERE rs.message_id = gm.id AND rs.user_id = :userId
        )
    """, nativeQuery = true)
    int markAllAsReadForUser(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
```

---

## 6. Service Layer

### 6.1 IGroupMessageService Interface

**File:** `src/main/java/org/example/learnlink/modules/messaging/service/IGroupMessageService.java`

```java
package org.example.learnlink.modules.messaging.service;

import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for group messaging operations.
 */
public interface IGroupMessageService {

    /**
     * Send a message to a group.
     * 
     * @param senderId ID of the sender
     * @param groupId ID of the study group
     * @param request Message content and metadata
     * @return The created message response
     * @throws GroupAccessDeniedException if user is not a member of the group
     */
    GroupMessageResponse sendMessage(Long senderId, Long groupId, GroupMessageRequest request);

    /**
     * Get paginated message history for a group.
     * 
     * @param userId ID of the requesting user (for membership validation)
     * @param groupId ID of the study group
     * @param pageable Pagination parameters
     * @return Page of messages, newest first
     */
    Page<GroupMessageResponse> getGroupMessages(Long userId, Long groupId, Pageable pageable);

    /**
     * Mark all messages in a group as read for a user.
     * 
     * @param userId ID of the user
     * @param groupId ID of the study group
     * @return Number of messages marked as read
     */
    int markAllAsRead(Long userId, Long groupId);

    /**
     * Mark a specific message as read.
     * 
     * @param userId ID of the user
     * @param messageId ID of the message
     */
    void markMessageAsRead(Long userId, Long messageId);

    /**
     * Get unread message count for a user in a group.
     * 
     * @param userId ID of the user
     * @param groupId ID of the study group
     * @return Number of unread messages
     */
    Long getUnreadCount(Long userId, Long groupId);

    /**
     * Get list of user IDs who have read a message.
     * 
     * @param messageId ID of the message
     * @return List of user IDs who read the message
     */
    List<Long> getMessageReaders(Long messageId);
}
```

### 6.2 GroupMessageServiceImpl

**File:** `src/main/java/org/example/learnlink/modules/messaging/service/GroupMessageServiceImpl.java`

```java
package org.example.learnlink.modules.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.matching.entity.GroupMembership;
import org.example.learnlink.modules.matching.entity.StudyGroup;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;
import org.example.learnlink.modules.matching.repository.GroupMembershipRepository;
import org.example.learnlink.modules.matching.repository.StudyGroupRepository;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.example.learnlink.modules.messaging.entity.GroupMessageReadStatus;
import org.example.learnlink.modules.messaging.event.GroupMessageSentEvent;
import org.example.learnlink.modules.messaging.exception.GroupAccessDeniedException;
import org.example.learnlink.modules.messaging.exception.GroupNotFoundException;
import org.example.learnlink.modules.messaging.exception.MessageNotFoundException;
import org.example.learnlink.modules.messaging.mapper.GroupMessageMapper;
import org.example.learnlink.modules.messaging.repository.GroupMessageReadStatusRepository;
import org.example.learnlink.modules.messaging.repository.GroupMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IGroupMessageService.
 * Handles all group messaging business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageServiceImpl implements IGroupMessageService {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupMessageReadStatusRepository readStatusRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupMessageMapper groupMessageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public GroupMessageResponse sendMessage(Long senderId, Long groupId, GroupMessageRequest request) {
        log.info("User {} sending message to group {}", senderId, groupId);

        // Validate group exists
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Validate user is an active member of the group
        validateMembership(senderId, groupId);

        // Create and save the message
        GroupMessage message = GroupMessage.builder()
                .studyGroup(group)
                .senderId(senderId)
                .content(request.getContent())
                .messageType(request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        GroupMessage savedMessage = groupMessageRepository.save(message);

        // Publish event for WebSocket broadcast and gamification
        eventPublisher.publishEvent(new GroupMessageSentEvent(
                this,
                savedMessage.getId(),
                groupId,
                senderId,
                getGroupMemberIds(groupId)
        ));

        log.info("Group message {} sent successfully", savedMessage.getId());
        return groupMessageMapper.toResponse(savedMessage, senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupMessageResponse> getGroupMessages(Long userId, Long groupId, Pageable pageable) {
        log.debug("User {} fetching messages for group {}", userId, groupId);

        // Validate membership
        validateMembership(userId, groupId);

        return groupMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(msg -> groupMessageMapper.toResponse(msg, userId));
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long groupId) {
        log.debug("Marking all messages as read for user {} in group {}", userId, groupId);

        // Validate membership
        validateMembership(userId, groupId);

        return readStatusRepository.markAllAsReadForUser(groupId, userId);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long userId, Long messageId) {
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        // Validate membership
        validateMembership(userId, message.getStudyGroup().getId());

        // Don't mark own messages as read
        if (message.getSenderId().equals(userId)) {
            return;
        }

        // Check if already read
        if (readStatusRepository.existsByGroupMessageIdAndUserId(messageId, userId)) {
            return;
        }

        GroupMessageReadStatus readStatus = GroupMessageReadStatus.builder()
                .groupMessage(message)
                .userId(userId)
                .readAt(LocalDateTime.now())
                .build();

        readStatusRepository.save(readStatus);
        log.debug("Message {} marked as read by user {}", messageId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId, Long groupId) {
        return groupMessageRepository.countUnreadMessages(groupId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMessageReaders(Long messageId) {
        return readStatusRepository.findByGroupMessageId(messageId)
                .stream()
                .map(GroupMessageReadStatus::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * Validate that a user is an active member of a group.
     */
    private void validateMembership(Long userId, Long groupId) {
        boolean isMember = membershipRepository.existsByStudyGroupIdAndUserIdAndStatus(
                groupId, userId, MembershipStatus.ACTIVE
        );
        
        if (!isMember) {
            throw new GroupAccessDeniedException(groupId, userId);
        }
    }

    /**
     * Get all active member IDs for a group.
     */
    private List<Long> getGroupMemberIds(Long groupId) {
        return membershipRepository.findByStudyGroupIdAndStatus(groupId, MembershipStatus.ACTIVE)
                .stream()
                .map(GroupMembership::getUserId)
                .collect(Collectors.toList());
    }
}
```

---

## 7. Controller Layer

### 7.1 GroupChatController (REST)

**File:** `src/main/java/org/example/learnlink/modules/messaging/controller/GroupChatController.java`

```java
package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.dto.PageResponse;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for group chat operations.
 * Provides endpoints for sending messages, fetching history, and managing read status.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group Chat", description = "Group messaging endpoints")
public class GroupChatController {

    private final IGroupMessageService groupMessageService;

    /**
     * Send a message to a group.
     * 
     * POST /api/groups/{groupId}/messages
     */
    @PostMapping
    @Operation(summary = "Send message to group", description = "Send a new message to a study group chat")
    public ResponseEntity<GroupMessageResponse> sendMessage(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMessageRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("REST: User {} sending message to group {}", userId, groupId);
        
        GroupMessageResponse response = groupMessageService.sendMessage(userId, groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get message history for a group.
     * 
     * GET /api/groups/{groupId}/messages?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Get group messages", description = "Get paginated message history for a group")
    public ResponseEntity<PageResponse<GroupMessageResponse>> getMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId
    ) {
        Page<GroupMessageResponse> messages = groupMessageService.getGroupMessages(
                userId,
                groupId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return ResponseEntity.ok(PageResponse.from(messages));
    }

    /**
     * Mark all messages in a group as read.
     * 
     * PUT /api/groups/{groupId}/messages/read
     */
    @PutMapping("/read")
    @Operation(summary = "Mark all as read", description = "Mark all messages in the group as read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId
    ) {
        int count = groupMessageService.markAllAsRead(userId, groupId);
        return ResponseEntity.ok(Map.of("messagesRead", count));
    }

    /**
     * Mark a specific message as read.
     * 
     * PUT /api/groups/{groupId}/messages/{messageId}/read
     */
    @PutMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long groupId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal Long userId
    ) {
        groupMessageService.markMessageAsRead(userId, messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get unread message count for current user in a group.
     * 
     * GET /api/groups/{groupId}/messages/unread-count
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get number of unread messages in the group")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId
    ) {
        Long count = groupMessageService.getUnreadCount(userId, groupId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get list of users who have read a specific message.
     * 
     * GET /api/groups/{groupId}/messages/{messageId}/readers
     */
    @GetMapping("/{messageId}/readers")
    @Operation(summary = "Get message readers", description = "Get list of user IDs who have read a message")
    public ResponseEntity<List<Long>> getMessageReaders(
            @PathVariable Long groupId,
            @PathVariable Long messageId
    ) {
        List<Long> readers = groupMessageService.getMessageReaders(messageId);
        return ResponseEntity.ok(readers);
    }
}
```

---

## 8. WebSocket Integration

### 8.1 GroupChatWebSocketController

**File:** `src/main/java/org/example/learnlink/modules/messaging/controller/GroupChatWebSocketController.java`

```java
package org.example.learnlink.modules.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.config.WebSocketAuthInterceptor.WebSocketPrincipal;
import org.example.learnlink.modules.messaging.dto.*;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket Controller for real-time group chat messaging.
 * 
 * <h3>Client Subscriptions:</h3>
 * <pre>
 * // Subscribe to group messages
 * stompClient.subscribe('/topic/group/{groupId}', (message) => {
 *     console.log('New group message:', JSON.parse(message.body));
 * });
 * 
 * // Subscribe to group typing indicators
 * stompClient.subscribe('/topic/group/{groupId}/typing', (indicator) => {
 *     console.log('Typing:', JSON.parse(indicator.body));
 * });
 * </pre>
 * 
 * <h3>Client Send Destinations:</h3>
 * <pre>
 * // Send a group message
 * stompClient.send('/app/group.send', {}, JSON.stringify({
 *     groupId: 123,
 *     content: 'Hello everyone!',
 *     type: 'TEXT'
 * }));
 * 
 * // Send typing indicator
 * stompClient.send('/app/group.typing', {}, JSON.stringify({
 *     groupId: 123,
 *     typing: true
 * }));
 * </pre>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GroupChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IGroupMessageService groupMessageService;

    /**
     * Handle incoming group message via WebSocket.
     * 
     * Client sends to: /app/group.send
     * Message broadcasted to: /topic/group/{groupId}
     */
    @MessageMapping("/group.send")
    public void sendGroupMessage(@Payload GroupChatMessageRequest request, Principal principal) {
        Long senderId = getUserId(principal);
        log.debug("WebSocket group message from user {} to group {}", senderId, request.getGroupId());

        // Convert to service request and save
        GroupMessageRequest serviceRequest = GroupMessageRequest.builder()
                .content(request.getContent())
                .type(request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        // Save message and get response
        GroupMessageResponse response = groupMessageService.sendMessage(
                senderId, 
                request.getGroupId(), 
                serviceRequest
        );

        // Broadcast to all group subscribers
        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getGroupId(),
                response
        );

        log.debug("Group message {} broadcasted to /topic/group/{}", response.getId(), request.getGroupId());
    }

    /**
     * Handle group typing indicator.
     * 
     * Client sends to: /app/group.typing
     * Notification broadcasted to: /topic/group/{groupId}/typing
     */
    @MessageMapping("/group.typing")
    public void handleGroupTyping(@Payload GroupTypingIndicator indicator, Principal principal) {
        Long senderId = getUserId(principal);
        log.trace("Group typing indicator from user {} in group {}: {}", 
                senderId, indicator.getGroupId(), indicator.isTyping());

        GroupTypingNotification notification = GroupTypingNotification.builder()
                .userId(senderId)
                .groupId(indicator.getGroupId())
                .typing(indicator.isTyping())
                .timestamp(LocalDateTime.now())
                .build();

        // Broadcast to all group subscribers (except sender - handled by client)
        messagingTemplate.convertAndSend(
                "/topic/group/" + indicator.getGroupId() + "/typing",
                notification
        );
    }

    /**
     * Handle read receipt for group messages.
     * 
     * Client sends to: /app/group.read
     * Notification broadcasted to: /topic/group/{groupId}/read
     */
    @MessageMapping("/group.read")
    public void handleReadReceipt(@Payload GroupReadReceipt receipt, Principal principal) {
        Long userId = getUserId(principal);
        log.trace("Group read receipt from user {} for message {} in group {}", 
                userId, receipt.getMessageId(), receipt.getGroupId());

        // Mark message as read
        groupMessageService.markMessageAsRead(userId, receipt.getMessageId());

        // Broadcast read status to group
        GroupReadNotification notification = GroupReadNotification.builder()
                .userId(userId)
                .groupId(receipt.getGroupId())
                .messageId(receipt.getMessageId())
                .readAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/group/" + receipt.getGroupId() + "/read",
                notification
        );
    }

    /**
     * Extract user ID from WebSocket principal.
     */
    private Long getUserId(Principal principal) {
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            return wsPrincipal.getUserId();
        }
        throw new IllegalStateException("Invalid principal type");
    }
}
```

### 8.2 WebSocket Configuration Update

Add group topics to the existing WebSocketConfig:

```java
// In WebSocketConfig.java - update configureMessageBroker method:

@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Enable simple broker for:
    // - /topic/* for broadcast (including /topic/group/*)
    // - /queue/* for point-to-point (user-specific)
    registry.enableSimpleBroker("/topic", "/queue");
    
    // Prefix for messages FROM client TO server
    registry.setApplicationDestinationPrefixes("/app");
    
    // Prefix for user-specific destinations
    registry.setUserDestinationPrefix("/user");
}
```

---

## 9. DTOs

### 9.1 Request DTOs

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupMessageRequest.java`

```java
package org.example.learnlink.modules.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

/**
 * Request DTO for sending a message to a group (REST API).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private String attachmentUrl;
    private String attachmentName;
}
```

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupChatMessageRequest.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

/**
 * Request DTO for sending a message via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatMessageRequest {
    private Long groupId;
    private String content;
    
    @Builder.Default
    private MessageType type = MessageType.TEXT;
    
    private String attachmentUrl;
    private String attachmentName;
}
```

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupTypingIndicator.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;

/**
 * DTO for group typing indicator from client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTypingIndicator {
    private Long groupId;
    private boolean typing;
}
```

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupReadReceipt.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;

/**
 * DTO for group message read receipt from client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupReadReceipt {
    private Long groupId;
    private Long messageId;
}
```

### 9.2 Response DTOs

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupMessageResponse.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

import java.time.LocalDateTime;

/**
 * Response DTO for a group message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageResponse {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderName;       // Fetched from User service
    private String senderAvatarUrl;  // Fetched from User service
    private String content;
    private MessageType type;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime createdAt;
    private int readCount;           // Number of users who read this message
    private boolean isReadByCurrentUser;
}
```

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupTypingNotification.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification DTO for group typing indicator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTypingNotification {
    private Long userId;
    private String userName;   // Optional: fetched from User service
    private Long groupId;
    private boolean typing;
    private LocalDateTime timestamp;
}
```

**File:** `src/main/java/org/example/learnlink/modules/messaging/dto/GroupReadNotification.java`

```java
package org.example.learnlink.modules.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification DTO for group read receipt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupReadNotification {
    private Long userId;
    private Long groupId;
    private Long messageId;
    private LocalDateTime readAt;
}
```

---

## 10. Events

### 10.1 GroupMessageSentEvent

**File:** `src/main/java/org/example/learnlink/modules/messaging/event/GroupMessageSentEvent.java`

```java
package org.example.learnlink.modules.messaging.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event published when a group message is sent.
 * Used for:
 * - WebSocket broadcast
 * - Gamification points
 * - Notifications
 */
@Getter
public class GroupMessageSentEvent extends ApplicationEvent {
    
    private final Long messageId;
    private final Long groupId;
    private final Long senderId;
    private final List<Long> memberIds;

    public GroupMessageSentEvent(Object source, Long messageId, Long groupId, 
                                  Long senderId, List<Long> memberIds) {
        super(source);
        this.messageId = messageId;
        this.groupId = groupId;
        this.senderId = senderId;
        this.memberIds = memberIds;
    }
}
```

### 10.2 GroupMessageEventListener

**File:** `src/main/java/org/example/learnlink/modules/messaging/listener/GroupMessageEventListener.java`

```java
package org.example.learnlink.modules.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.service.ScoreService;
import org.example.learnlink.modules.messaging.event.GroupMessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for group messaging events.
 * Handles gamification and other cross-cutting concerns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupMessageEventListener {

    private final ScoreService scoreService;
    // private final NotificationService notificationService; // If needed

    /**
     * Award points for sending a group message.
     * Points: 2 points per group message (encourages participation)
     */
    @Async
    @EventListener
    public void handleGroupMessageSent(GroupMessageSentEvent event) {
        log.debug("Processing GroupMessageSentEvent for message {} in group {}", 
                event.getMessageId(), event.getGroupId());

        try {
            // Award 2 points for group message participation
            scoreService.addPoints(
                    event.getSenderId(),
                    2,
                    "GROUP_MESSAGE",
                    "Sent message in study group"
            );
        } catch (Exception e) {
            log.error("Failed to process group message event", e);
        }
    }
}
```

---

## 11. Migration Script

**File:** `src/main/resources/db/migration/V1_3_0__Create_Group_Messages_Table.sql`

```sql
-- V1_3_0__Create_Group_Messages_Table.sql
-- Create tables for group chat messaging feature

-- Group Messages Table
CREATE TABLE IF NOT EXISTS group_messages (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(4000) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to study_groups table
    CONSTRAINT fk_group_message_group 
        FOREIGN KEY (group_id) 
        REFERENCES study_groups(id) 
        ON DELETE CASCADE
);

-- Indexes for group_messages
CREATE INDEX idx_group_msg_group_id ON group_messages(group_id);
CREATE INDEX idx_group_msg_sender_id ON group_messages(sender_id);
CREATE INDEX idx_group_msg_created_at ON group_messages(created_at DESC);
CREATE INDEX idx_group_msg_group_created ON group_messages(group_id, created_at DESC);

-- Group Message Read Status Table
CREATE TABLE IF NOT EXISTS group_message_read_status (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to group_messages table
    CONSTRAINT fk_read_status_message 
        FOREIGN KEY (message_id) 
        REFERENCES group_messages(id) 
        ON DELETE CASCADE,
    
    -- Each user can only have one read status per message
    CONSTRAINT uk_message_user_read 
        UNIQUE (message_id, user_id)
);

-- Indexes for group_message_read_status
CREATE INDEX idx_read_status_message_id ON group_message_read_status(message_id);
CREATE INDEX idx_read_status_user_id ON group_message_read_status(user_id);

-- Comments for documentation
COMMENT ON TABLE group_messages IS 'Stores chat messages within study groups';
COMMENT ON COLUMN group_messages.group_id IS 'Reference to the study group';
COMMENT ON COLUMN group_messages.sender_id IS 'ID of the user who sent the message';
COMMENT ON COLUMN group_messages.content IS 'Message content (up to 4000 characters)';
COMMENT ON COLUMN group_messages.message_type IS 'Type: TEXT, IMAGE, FILE, AUDIO, VIDEO';

COMMENT ON TABLE group_message_read_status IS 'Tracks which users have read which messages';
COMMENT ON COLUMN group_message_read_status.message_id IS 'Reference to the group message';
COMMENT ON COLUMN group_message_read_status.user_id IS 'ID of the user who read the message';
COMMENT ON COLUMN group_message_read_status.read_at IS 'Timestamp when the message was read';
```

---

## 12. Testing

### 12.1 Unit Test Example

**File:** `src/test/java/org/example/learnlink/modules/messaging/service/GroupMessageServiceTest.java`

```java
package org.example.learnlink.modules.messaging.service;

import org.example.learnlink.modules.matching.entity.StudyGroup;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;
import org.example.learnlink.modules.matching.repository.GroupMembershipRepository;
import org.example.learnlink.modules.matching.repository.StudyGroupRepository;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.example.learnlink.modules.messaging.entity.MessageType;
import org.example.learnlink.modules.messaging.exception.GroupAccessDeniedException;
import org.example.learnlink.modules.messaging.mapper.GroupMessageMapper;
import org.example.learnlink.modules.messaging.repository.GroupMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMessageServiceTest {

    @Mock
    private GroupMessageRepository groupMessageRepository;
    
    @Mock
    private StudyGroupRepository studyGroupRepository;
    
    @Mock
    private GroupMembershipRepository membershipRepository;
    
    @Mock
    private GroupMessageMapper groupMessageMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GroupMessageServiceImpl groupMessageService;

    private StudyGroup testGroup;
    private GroupMessage testMessage;
    private GroupMessageResponse testResponse;

    @BeforeEach
    void setUp() {
        testGroup = StudyGroup.builder()
                .id(1L)
                .name("Test Group")
                .ownerId(100L)
                .build();

        testMessage = GroupMessage.builder()
                .id(1L)
                .studyGroup(testGroup)
                .senderId(100L)
                .content("Hello group!")
                .messageType(MessageType.TEXT)
                .build();

        testResponse = GroupMessageResponse.builder()
                .id(1L)
                .groupId(1L)
                .senderId(100L)
                .content("Hello group!")
                .type(MessageType.TEXT)
                .build();
    }

    @Test
    void sendMessage_Success() {
        // Given
        Long senderId = 100L;
        Long groupId = 1L;
        GroupMessageRequest request = GroupMessageRequest.builder()
                .content("Hello group!")
                .type(MessageType.TEXT)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(membershipRepository.existsByStudyGroupIdAndUserIdAndStatus(
                groupId, senderId, MembershipStatus.ACTIVE)).thenReturn(true);
        when(groupMessageRepository.save(any(GroupMessage.class))).thenReturn(testMessage);
        when(groupMessageMapper.toResponse(any(), eq(senderId))).thenReturn(testResponse);

        // When
        GroupMessageResponse result = groupMessageService.sendMessage(senderId, groupId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello group!");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void sendMessage_NonMember_ThrowsException() {
        // Given
        Long senderId = 999L; // Non-member
        Long groupId = 1L;
        GroupMessageRequest request = GroupMessageRequest.builder()
                .content("Hello!")
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(membershipRepository.existsByStudyGroupIdAndUserIdAndStatus(
                groupId, senderId, MembershipStatus.ACTIVE)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> groupMessageService.sendMessage(senderId, groupId, request))
                .isInstanceOf(GroupAccessDeniedException.class);
    }
}
```

### 12.2 Integration Test Example

**File:** `src/test/java/org/example/learnlink/modules/messaging/controller/GroupChatControllerIntegrationTest.java`

```java
package org.example.learnlink.modules.messaging.controller;

import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.entity.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class GroupChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getGroupMessages_Returns200() throws Exception {
        mockMvc.perform(get("/api/groups/1/messages")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void sendMessage_ValidRequest_Returns201() throws Exception {
        String requestBody = """
            {
                "content": "Hello everyone!",
                "type": "TEXT"
            }
            """;

        mockMvc.perform(post("/api/groups/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello everyone!"));
    }
}
```

---

## 13. Usage Examples

### 13.1 Frontend Integration (React + STOMP)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Initialize STOMP client
const client = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    connectHeaders: {
        'X-User-Id': userId.toString(),
        'Authorization': `Bearer ${authToken}`
    },
    onConnect: () => {
        console.log('Connected to WebSocket');
        
        // Subscribe to group messages
        subscribeToGroup(groupId);
    },
    onDisconnect: () => {
        console.log('Disconnected from WebSocket');
    }
});

// Subscribe to a group's messages
function subscribeToGroup(groupId) {
    // Subscribe to messages
    client.subscribe(`/topic/group/${groupId}`, (message) => {
        const groupMessage = JSON.parse(message.body);
        console.log('New group message:', groupMessage);
        // Update UI with new message
        addMessageToChat(groupMessage);
    });

    // Subscribe to typing indicators
    client.subscribe(`/topic/group/${groupId}/typing`, (message) => {
        const typing = JSON.parse(message.body);
        console.log('Typing indicator:', typing);
        // Show/hide typing indicator
        updateTypingIndicator(typing);
    });

    // Subscribe to read receipts
    client.subscribe(`/topic/group/${groupId}/read`, (message) => {
        const readReceipt = JSON.parse(message.body);
        console.log('Read receipt:', readReceipt);
        // Update message read status
        updateMessageReadStatus(readReceipt);
    });
}

// Send a group message
function sendGroupMessage(groupId, content, type = 'TEXT') {
    client.publish({
        destination: '/app/group.send',
        body: JSON.stringify({
            groupId: groupId,
            content: content,
            type: type
        })
    });
}

// Send typing indicator
function sendTypingIndicator(groupId, isTyping) {
    client.publish({
        destination: '/app/group.typing',
        body: JSON.stringify({
            groupId: groupId,
            typing: isTyping
        })
    });
}

// Send read receipt
function sendReadReceipt(groupId, messageId) {
    client.publish({
        destination: '/app/group.read',
        body: JSON.stringify({
            groupId: groupId,
            messageId: messageId
        })
    });
}

// Connect
client.activate();
```

### 13.2 REST API Usage (cURL)

```bash
# Get group messages (paginated)
curl -X GET "http://localhost:8080/api/groups/1/messages?page=0&size=20" \
  -H "Authorization: Bearer <token>"

# Send a message to a group
curl -X POST "http://localhost:8080/api/groups/1/messages" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello everyone! Ready to study?",
    "type": "TEXT"
  }'

# Send a message with attachment
curl -X POST "http://localhost:8080/api/groups/1/messages" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Here are my notes from today",
    "type": "FILE",
    "attachmentUrl": "https://s3.amazonaws.com/bucket/notes.pdf",
    "attachmentName": "Chapter5_Notes.pdf"
  }'

# Mark all messages as read
curl -X PUT "http://localhost:8080/api/groups/1/messages/read" \
  -H "Authorization: Bearer <token>"

# Get unread count
curl -X GET "http://localhost:8080/api/groups/1/messages/unread-count" \
  -H "Authorization: Bearer <token>"

# Get readers of a specific message
curl -X GET "http://localhost:8080/api/groups/1/messages/42/readers" \
  -H "Authorization: Bearer <token>"
```

---

## Summary

### Files to Create

| File | Location |
|------|----------|
| `GroupMessage.java` | `modules/messaging/entity/` |
| `GroupMessageReadStatus.java` | `modules/messaging/entity/` |
| `GroupMessageRepository.java` | `modules/messaging/repository/` |
| `GroupMessageReadStatusRepository.java` | `modules/messaging/repository/` |
| `IGroupMessageService.java` | `modules/messaging/service/` |
| `GroupMessageServiceImpl.java` | `modules/messaging/service/` |
| `GroupChatController.java` | `modules/messaging/controller/` |
| `GroupChatWebSocketController.java` | `modules/messaging/controller/` |
| `GroupMessageRequest.java` | `modules/messaging/dto/` |
| `GroupChatMessageRequest.java` | `modules/messaging/dto/` |
| `GroupMessageResponse.java` | `modules/messaging/dto/` |
| `GroupTypingIndicator.java` | `modules/messaging/dto/` |
| `GroupTypingNotification.java` | `modules/messaging/dto/` |
| `GroupReadReceipt.java` | `modules/messaging/dto/` |
| `GroupReadNotification.java` | `modules/messaging/dto/` |
| `GroupMessageSentEvent.java` | `modules/messaging/event/` |
| `GroupMessageEventListener.java` | `modules/messaging/listener/` |
| `GroupMessageMapper.java` | `modules/messaging/mapper/` |
| `GroupAccessDeniedException.java` | `modules/messaging/exception/` |
| `GroupNotFoundException.java` | `modules/messaging/exception/` |
| `V1_3_0__Create_Group_Messages_Table.sql` | `resources/db/migration/` |

### Gamification Points

| Action | Points |
|--------|--------|
| Send group message | +2 |
| First message in a group | +5 (bonus) |
| Answer a question in group | +3 |

### API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groups/{groupId}/messages` | Send message |
| GET | `/api/groups/{groupId}/messages` | Get messages (paginated) |
| PUT | `/api/groups/{groupId}/messages/read` | Mark all as read |
| PUT | `/api/groups/{groupId}/messages/{id}/read` | Mark one as read |
| GET | `/api/groups/{groupId}/messages/unread-count` | Get unread count |
| GET | `/api/groups/{groupId}/messages/{id}/readers` | Get readers |

### WebSocket Destinations

| Direction | Destination | Description |
|-----------|-------------|-------------|
| Client → Server | `/app/group.send` | Send message |
| Client → Server | `/app/group.typing` | Typing indicator |
| Client → Server | `/app/group.read` | Read receipt |
| Server → Client | `/topic/group/{id}` | Receive messages |
| Server → Client | `/topic/group/{id}/typing` | Typing updates |
| Server → Client | `/topic/group/{id}/read` | Read updates |
