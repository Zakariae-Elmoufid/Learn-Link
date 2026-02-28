# Notification System Implementation Guide

## 📋 Overview

This document provides a comprehensive guide for implementing the LearnLink notification system based on the existing architecture patterns. The notification module will integrate with the event-driven architecture already in place.

---

## 🏗️ Module Structure

```
modules/notification/
├── controller/
│   └── NotificationController.java
├── dto/
│   ├── request/
│   │   └── UpdatePreferencesRequest.java
│   └── response/
│       ├── NotificationResponse.java
│       ├── NotificationPreferencesResponse.java
│       └── UnreadCountResponse.java
├── entity/
│   ├── Notification.java
│   ├── NotificationPreference.java
│   └── NotificationType.java
├── event/
│   ├── NotificationEvent.java
│   └── PushNotificationEvent.java
├── listener/
│   ├── NotificationEventListener.java
│   └── NotificationTriggerListener.java
├── repository/
│   ├── NotificationRepository.java
│   └── NotificationPreferenceRepository.java
├── service/
│   ├── NotificationService.java
│   ├── NotificationServiceImpl.java
│   ├── EmailNotificationService.java
│   ├── PushNotificationService.java
│   └── NotificationPreferenceService.java
└── config/
    └── WebPushConfig.java
```

---

## 📊 Database Schema

### Migration: `V1_7_0__Create_Notification_Tables.sql`

```sql
-- Notification types enum values
-- TASK_REMINDER, TASK_DUE, CONNECTION_REQUEST, CONNECTION_ACCEPTED,
-- NEW_MESSAGE, POST_LIKED, POST_COMMENTED, QUESTION_ANSWERED, 
-- ANSWER_ACCEPTED, BADGE_EARNED, POINTS_EARNED, LEVEL_UP,
-- GROUP_INVITATION, SESSION_REMINDER, MODERATION_ACTION, SYSTEM

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,  -- Additional data (link, entity_id, etc.)
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Notification preferences table
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- In-app notification preferences
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Email notification preferences
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_tasks BOOLEAN NOT NULL DEFAULT TRUE,
    email_connections BOOLEAN NOT NULL DEFAULT TRUE,
    email_messages BOOLEAN NOT NULL DEFAULT FALSE,  -- Default OFF (too frequent)
    email_community BOOLEAN NOT NULL DEFAULT TRUE,
    email_gamification BOOLEAN NOT NULL DEFAULT TRUE,
    email_system BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Push notification preferences (optional)
    push_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    push_subscription JSONB,  -- Web Push subscription object
    
    -- Digest preferences
    email_digest_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    email_digest_frequency VARCHAR(20) DEFAULT 'DAILY',  -- DAILY, WEEKLY
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_notification_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);
```

---

## 🔧 Entity Definitions

### NotificationType.java

```java
package org.example.learnlink.modules.notification.entity;

/**
 * Types of notifications in the system
 */
public enum NotificationType {
    // Task related
    TASK_REMINDER("Task Reminder", "tasks"),
    TASK_DUE("Task Due", "tasks"),
    
    // Connection related
    CONNECTION_REQUEST("Connection Request", "connections"),
    CONNECTION_ACCEPTED("Connection Accepted", "connections"),
    
    // Messaging
    NEW_MESSAGE("New Message", "messages"),
    
    // Community
    POST_LIKED("Post Liked", "community"),
    POST_COMMENTED("Post Commented", "community"),
    QUESTION_ANSWERED("Question Answered", "community"),
    ANSWER_ACCEPTED("Answer Accepted", "community"),
    
    // Gamification
    BADGE_EARNED("Badge Earned", "gamification"),
    POINTS_EARNED("Points Earned", "gamification"),
    LEVEL_UP("Level Up", "gamification"),
    
    // Groups & Sessions
    GROUP_INVITATION("Group Invitation", "groups"),
    SESSION_REMINDER("Study Session Reminder", "groups"),
    
    // Admin/Moderation
    MODERATION_ACTION("Moderation Action", "system"),
    
    // System
    SYSTEM("System Notification", "system");

    private final String displayName;
    private final String category;

    NotificationType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }
}
```

