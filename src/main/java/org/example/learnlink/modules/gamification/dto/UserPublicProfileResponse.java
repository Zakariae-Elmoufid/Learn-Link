package org.example.learnlink.modules.gamification.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicProfileResponse {
    private Long userId;
    private String username;
    private Integer level;
    private Integer totalPoints;
    private Integer rank;
    private Long badgeCount;
    private List<UserBadgeResponse> badges;
}

