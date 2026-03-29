package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.messaging.entity.MessageType;

/**
 * DTO for WebSocket chat message (incoming from client)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private Long recipientId;
    private String content;
    
    @Builder.Default
    private MessageType type = MessageType.TEXT;
    
    private String attachmentUrl;
    private String attachmentName;
}
