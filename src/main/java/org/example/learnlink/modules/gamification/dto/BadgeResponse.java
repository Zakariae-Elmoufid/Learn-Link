package org.example.learnlink.modules.gamification.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private String type;
    private String rarity;
    private Integer pointsRequired;
    private Boolean active;
    private Instant createdAt;
}

