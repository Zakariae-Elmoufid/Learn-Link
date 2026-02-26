package org.example.learnlink.modules.messaging.dto;

import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

/**
 * Request DTO for sending a message via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatMessageRequest {
    
    private Long groupId;
    private String content;
    
    @Builder.Default
    private MessageType type = MessageType.TEXT;
    
    private String attachmentUrl;
    private String attachmentName;
}
