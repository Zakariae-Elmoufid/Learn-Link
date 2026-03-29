package org.example.learnlink.modules.gamification.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.example.learnlink.modules.gamification.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntryResponse>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "100") int limit) {
        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getGlobalLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<LeaderboardEntryResponse>> getWeeklyLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getWeeklyLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<Integer> getUserRank(@PathVariable Long userId) {
        Integer rank = leaderboardService.getUserRank(userId);
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/rank-percentage/{userId}")
    public ResponseEntity<Long> getUserRankPercentage(@PathVariable Long userId) {
        Long percentage = leaderboardService.getUserRankPercentage(userId);
        return ResponseEntity.ok(percentage);
    }
}

