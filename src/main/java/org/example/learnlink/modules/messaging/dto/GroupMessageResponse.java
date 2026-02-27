package org.example.learnlink.modules.messaging.dto;

import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

import java.time.LocalDateTime;

/**
 * Response DTO for a group message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageResponse {
    
    private Long id;
    private Long groupId;
    private String groupName;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private MessageType type;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int readCount;
    private boolean readByCurrentUser;
}
