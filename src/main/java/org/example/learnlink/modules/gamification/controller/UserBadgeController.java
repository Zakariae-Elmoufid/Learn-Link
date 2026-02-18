package org.example.learnlink.modules.gamification.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.UserBadgeResponse;
import org.example.learnlink.modules.gamification.service.UserBadgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(@PathVariable Long userId) {
        List<UserBadgeResponse> badges = userBadgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<Long> getUserBadgeCount(@PathVariable Long userId) {
        long count = userBadgeService.getUserBadgeCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/has/{badgeId}")
    public ResponseEntity<Boolean> userHasBadge(@PathVariable Long userId, @PathVariable Long badgeId) {
        boolean hasBadge = userBadgeService.userHasBadge(userId, badgeId);
        return ResponseEntity.ok(hasBadge);
    }

    @PostMapping("/{userId}/award/{badgeId}")
    public ResponseEntity<Void> awardBadgeToUser(@PathVariable Long userId, @PathVariable Long badgeId) {
        userBadgeService.awardBadgeToUser(userId, badgeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

