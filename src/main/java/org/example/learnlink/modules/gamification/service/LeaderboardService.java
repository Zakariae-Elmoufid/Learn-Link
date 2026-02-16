package org.example.learnlink.modules.gamification.service;

import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntryResponse> getGlobalLeaderboard(int limit);
    List<LeaderboardEntryResponse> getWeeklyLeaderboard(int limit);
    Integer getUserRank(Long userId);
    Long getUserRankPercentage(Long userId);
}

