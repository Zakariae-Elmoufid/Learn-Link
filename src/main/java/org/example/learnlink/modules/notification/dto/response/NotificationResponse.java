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
