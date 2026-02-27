package org.example.learnlink.modules.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.learnlink.modules.messaging.entity.MessageType;

/**
 * Request DTO for sending a message to a group (REST API).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Size(max = 500, message = "Attachment URL cannot exceed 500 characters")
    private String attachmentUrl;

    @Size(max = 255, message = "Attachment name cannot exceed 255 characters")
    private String attachmentName;
}
