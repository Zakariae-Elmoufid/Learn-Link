package org.example.learnlink.modules.gamification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBadgeRequest {
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private String type;
    private String rarity;
    private Integer pointsRequired;
}

