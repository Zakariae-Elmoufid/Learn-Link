package org.example.learnlink.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityItem {
    private String type; // "connection", "post", "question", "answer", "comment"
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private int pointsEarned;
    private String badgeColor; // "gold", "silver", "bronze", "gray"
}
