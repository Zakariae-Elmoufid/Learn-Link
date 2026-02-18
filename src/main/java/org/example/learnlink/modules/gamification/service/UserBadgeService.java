package org.example.learnlink.modules.gamification.service;

import org.example.learnlink.modules.gamification.dto.UserBadgeResponse;

import java.util.List;

public interface UserBadgeService {
    void awardBadgeToUser(Long userId, Long badgeId);
    List<UserBadgeResponse> getUserBadges(Long userId);
    long getUserBadgeCount(Long userId);
    boolean userHasBadge(Long userId, Long badgeId);
}