### Notification.java

```java
package org.example.learnlink.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data;  // link, entityId, entityType, etc.

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
```

### NotificationPreference.java

```java
package org.example.learnlink.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // In-app
    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    // Email preferences
    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "email_tasks")
    @Builder.Default
    private Boolean emailTasks = true;

    @Column(name = "email_connections")
    @Builder.Default
    private Boolean emailConnections = true;

    @Column(name = "email_messages")
    @Builder.Default
    private Boolean emailMessages = false;

    @Column(name = "email_community")
    @Builder.Default
    private Boolean emailCommunity = true;

    @Column(name = "email_gamification")
    @Builder.Default
    private Boolean emailGamification = true;

    @Column(name = "email_system")
    @Builder.Default
    private Boolean emailSystem = true;

    // Push notifications
    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "push_subscription", columnDefinition = "jsonb")
    private Map<String, Object> pushSubscription;

    // Digest
    @Column(name = "email_digest_enabled")
    @Builder.Default
    private Boolean emailDigestEnabled = false;

    @Column(name = "email_digest_frequency")
    @Builder.Default
    private String emailDigestFrequency = "DAILY";

    @Column(name = "created_at", updatable = false)
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
     * Check if email should be sent for a given notification type
     */
    public boolean shouldSendEmail(NotificationType type) {
        if (!emailEnabled) return false;
        
        return switch (type.getCategory()) {
            case "tasks" -> emailTasks;
            case "connections" -> emailConnections;
            case "messages" -> emailMessages;
            case "community" -> emailCommunity;
            case "gamification" -> emailGamification;
            case "system" -> emailSystem;
            default -> true;
        };
    }
}
```

---

## 🎯 Event Definitions

### NotificationEvent.java

```java
package org.example.learnlink.modules.notification.event;

import lombok.Getter;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Generic event for triggering notifications.
 * Published by various modules, consumed by NotificationEventListener.
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Map<String, Object> data;

    public NotificationEvent(Object source, Long userId, NotificationType type, 
                            String title, String message, Map<String, Object> data) {
        super(source);
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
    }

    public static NotificationEventBuilder builder(Object source) {
        return new NotificationEventBuilder(source);
    }

    public static class NotificationEventBuilder {
        private final Object source;
        private Long userId;
        private NotificationType type;
        private String title;
        private String message;
        private Map<String, Object> data;

        public NotificationEventBuilder(Object source) {
            this.source = source;
        }

        public NotificationEventBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public NotificationEventBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationEventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationEventBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationEventBuilder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(source, userId, type, title, message, data);
        }
    }
}
```

---

## 📦 Repository Layer

### NotificationRepository.java

```java
package org.example.learnlink.modules.notification.repository;

import org.example.learnlink.modules.notification.entity.Notification;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user, ordered by creation date
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Find notifications by type for a user
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId, NotificationType type, Pageable pageable);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark specific notifications as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id IN :ids AND n.userId = :userId")
    int markAsRead(@Param("ids") List<Long> ids, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old notifications (for cleanup job)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before AND n.isRead = true")
    int deleteOldReadNotifications(@Param("before") LocalDateTime before);
}
```

### NotificationPreferenceRepository.java

```java
package org.example.learnlink.modules.notification.repository;

import org.example.learnlink.modules.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
```

---

## 🛠️ Service Layer

### NotificationService.java (Interface)

```java
package org.example.learnlink.modules.notification.service;

import org.example.learnlink.modules.notification.dto.response.NotificationResponse;
import org.example.learnlink.modules.notification.dto.response.UnreadCountResponse;
import org.example.learnlink.modules.notification.entity.Notification;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * Create and send a notification
     */
    Notification createNotification(Long userId, NotificationType type, 
                                   String title, String message, Map<String, Object> data);

    /**
     * Get all notifications for a user
     */
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Get unread notifications for a user
     */
    Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Get unread count for a user
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * Mark a single notification as read
     */
    NotificationResponse markAsRead(Long notificationId, Long userId);

    /**
     * Mark multiple notifications as read
     */
    int markMultipleAsRead(List<Long> notificationIds, Long userId);

    /**
     * Mark all notifications as read for a user
     */
    int markAllAsRead(Long userId);

    /**
     * Delete a notification
     */
    void deleteNotification(Long notificationId, Long userId);
}
```

