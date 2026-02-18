package org.example.learnlink.modules.gamification.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadgeResponse {
    private Long badgeId;
    private String code;
    private String name;
    private String iconUrl;
    private String rarity;
    private Instant earnedAt;
}

