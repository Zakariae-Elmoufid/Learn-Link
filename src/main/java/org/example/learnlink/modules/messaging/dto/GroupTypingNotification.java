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
    private String userName;
    private Long groupId;
    private boolean typing;
    private LocalDateTime timestamp;
}