### NotificationServiceImpl.java

```java
package org.example.learnlink.modules.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.example.learnlink.modules.notification.dto.response.NotificationResponse;
import org.example.learnlink.modules.notification.dto.response.UnreadCountResponse;
import org.example.learnlink.modules.notification.entity.Notification;
import org.example.learnlink.modules.notification.entity.NotificationPreference;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.example.learnlink.modules.notification.repository.NotificationPreferenceRepository;
import org.example.learnlink.modules.notification.repository.NotificationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailNotificationService;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    @CacheEvict(value = "notification:unread", key = "#userId")
    public Notification createNotification(Long userId, NotificationType type,
                                          String title, String message, Map<String, Object> data) {
        log.info("Creating notification for user {}: type={}, title={}", userId, type, title);

        // Get user preferences (or create default if not exists)
        NotificationPreference preferences = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        Notification notification = null;

        // 1. Create in-app notification (if enabled)
        if (preferences.getInAppEnabled()) {
            notification = Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .data(data)
                    .build();
            notification = notificationRepository.save(notification);

            // Push via WebSocket
            sendWebSocketNotification(userId, notification);
        }

        // 2. Send email notification (if enabled for this type)
        if (preferences.shouldSendEmail(type)) {
            emailNotificationService.sendNotificationEmail(userId, type, title, message, data);
        }

        // 3. Send push notification (if enabled)
        if (preferences.getPushEnabled() && preferences.getPushSubscription() != null) {
            pushNotificationService.sendPushNotification(preferences.getPushSubscription(), title, message, data);
        }

        return notification;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notification:unread", key = "#userId")
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder()
                .userId(userId)
                .unreadCount(count)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "notification:unread", key = "#userId")
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Notification does not belong to user");
        }

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notification:unread", key = "#userId")
    public int markMultipleAsRead(List<Long> notificationIds, Long userId) {
        return notificationRepository.markAsRead(notificationIds, userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    @CacheEvict(value = "notification:unread", key = "#userId")
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    @CacheEvict(value = "notification:unread", key = "#userId")
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Notification does not belong to user");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Push notification via WebSocket to connected client
     */
    private void sendWebSocketNotification(Long userId, Notification notification) {
        try {
            NotificationResponse response = mapToResponse(notification);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    response
            );
            log.debug("WebSocket notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .build();
        return preferenceRepository.save(pref);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeName(notification.getType().getDisplayName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
```

### EmailNotificationService.java

```java
package org.example.learnlink.modules.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send notification email asynchronously
     */
    @Async
    public void sendNotificationEmail(Long userId, NotificationType type, 
                                     String title, String message, Map<String, Object> data) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email: user {} not found or no email", userId);
                return;
            }

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("[LearnLink] " + title);
            
            // Build email body
            StringBuilder body = new StringBuilder();
            body.append("Hello ").append(user.getUsername()).append(",\n\n");
            body.append(message).append("\n\n");
            
            // Add link if available
            if (data != null && data.containsKey("link")) {
                body.append("Click here to view: ").append(appUrl).append(data.get("link")).append("\n\n");
            }
            
            body.append("---\n");
            body.append("LearnLink - Your Study Companion\n");
            body.append("Manage notification preferences: ").append(appUrl).append("/settings/notifications");
            
            mailMessage.setText(body.toString());
            
            mailSender.send(mailMessage);
            log.info("Email notification sent to user {} for type {}", userId, type);
            
        } catch (Exception e) {
            log.error("Failed to send email notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send HTML email with template (optional enhancement)
     */
    public void sendHtmlNotificationEmail(Long userId, String templateName, Map<String, Object> variables) {
        // Implementation for HTML emails using Thymeleaf templates
        // Requires spring-boot-starter-thymeleaf dependency
    }
}
```

### PushNotificationService.java

