package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for typing notification (outgoing to client)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingNotification {

    private Long senderId;
    private boolean typing;
    private LocalDateTime timestamp;
}
