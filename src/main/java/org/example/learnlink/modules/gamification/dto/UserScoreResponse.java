package org.example.learnlink.modules.gamification.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScoreResponse {
    private long userId;
    private Integer totalPoints;
    private Integer level;
    private Integer currentLevelPoints;
    private Integer pointsForNextLevel;
    private Double progressPercentage;
}