```java
package org.example.learnlink.modules.notification.service;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Service for sending Web Push notifications.
 * 
 * Requires: 
 * - dependency: com.github.nickmcdowall:webpush-java:5.1.0
 * - VAPID keys generated
 */
@Slf4j
@Service
public class PushNotificationService {

    private final PushService pushService;
    private final ObjectMapper objectMapper;

    @Value("${push.vapid.public-key:}")
    private String vapidPublicKey;

    public PushNotificationService(ObjectMapper objectMapper,
                                  @Value("${push.vapid.public-key:}") String publicKey,
                                  @Value("${push.vapid.private-key:}") String privateKey) {
        this.objectMapper = objectMapper;
        
        if (!publicKey.isEmpty() && !privateKey.isEmpty()) {
            try {
                this.pushService = new PushService()
                        .setPublicKey(publicKey)
                        .setPrivateKey(privateKey);
            } catch (Exception e) {
                log.warn("Failed to initialize PushService: {}", e.getMessage());
                this.pushService = null;
            }
        } else {
            this.pushService = null;
            log.info("Push notifications disabled (no VAPID keys configured)");
        }
    }

    /**
     * Send push notification asynchronously
     */
    @Async
    public void sendPushNotification(Map<String, Object> subscriptionData, 
                                    String title, String message, Map<String, Object> data) {
        if (pushService == null) {
            log.debug("Push notifications disabled");
            return;
        }

        try {
            // Build subscription from stored data
            Subscription subscription = new Subscription(
                    (String) subscriptionData.get("endpoint"),
                    new Subscription.Keys(
                            (String) subscriptionData.get("p256dh"),
                            (String) subscriptionData.get("auth")
                    )
            );

            // Build payload
            Map<String, Object> payload = Map.of(
                    "title", title,
                    "body", message,
                    "data", data != null ? data : Map.of()
            );

            // Send notification
            Notification notification = new Notification(
                    subscription,
                    objectMapper.writeValueAsString(payload)
            );
            
            pushService.send(notification);
            log.debug("Push notification sent");
            
        } catch (Exception e) {
            log.error("Failed to send push notification: {}", e.getMessage());
        }
    }

    /**
     * Get VAPID public key for client subscription
     */
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
```

### NotificationPreferenceService.java

```java
package org.example.learnlink.modules.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.notification.dto.request.UpdatePreferencesRequest;
import org.example.learnlink.modules.notification.dto.response.NotificationPreferencesResponse;
import org.example.learnlink.modules.notification.entity.NotificationPreference;
import org.example.learnlink.modules.notification.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getUserPreferences(Long userId) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return mapToResponse(pref);
    }

    @Transactional
    public NotificationPreferencesResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        // Update in-app
        if (request.getInAppEnabled() != null) {
            pref.setInAppEnabled(request.getInAppEnabled());
        }

        // Update email preferences
        if (request.getEmailEnabled() != null) {
            pref.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getEmailTasks() != null) {
            pref.setEmailTasks(request.getEmailTasks());
        }
        if (request.getEmailConnections() != null) {
            pref.setEmailConnections(request.getEmailConnections());
        }
        if (request.getEmailMessages() != null) {
            pref.setEmailMessages(request.getEmailMessages());
        }
        if (request.getEmailCommunity() != null) {
            pref.setEmailCommunity(request.getEmailCommunity());
        }
        if (request.getEmailGamification() != null) {
            pref.setEmailGamification(request.getEmailGamification());
        }
        if (request.getEmailSystem() != null) {
            pref.setEmailSystem(request.getEmailSystem());
        }

        // Update push preferences
        if (request.getPushEnabled() != null) {
            pref.setPushEnabled(request.getPushEnabled());
        }

        // Update digest preferences
        if (request.getEmailDigestEnabled() != null) {
            pref.setEmailDigestEnabled(request.getEmailDigestEnabled());
        }
        if (request.getEmailDigestFrequency() != null) {
            pref.setEmailDigestFrequency(request.getEmailDigestFrequency());
        }

        pref = preferenceRepository.save(pref);
        log.info("Updated notification preferences for user {}", userId);

        return mapToResponse(pref);
    }

    /**
     * Subscribe to push notifications
     */
    @Transactional
    public void subscribeToPush(Long userId, Map<String, Object> subscription) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        pref.setPushEnabled(true);
        pref.setPushSubscription(subscription);
        preferenceRepository.save(pref);

        log.info("User {} subscribed to push notifications", userId);
    }

    /**
     * Unsubscribe from push notifications
     */
    @Transactional
    public void unsubscribeFromPush(Long userId) {
        preferenceRepository.findByUserId(userId).ifPresent(pref -> {
            pref.setPushEnabled(false);
            pref.setPushSubscription(null);
            preferenceRepository.save(pref);
            log.info("User {} unsubscribed from push notifications", userId);
        });
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .build();
        return preferenceRepository.save(pref);
    }

    private NotificationPreferencesResponse mapToResponse(NotificationPreference pref) {
        return NotificationPreferencesResponse.builder()
                .userId(pref.getUserId())
                .inAppEnabled(pref.getInAppEnabled())
                .emailEnabled(pref.getEmailEnabled())
                .emailTasks(pref.getEmailTasks())
                .emailConnections(pref.getEmailConnections())
                .emailMessages(pref.getEmailMessages())
                .emailCommunity(pref.getEmailCommunity())
                .emailGamification(pref.getEmailGamification())
                .emailSystem(pref.getEmailSystem())
                .pushEnabled(pref.getPushEnabled())
                .hasPushSubscription(pref.getPushSubscription() != null)
                .emailDigestEnabled(pref.getEmailDigestEnabled())
                .emailDigestFrequency(pref.getEmailDigestFrequency())
                .build();
    }
}
```

