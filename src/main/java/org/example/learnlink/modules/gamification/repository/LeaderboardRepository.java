package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.dto.LeaderboardEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LeaderboardRepository {

    private final EntityManager entityManager;

    public List<LeaderboardEntryResponse> getGlobalLeaderboard(Pageable pageable) {
        String sql = """
            SELECT 
                us.user_id,
                us.level,
                us.total_points,
                COUNT(DISTINCT ub.badge_id) as badge_count
            FROM user_scores us
            LEFT JOIN user_badges ub ON us.user_id = ub.user_id
            GROUP BY us.user_id, us.level, us.total_points
            ORDER BY us.total_points DESC, us.level DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> LeaderboardEntryResponse.builder()
                .userId((Long) row[0])
                .level((Integer) row[1])
                .totalPoints((Integer) row[2])
                .badgeCount(((Number) row[3]).longValue())
                .build())
            .limit(pageable.getPageSize())
            .collect(java.util.stream.Collectors.toList())
            .stream()
            .map(entry -> entry)
            .collect(java.util.stream.Collectors.toList());
    }

    public List<LeaderboardEntryResponse> getWeeklyLeaderboard(int limit) {
        String sql = """
            SELECT 
                us.user_id,
                us.level,
                us.total_points,
                COUNT(DISTINCT ub.badge_id) as badge_count
            FROM user_scores us
            LEFT JOIN user_badges ub ON us.user_id = ub.user_id
            WHERE us.updated_at >= CURRENT_DATE - INTERVAL 7 DAY
            GROUP BY us.user_id, us.level, us.total_points
            ORDER BY us.total_points DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);

        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> LeaderboardEntryResponse.builder()
                .userId((Long) row[0])
                .level((Integer) row[1])
                .totalPoints((Integer) row[2])
                .badgeCount(((Number) row[3]).longValue())
                .build())
            .toList();
    }
}

