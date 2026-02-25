package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for read receipt notification (outgoing to client)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptNotification {

    private Long messageId;
    private Long readerId;
    private LocalDateTime readAt;
}