---

## 👂 Event Listeners

### NotificationEventListener.java

```java
package org.example.learnlink.modules.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.notification.event.NotificationEvent;
import org.example.learnlink.modules.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for NotificationEvent and creates notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        log.debug("Received notification event for user {}: {}", event.getUserId(), event.getType());
        
        notificationService.createNotification(
                event.getUserId(),
                event.getType(),
                event.getTitle(),
                event.getMessage(),
                event.getData()
        );
    }
}
```

### NotificationTriggerListener.java

```java
package org.example.learnlink.modules.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.gamification.event.BadgeEarnedEvent;
import org.example.learnlink.modules.gamification.event.PointsAwardedEvent;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.example.learnlink.modules.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Listens for various domain events and triggers corresponding notifications.
 * Centralizes notification triggering logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerListener {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Handle badge earned event -> Create BADGE_EARNED notification
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBadgeEarned(BadgeEarnedEvent event) {
        eventPublisher.publishEvent(
                NotificationEvent.builder(this)
                        .userId(event.getUserId())
                        .type(NotificationType.BADGE_EARNED)
                        .title("New Badge Earned!")
                        .message("Congratulations! You earned the \"" + event.getBadgeName() + "\" badge!")
                        .data(Map.of(
                                "badgeId", event.getBadgeId(),
                                "badgeName", event.getBadgeName(),
                                "link", "/profile/badges"
                        ))
                        .build()
        );
    }

    /**
     * Handle points awarded event -> Create POINTS_EARNED notification
     * Note: Only notify for significant point gains to avoid spam
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointsAwarded(PointsAwardedEvent event) {
        // Only notify for significant point gains (e.g., >= 10 points)
        if (event.getPoints() < 10) {
            return;
        }

        eventPublisher.publishEvent(
                NotificationEvent.builder(this)
                        .userId(event.getUserId())
                        .type(NotificationType.POINTS_EARNED)
                        .title("Points Earned!")
                        .message("You earned " + event.getPoints() + " points for: " + event.getActionType())
                        .data(Map.of(
                                "points", event.getPoints(),
                                "action", event.getActionType(),
                                "link", "/profile"
                        ))
                        .build()
        );
    }

    // Add more event handlers for:
    // - ConnectionRequestEvent -> CONNECTION_REQUEST notification
    // - ConnectionAcceptedEvent -> CONNECTION_ACCEPTED notification
    // - NewMessageEvent -> NEW_MESSAGE notification
    // - PostLikedEvent -> POST_LIKED notification
    // - PostCommentedEvent -> POST_COMMENTED notification
    // - QuestionAnsweredEvent -> QUESTION_ANSWERED notification
    // - AnswerAcceptedEvent -> ANSWER_ACCEPTED notification
    // - TaskDueEvent -> TASK_DUE notification
    // etc.
}
```

