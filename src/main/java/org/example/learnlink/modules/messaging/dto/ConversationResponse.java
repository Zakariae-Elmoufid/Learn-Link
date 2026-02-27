package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for conversation summary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long participantId;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
}
