package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.BadgeResponse;
import org.example.learnlink.modules.gamification.dto.CreateBadgeRequest;
import org.example.learnlink.modules.gamification.entity.Badge;
import org.example.learnlink.modules.gamification.entity.BadgeRarity;
import org.example.learnlink.modules.gamification.entity.BadgeType;
import org.example.learnlink.modules.gamification.exception.BadgeNotFoundException;
import org.example.learnlink.modules.gamification.repository.BadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BadgeServiceImp implements BadgeService {

    private final BadgeRepository badgeRepository;

    @Override
    public Badge createBadge(CreateBadgeRequest request) {
        log.info("Creating badge with code: {}", request.getCode());

        Badge badge = Badge.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .type(BadgeType.valueOf(request.getType()))
                .rarity(BadgeRarity.valueOf(request.getRarity()))
                .pointsRequired(request.getPointsRequired())
                .active(true)
                .build();

        badge = badgeRepository.save(badge);
        log.info("Badge created successfully with id: {}", badge.getId());
        return badge;
    }

    @Override
    public BadgeResponse getBadgeById(Long badgeId) {
        log.info("Fetching badge with id: {}", badgeId);
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));
        return mapToResponse(badge);
    }

    @Override
    public BadgeResponse getBadgeByCode(String code) {
        log.info("Fetching badge with code: {}", code);
        Badge badge = badgeRepository.findByCode(code)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with code: " + code));
        return mapToResponse(badge);
    }

    @Override
    public List<BadgeResponse> getAllBadges() {
        log.info("Fetching all badges");
        return badgeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BadgeResponse> getActiveBadges() {
        log.info("Fetching active badges");
        return badgeRepository.findByActive(true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Badge updateBadge(Long badgeId, CreateBadgeRequest request) {
        log.info("Updating badge with id: {}", badgeId);
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));

        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(request.getIconUrl());
        badge.setType(BadgeType.valueOf(request.getType()));
        badge.setRarity(BadgeRarity.valueOf(request.getRarity()));
        badge.setPointsRequired(request.getPointsRequired());

        badge = badgeRepository.save(badge);
        log.info("Badge updated successfully");
        return badge;
    }

    @Override
    public void deleteBadge(Long badgeId) {
        log.info("Deleting badge with id: {}", badgeId);
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));
        badge.setActive(false);
        badgeRepository.save(badge);
        log.info("Badge deactivated successfully");
    }

    private BadgeResponse mapToResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .code(badge.getCode())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .type(badge.getType().name())
                .rarity(badge.getRarity().name())
                .pointsRequired(badge.getPointsRequired())
                .active(badge.getActive())
                .createdAt(badge.getCreatedAt())
                .build();
    }
}

