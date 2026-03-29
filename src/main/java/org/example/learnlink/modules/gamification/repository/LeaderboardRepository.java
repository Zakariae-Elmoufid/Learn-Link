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
                ranked.rank,
                ranked.user_id,
                ranked.username,
                ranked.level,
                ranked.total_points,
                ranked.badge_count
            FROM (
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY us.total_points DESC, us.level DESC) as rank,
                    us.user_id,
                    u.username,
                    us.level,
                    us.total_points,
                    COUNT(DISTINCT ub.badge_id) as badge_count
                FROM user_scores us
                LEFT JOIN users u ON us.user_id = u.id  
                LEFT JOIN user_badges ub ON us.user_id = ub.user_id
                GROUP BY us.user_id, u.username, us.level, us.total_points
            ) ranked
            ORDER BY ranked.rank ASC
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> LeaderboardEntryResponse.builder()
                .rank(((Number) row[0]).intValue())
                .userId((Long) row[1])
                .username((String) row[2])
                .level((Integer) row[3])
                .totalPoints((Integer) row[4])
                .badgeCount(((Number) row[5]).longValue())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    public List<LeaderboardEntryResponse> getWeeklyLeaderboard(int limit) {
        String sql = """
            SELECT 
                ranked.rank,
                ranked.user_id,
                ranked.username,
                ranked.level,
                ranked.total_points,
                ranked.badge_count
            FROM (
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY us.total_points DESC, us.level DESC) as rank,
                    us.user_id,
                    u.username,
                    us.level,
                    us.total_points,
                    COUNT(DISTINCT ub.badge_id) as badge_count
                FROM user_scores us
                LEFT JOIN users u ON us.user_id = u.id
                LEFT JOIN user_badges ub ON us.user_id = ub.user_id
                WHERE us.updated_at >= CURRENT_DATE - INTERVAL 7 DAY
                GROUP BY us.user_id, u.username, us.level, us.total_points
            ) ranked
            ORDER BY ranked.rank ASC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);

        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> LeaderboardEntryResponse.builder()
                .rank(((Number) row[0]).intValue())
                .userId((Long) row[1])
                .username((String) row[2])
                .level((Integer) row[3])
                .totalPoints((Integer) row[4])
                .badgeCount(((Number) row[5]).longValue())
                .build())
            .toList();
    }
}

