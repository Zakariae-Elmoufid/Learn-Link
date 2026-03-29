package org.example.learnlink.modules.notification.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private Long userId;
    private Long unreadCount;
}
