package org.example.learnlink.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CurrentUser;
import org.example.learnlink.modules.auth.security.UserPrincipal;
import org.example.learnlink.modules.notification.dto.request.MarkReadRequest;
import org.example.learnlink.modules.notification.dto.response.NotificationResponse;
import org.example.learnlink.modules.notification.dto.response.UnreadCountResponse;
import org.example.learnlink.modules.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== F-N-01: In-app Notifications ====================

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Returns paginated list of user notifications")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @CurrentUser UserPrincipal user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @CurrentUser UserPrincipal user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(user.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    // ==================== F-N-02: Badge Counter ====================

    @GetMapping("/count")
    @Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@CurrentUser UserPrincipal user) {
        UnreadCountResponse count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }

    // ==================== F-N-03: Mark as Read ====================

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        NotificationResponse notification = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/read")
    @Operation(summary = "Mark multiple notifications as read")
    public ResponseEntity<Map<String, Integer>> markMultipleAsRead(
            @CurrentUser UserPrincipal user,
            @Valid @RequestBody MarkReadRequest request) {
        int count = notificationService.markMultipleAsRead(request.getNotificationIds(), user.getId());
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    // ==================== F-N-04: Mark All as Read ====================

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@CurrentUser UserPrincipal user) {
        int count = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    // ==================== Delete Notification ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
