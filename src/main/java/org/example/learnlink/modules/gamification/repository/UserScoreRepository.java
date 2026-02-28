package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    
    Optional<UserScore> findByUserId(long userId);
    
    // Admin statistics queries
    
    /**
     * Count users with score updates after a specific date (active users)
     */
    long countByUpdatedAtAfter(Instant since);
    
    /**
     * Sum total points awarded across all users
     */
    @Query("SELECT COALESCE(SUM(us.totalPoints), 0) FROM UserScore us")
    long sumTotalPoints();
}
