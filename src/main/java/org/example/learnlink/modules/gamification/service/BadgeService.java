package org.example.learnlink.modules.gamification.service;

import org.example.learnlink.modules.gamification.dto.BadgeResponse;
import org.example.learnlink.modules.gamification.dto.CreateBadgeRequest;
import org.example.learnlink.modules.gamification.entity.Badge;

import java.util.List;

public interface BadgeService {
    Badge createBadge(CreateBadgeRequest request);
    BadgeResponse getBadgeById(Long badgeId);
    BadgeResponse getBadgeByCode(String code);
    List<BadgeResponse> getAllBadges();
    List<BadgeResponse> getActiveBadges();
    Badge updateBadge(Long badgeId, CreateBadgeRequest request);
    void deleteBadge(Long badgeId);
}

