package org.example.learnlink.modules.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for mark message as read request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptRequest {

    private Long messageId;
}
