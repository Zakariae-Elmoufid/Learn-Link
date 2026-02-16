package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long userId);

    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(DISTINCT ub.badgeId) FROM UserBadge ub WHERE ub.userId = :userId")
    long countUniqueBadgesByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);
}

