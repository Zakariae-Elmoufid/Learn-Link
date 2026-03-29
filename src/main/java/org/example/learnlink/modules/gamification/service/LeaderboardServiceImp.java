package org.example.learnlink.modules.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.exception.UserScoreNotFoundException;
import org.example.learnlink.modules.gamification.repository.LeaderboardRepository;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaderboardServiceImp implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserScoreRepository userScoreRepository;

    @Override
    public List<LeaderboardEntryResponse> getGlobalLeaderboard(int limit) {
        log.info("Fetching global leaderboard with limit: {}", limit);
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return leaderboardRepository.getGlobalLeaderboard(pageable)
                .stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<LeaderboardEntryResponse> getWeeklyLeaderboard(int limit) {
        log.info("Fetching weekly leaderboard with limit: {}", limit);
        return leaderboardRepository.getWeeklyLeaderboard(limit);
    }

    @Override
    public Integer getUserRank(Long userId) {
        log.info("Fetching rank for user: {}", userId);

        UserScore userScore = userScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new UserScoreNotFoundException("User score not found"));

        List<UserScore> allScores = userScoreRepository.findAll();

        int rank = 1;
        for (UserScore score : allScores) {
            if (score.getTotalPoints() > userScore.getTotalPoints()) {
                rank++;
            } else if (score.getTotalPoints() == userScore.getTotalPoints() &&
                      score.getLevel() > userScore.getLevel()) {
                rank++;
            }
        }

        return rank;
    }

    @Override
    public Long getUserRankPercentage(Long userId) {
        log.info("Fetching rank percentage for user: {}", userId);

        List<UserScore> allScores = userScoreRepository.findAll();
        Integer userRank = getUserRank(userId);

        if (allScores.isEmpty()) {
            return 100L;
        }

        long percentage = (long) ((allScores.size() - userRank + 1) * 100 / allScores.size());
        return Math.min(100L, Math.max(0L, percentage));
    }
}

