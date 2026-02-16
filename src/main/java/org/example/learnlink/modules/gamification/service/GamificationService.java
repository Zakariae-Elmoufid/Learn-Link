package org.example.learnlink.modules.gamification.service;

import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.dto.UserPublicProfileResponse;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.entity.UserScore;


public interface GamificationService {
    UserScore addPoints(Long userId, AddPointsRequest request);
    UserScoreResponse getUserScore(Long userId);
    UserPublicProfileResponse getUserPublicProfile(Long userId);
}
