package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreRepository  extends JpaRepository<UserScore, Long> {
    Optional<UserScore> findByUserId(long userId);

}
