package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.messaging.entity.MessageStatus;
import org.example.learnlink.modules.messaging.entity.MessageType;

import java.time.LocalDateTime;

/**
 * DTO for message response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
