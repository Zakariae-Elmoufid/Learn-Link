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
