package org.example.learnlink.modules.gamification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private Long userId;
    private String username;
    private Integer level;
    private Integer totalPoints;
    private Integer rank;
    private Long badgeCount;
}

