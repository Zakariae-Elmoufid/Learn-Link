package org.example.learnlink.modules.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadRequest {
    @NotEmpty
    private List<Long> notificationIds;
}
