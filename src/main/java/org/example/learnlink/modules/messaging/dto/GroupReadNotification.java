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
