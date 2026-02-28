package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.UserBadgeResponse;
import org.example.learnlink.modules.gamification.entity.Badge;
import org.example.learnlink.modules.gamification.entity.UserBadge;
import org.example.learnlink.modules.gamification.event.BadgeEarnedEvent;
import org.example.learnlink.modules.gamification.exception.BadgeNotFoundException;
import org.example.learnlink.modules.gamification.exception.UserScoreNotFoundException;
import org.example.learnlink.modules.gamification.repository.BadgeRepository;
import org.example.learnlink.modules.gamification.repository.UserBadgeRepository;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserBadgeServiceImp implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final UserScoreRepository userScoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void awardBadgeToUser(Long userId, Long badgeId) {
        log.info("Awarding badge {} to user {}", badgeId, userId);

        // Verify user exists
        userScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new UserScoreNotFoundException("User score not found for user: " + userId));

        // Verify badge exists
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));

        // Check if user already has this badge
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            log.warn("User {} already has badge {}", userId, badgeId);
            return;
        }

        UserBadge userBadge = UserBadge.builder()
                .userId(userId)
                .badgeId(badgeId)
                .build();

        userBadgeRepository.save(userBadge);
        
        // Publish event for admin stats tracking
        eventPublisher.publishEvent(new BadgeEarnedEvent(this, userId, badgeId, badge.getCode()));
        
        log.info("Badge {} awarded successfully to user {}", badgeId, userId);
    }

    @Override
    public List<UserBadgeResponse> getUserBadges(Long userId) {
        log.info("Fetching badges for user: {}", userId);

        return userBadgeRepository.findByUserId(userId)
                .stream()
                .map(userBadge -> {
                    Badge badge = badgeRepository.findById(userBadge.getBadgeId())
                            .orElseThrow(() -> new BadgeNotFoundException("Badge not found"));
                    return mapToResponse(userBadge, badge);
                })
                .toList();
    }

    @Override
    public long getUserBadgeCount(Long userId) {
        log.info("Counting badges for user: {}", userId);
        return userBadgeRepository.countByUserId(userId);
    }

    @Override
    public boolean userHasBadge(Long userId, Long badgeId) {
        return userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId);
    }

    private UserBadgeResponse mapToResponse(UserBadge userBadge, Badge badge) {
        return UserBadgeResponse.builder()
                .badgeId(badge.getId())
                .code(badge.getCode())
                .name(badge.getName())
                .iconUrl(badge.getIconUrl())
                .rarity(badge.getRarity().name())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}

