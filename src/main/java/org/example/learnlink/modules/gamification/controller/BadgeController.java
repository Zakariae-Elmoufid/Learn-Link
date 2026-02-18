package org.example.learnlink.modules.gamification.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.BadgeResponse;
import org.example.learnlink.modules.gamification.dto.CreateBadgeRequest;
import org.example.learnlink.modules.gamification.service.BadgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> getBadgeById(@PathVariable Long badgeId) {
        BadgeResponse badge = badgeService.getBadgeById(badgeId);
        return ResponseEntity.ok(badge);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BadgeResponse> getBadgeByCode(@PathVariable String code) {
        BadgeResponse badge = badgeService.getBadgeByCode(code);
        return ResponseEntity.ok(badge);
    }

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BadgeResponse>> getActiveBadges() {
        List<BadgeResponse> badges = badgeService.getActiveBadges();
        return ResponseEntity.ok(badges);
    }

    @PostMapping
    public ResponseEntity<BadgeResponse> createBadge(@RequestBody CreateBadgeRequest request) {
        var badge = badgeService.createBadge(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BadgeResponse.builder()
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
                        .build());
    }

    @PutMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> updateBadge(@PathVariable Long badgeId,
                                                    @RequestBody CreateBadgeRequest request) {
        var badge = badgeService.updateBadge(badgeId, request);
        return ResponseEntity.ok(BadgeResponse.builder()
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
                .build());
    }

    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }
}

