package org.example.learnlink.modules.messaging.dto;

import lombok.*;

/**
 * DTO for group typing indicator from client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTypingIndicator {
    
    private Long groupId;
    private boolean typing;
}