---

## 🎮 Controller Layer

### NotificationController.java

```java
package org.example.learnlink.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.notification.dto.request.MarkReadRequest;
import org.example.learnlink.modules.notification.dto.request.PushSubscriptionRequest;
import org.example.learnlink.modules.notification.dto.request.UpdatePreferencesRequest;
import org.example.learnlink.modules.notification.dto.response.NotificationPreferencesResponse;
import org.example.learnlink.modules.notification.dto.response.NotificationResponse;
import org.example.learnlink.modules.notification.dto.response.UnreadCountResponse;
import org.example.learnlink.modules.notification.service.NotificationPreferenceService;
import org.example.learnlink.modules.notification.service.NotificationService;
import org.example.learnlink.modules.notification.service.PushNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final PushNotificationService pushNotificationService;

    // ==================== F-N-01: In-app Notifications ====================

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Returns paginated list of user notifications")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(user.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    // ==================== F-N-02: Badge Counter ====================

    @GetMapping("/count")
    @Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@AuthenticationPrincipal User user) {
        UnreadCountResponse count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }

    // ==================== F-N-03: Mark as Read ====================

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        NotificationResponse notification = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/read")
    @Operation(summary = "Mark multiple notifications as read")
    public ResponseEntity<Map<String, Integer>> markMultipleAsRead(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MarkReadRequest request) {
        int count = notificationService.markMultipleAsRead(request.getNotificationIds(), user.getId());
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    // ==================== F-N-04: Mark All as Read ====================

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@AuthenticationPrincipal User user) {
        int count = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    // ==================== F-N-06: Notification Preferences ====================

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(@AuthenticationPrincipal User user) {
        NotificationPreferencesResponse preferences = preferenceService.getUserPreferences(user.getId());
        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        NotificationPreferencesResponse preferences = preferenceService.updatePreferences(user.getId(), request);
        return ResponseEntity.ok(preferences);
    }

    // ==================== F-N-07: Push Notifications (Optional) ====================

    @GetMapping("/push/vapid-key")
    @Operation(summary = "Get VAPID public key for push notification subscription")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        String key = pushNotificationService.getVapidPublicKey();
        return ResponseEntity.ok(Map.of("publicKey", key != null ? key : ""));
    }

    @PostMapping("/push/subscribe")
    @Operation(summary = "Subscribe to push notifications")
    public ResponseEntity<Void> subscribeToPush(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PushSubscriptionRequest request) {
        preferenceService.subscribeToPush(user.getId(), request.getSubscription());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/push/unsubscribe")
    @Operation(summary = "Unsubscribe from push notifications")
    public ResponseEntity<Void> unsubscribeFromPush(@AuthenticationPrincipal User user) {
        preferenceService.unsubscribeFromPush(user.getId());
        return ResponseEntity.ok().build();
    }

    // ==================== Delete Notification ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
```

---

## 📝 DTO Definitions

### Request DTOs

```java
// UpdatePreferencesRequest.java
package org.example.learnlink.modules.notification.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean emailTasks;
    private Boolean emailConnections;
    private Boolean emailMessages;
    private Boolean emailCommunity;
    private Boolean emailGamification;
    private Boolean emailSystem;
    private Boolean pushEnabled;
    private Boolean emailDigestEnabled;
    private String emailDigestFrequency;  // DAILY, WEEKLY
}

// MarkReadRequest.java
package org.example.learnlink.modules.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadRequest {
    @NotEmpty
    private List<Long> notificationIds;
}

// PushSubscriptionRequest.java
package org.example.learnlink.modules.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionRequest {
    @NotNull
    private Map<String, Object> subscription;  // endpoint, keys.p256dh, keys.auth
}
```

### Response DTOs

