package org.example.learnlink.modules.messaging.dto;

import lombok.*;

/**
 * DTO for group message read receipt from client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupReadReceipt {
    
    private Long groupId;
    private Long messageId;
}
