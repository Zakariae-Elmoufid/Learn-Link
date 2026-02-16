package org.example.learnlink.modules.gamification.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificationServiceImp  implements    GamificationService{

    private final UserScoreRepository userScoreRepository;

        @Override
        public UserScore addPoints(Long userId, AddPointsRequest request) {
            log.info("Adding {} points to user {} for action {}", request.getPoints(), userId, request.getActionType());

            UserScore userScore = userScoreRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserScore newScore = UserScore.builder()
                                .userId(userId)
                                .totalPoints(0)
                                .level(1)
                                .currentLevelPoints(0)
                                .pointsForNextLevel(100)
                                .build();
                        return userScoreRepository.save(newScore);
                    });

            int previousLevel = userScore.getLevel();
            userScore.addPoints(request.getPoints());
            userScore = userScoreRepository.save(userScore);

            log.info("Points added successfully. New total: {}, Level: {}",
                    userScore.getTotalPoints(), userScore.getLevel());

            return userScore;
        }

        @Override
        public UserScoreResponse getUserScore(Long userId) {
            UserScore userScore = userScoreRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User score not found"));
            return UserScoreResponse.builder()
                    .userId(userScore.getUserId())
                    .totalPoints(userScore.getTotalPoints())
                    .level(userScore.getLevel())
                    .currentLevelPoints(userScore.getCurrentLevelPoints())
                    .pointsForNextLevel(userScore.getPointsForNextLevel())
                    .progressPercentage((double) userScore.getCurrentLevelPoints() /
                            userScore.getPointsForNextLevel() * 100)
                    .build();
        }
}