```java
// NotificationResponse.java
package org.example.learnlink.modules.notification.dto.response;

import lombok.*;
import org.example.learnlink.modules.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String typeName;
    private String title;
    private String message;
    private Map<String, Object> data;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

// UnreadCountResponse.java
package org.example.learnlink.modules.notification.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private Long userId;
    private Long unreadCount;
}

// NotificationPreferencesResponse.java
package org.example.learnlink.modules.notification.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {
    private Long userId;
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean emailTasks;
    private Boolean emailConnections;
    private Boolean emailMessages;
    private Boolean emailCommunity;
    private Boolean emailGamification;
    private Boolean emailSystem;
    private Boolean pushEnabled;
    private Boolean hasPushSubscription;
    private Boolean emailDigestEnabled;
    private String emailDigestFrequency;
}
```

---

## 🔌 WebSocket Configuration Update

Add notification subscription to WebSocket configuration docs:

```java
// Client-side subscription for real-time notifications
stompClient.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    console.log('New notification:', notification);
    // Update UI, show toast, increment badge counter
});
```

---

## 📋 API Endpoints Summary

| Method | Endpoint | Description | Feature ID |
|--------|----------|-------------|------------|
| GET | `/api/notifications` | Get all notifications (paginated) | F-N-01 |
| GET | `/api/notifications/unread` | Get unread notifications | F-N-01 |
| GET | `/api/notifications/count` | Get unread count | F-N-02 |
| PATCH | `/api/notifications/{id}/read` | Mark one as read | F-N-03 |
| PATCH | `/api/notifications/read` | Mark multiple as read | F-N-03 |
| PATCH | `/api/notifications/read-all` | Mark all as read | F-N-04 |
| GET | `/api/notifications/preferences` | Get preferences | F-N-06 |
| PUT | `/api/notifications/preferences` | Update preferences | F-N-06 |
| GET | `/api/notifications/push/vapid-key` | Get VAPID public key | F-N-07 |
| POST | `/api/notifications/push/subscribe` | Subscribe to push | F-N-07 |
| DELETE | `/api/notifications/push/unsubscribe` | Unsubscribe from push | F-N-07 |
| DELETE | `/api/notifications/{id}` | Delete notification | - |

---

## 🔄 Integration Points

### Publishing Notifications from Other Modules

Example: In `TaskService` when task is due:

```java
@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void checkAndNotifyDueTasks() {
        List<Task> dueTasks = taskRepository.findTasksDueSoon();
        
        for (Task task : dueTasks) {
            eventPublisher.publishEvent(
                NotificationEvent.builder(this)
                    .userId(task.getUserId())
                    .type(NotificationType.TASK_DUE)
                    .title("Task Due Soon!")
                    .message("Your task \"" + task.getTitle() + "\" is due in 1 hour")
                    .data(Map.of(
                        "taskId", task.getId(),
                        "link", "/planner/tasks/" + task.getId()
                    ))
                    .build()
            );
        }
    }
}
```

---

## 📦 Dependencies

Add to `pom.xml` for push notifications (optional):

```xml
<!-- Web Push (optional - for F-N-07) -->
<dependency>
    <groupId>com.github.nickmcdowall</groupId>
    <artifactId>webpush-java</artifactId>
    <version>5.1.0</version>
</dependency>
```

---

## ⚙️ Configuration

Add to `application.properties`:

```properties
# Push Notifications (optional - for F-N-07)
push.vapid.public-key=YOUR_VAPID_PUBLIC_KEY
push.vapid.private-key=YOUR_VAPID_PRIVATE_KEY

# Generate VAPID keys using: npx web-push generate-vapid-keys
```

---

## 🧪 Testing Checklist

- [ ] Create notification → Verify in-app, email, push based on preferences
- [ ] Mark single notification as read → Verify status change
- [ ] Mark all as read → Verify all marked, cache evicted
- [ ] Update preferences → Verify persistence and respect in notification creation
- [ ] WebSocket connection → Verify real-time notifications received
- [ ] Email templates → Verify correct content and unsubscribe link
- [ ] Push subscription → Verify subscription saved and notifications sent
- [ ] Redis caching → Verify unread count caching and eviction
